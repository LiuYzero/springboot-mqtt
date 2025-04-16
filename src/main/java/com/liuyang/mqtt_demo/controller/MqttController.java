package com.liuyang.mqtt_demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.liuyang.mqtt_demo.Entity.ResponseResult;
import com.liuyang.mqtt_demo.service.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MqttController {
    private static final Logger logger = LoggerFactory.getLogger(MqttController.class);
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

    @PostMapping("/publish")
    public ResponseResult plushMessage(@RequestBody JSONObject message){
        logger.info("publishMessage:{}",message);
        if(mqttService.publish(message)){
        return ResponseResult.success();}else{
            return ResponseResult.fail();
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