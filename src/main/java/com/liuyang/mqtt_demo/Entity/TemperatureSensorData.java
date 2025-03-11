package com.liuyang.mqtt_demo.Entity;

/**
 * dht11 温湿度传感器数据
 * @author liuyang
 */
public class TemperatureSensorData {
    private String source;
    private String target;
    private String msgType;
    private double temperature;
    private double humidity;

    // 无参构造函数
    public TemperatureSensorData() {
    }

    // 带参构造函数
    public TemperatureSensorData(String source, String target, String msgType, double temperature, double humidity) {
        this.source = source;
        this.target = target;
        this.msgType = msgType;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    // Getter 和 Setter 方法
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    // toString 方法
    @Override
    public String toString() {
        return "SensorData{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", msgType='" + msgType + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                '}';
    }
}
