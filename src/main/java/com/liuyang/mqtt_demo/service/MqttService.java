package com.liuyang.mqtt_demo.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuyang.mqtt_demo.Entity.TemperatureSensorData;
import jakarta.annotation.PostConstruct;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;


@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private static final String INFLUXDB_URL = "http://IOT-INFLUXDB-API/influxdb/insertData";
    private static final String REDIS_SETSTRING = "http://REDISPROJECT/setString";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static ExecutorService executor = new ThreadPoolExecutor(
            1,
            2,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    ExpiringMap<String,String> deviceExpiringMap = ExpiringMap.builder()
            .maxSize(100)
            .expiration(180, TimeUnit.SECONDS)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expirationListener((k, v) -> recordDevicesOffLine((String) k, (String) v))
            .build();

    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    private MqttClient mqttClient;

    @Value("${mqtt.broker.topic}")
    private String topic;

    @Value("${mqtt.broker.qos}")
    private int qos;

    public void subscribe() throws MqttException {
        mqttClient.subscribe(topic, qos, (topic, message) -> {
            logger.info("Received message: {}", new String(message.getPayload()));
                executor.submit(()-> {
                    try {
                        sendDataToInfluxDB(new String(message.getPayload()));
                        sendDataToRedis(new String(message.getPayload()));
                        recordDeivcesOnLine(new String(message.getPayload()));
                    } catch (Exception e) {
                        logger.error(" , e", e);
                    }
                });

        });
    }

    /**
     * 记录设备在线状态
     * @param message 原始消息，心跳消息
     */
    public void recordDeivcesOnLine(String message){
        JSONObject mqttMessage = JSONObject.parseObject(message);
        if(!StringUtils.equals(mqttMessage.getString("msgType"), "heartbeat")){
            return;
        }
        deviceExpiringMap.put(mqttMessage.getString("source"), mqttMessage.getString("source"));
        logger.info("device on line: {}", mqttMessage.getString("source"));
    }

    /**
     * 记录设备离线状态
     * @param deivceId 设备id
     * @param source 设备来源
     */
    public void recordDevicesOffLine(String deivceId, String source){
        try {
            JSONObject message = new JSONObject();
            message.put("msgType", "offline");
            message.put("source", source);
            message.put("timestamp", System.currentTimeMillis());

            MqttMessage mqttMessage = new MqttMessage(message.toJSONString().getBytes());
            Integer tmpQos = message.getInteger("qos");
            mqttMessage.setQos(tmpQos == null ? qos : tmpQos);

            mqttClient.publish("GlobalNotification", mqttMessage);
        } catch (MqttException e) {
            logger.error("{}", e);
        }
        logger.info("device offline: {}", source);
    }

    public void sendDataToRedis(String message) throws JsonProcessingException {
        logger.info("message: {}", message);
        JSONObject mqttMessage = JSONObject.parseObject(message);
        if(!StringUtils.equals(mqttMessage.getString("msgType"), "heartbeat")){
            return;
        }

        JSONObject redisData = new JSONObject();
        redisData.put("key", mqttMessage.getString("source"));
        redisData.put("value", mqttMessage.getString("source"));
        redisData.put("timeout", 60);

        logger.info("redisData minioData: {}", redisData);

        if(redisData.isEmpty()){
            return;
        }
        String response = restTemplate.postForObject(REDIS_SETSTRING, redisData, String.class);
        logger.info("resposne is {}", response);
    }

    private void sendDataToInfluxDB(String message) throws JsonProcessingException {
        logger.info("message: {}", message);
        JSONObject influxDBData = new JSONObject();

        JsonNode jsonNode = OBJECT_MAPPER.readTree(message);
        logger.info("msgType is {}",jsonNode.get("msgType").asText());
        if(StringUtils.equals(jsonNode.get("msgType").asText(), "temp-humi")){
            TemperatureSensorData temperatureSensorData = OBJECT_MAPPER.readValue(message, TemperatureSensorData.class);

            influxDBData.put("database", "db_iot");
            influxDBData.put("table","t_temperature");
            JSONObject tags = new JSONObject();
            tags.put("deviceId", temperatureSensorData.getSource());
            tags.put("location", temperatureSensorData.getSource());

            JSONObject fields = new JSONObject();
            fields.put("temperature", temperatureSensorData.getTemperature());
            fields.put("humidity", temperatureSensorData.getHumidity());

            influxDBData.put("tags", tags);
            influxDBData.put("fields", fields);
        }else if(StringUtils.equals(jsonNode.get("msgType").toString(), "heartbeat")){

        }


        logger.info("influxDBData minioData: {}", influxDBData);

        if(influxDBData.isEmpty()){
            return;
        }
        String resposne = restTemplate.postForObject(INFLUXDB_URL, influxDBData, String.class);
        logger.info("resposne is {}", resposne);

    }

    public void publish(String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(qos);
        mqttClient.publish(topic, mqttMessage);
    }

    public boolean publish(JSONObject message)  {
        MqttMessage mqttMessage = new MqttMessage(message.getString("message").getBytes());
        Integer tmpQos = message.getInteger("qos");
        mqttMessage.setQos(tmpQos == null ? qos : tmpQos);
        String tmpTopic = message.getString("topic");

        boolean flag = false;
        try {
            mqttClient.publish(tmpTopic, mqttMessage);
            flag = true;
        } catch (MqttException e) {
            logger.error("{}", e);
        }
        logger.info("flag {}", flag);
        return flag;
    }

    @PostConstruct // 在 Bean 初始化完成后自动执行
    public void init() {
        try {
            subscribe();
            logger.info("Subscribed to MQTT topic: {}", topic);
        } catch (MqttException e) {
            logger.error("Failed to subscribe to MQTT topic: " + e.getMessage());
        }
    }
}