package com.liuyang.mqtt_demo.service;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    @Autowired
    private MqttClient mqttClient;

    @Value("${mqtt.broker.topic}")
    private String topic;

    @Value("${mqtt.broker.qos}")
    private int qos;

    public void subscribe() throws MqttException {
        mqttClient.subscribe(topic, qos, (topic, message) -> {
            System.out.println("Received message: " + new String(message.getPayload()));
        });
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