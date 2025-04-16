package com.liuyang.mqtt_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class MqttDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqttDemoApplication.class, args);
	}

}
