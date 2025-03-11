package com.liuyang.mqtt_demo.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuyang.mqtt_demo.Entity.TemperatureSensorData;
import jakarta.annotation.PostConstruct;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static ExecutorService executor = new ThreadPoolExecutor(
            1,
            2,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.DiscardPolicy()
    );

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
                    } catch (JsonProcessingException e) {
                        logger.error(" , e", e);
                    }
                });

        });
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

    @PostConstruct // 在 Bean 初始化完成后自动执行
    public void init() {
        try {
            subscribe();
            System.out.println("Subscribed to MQTT topic: " + topic);
        } catch (MqttException e) {
            System.err.println("Failed to subscribe to MQTT topic: " + e.getMessage());
        }
    }
}