#!/bin/bash
tag=$(date +"%Y-%m-%d")

docker build -t registry.cn-hangzhou.aliyuncs.com/mine-maojindao/mine-eureka:${tag} .

docker images

docker push registry.cn-hangzhou.aliyuncs.com/mine-maojindao/mine-eureka:${tag}