package com.liuyang.mqtt_demo.controller;

import com.liuyang.mqtt_demo.service.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqttController {

    @Autowired
    private MqttService mqttService;

    @GetMapping("/publish")
    public String publishMessage(@RequestParam String message) {
        try {
            mqttService.publish(message);
            return "Message published: " + message;
        } catch (Exception e) {
            return "Failed to publish message: " + e.getMessage();
        }
    }

    @GetMapping("/subscribe")
    public String subscribe() {
        try {
            mqttService.subscribe();
            return "Subscribed to topic";
        } catch (Exception e) {
            return "Failed to subscribe: " + e.getMessage();
        }
    }
}