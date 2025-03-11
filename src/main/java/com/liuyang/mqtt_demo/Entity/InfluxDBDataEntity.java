package com.liuyang.mqtt_demo.Entity;

import java.util.Map;

/**
 * InlfuxDB 数据实体类
 *
 * @author liuyang
 * @since  2025年3月11日 19点24分
 */
public class InfluxDBDataEntity {
    String database;
    String table;
    Map<String, Object> tags;

    Map<String, Object> fields;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "InlfuxDBDataEntity{" +
                "database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", tags=" + tags +
                ", fields=" + fields +
                '}';
    }
}
