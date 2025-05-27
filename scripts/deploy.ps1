$microservicename="MQTT-DEMO"
$serverAddress = "192.168.1.102"

cd ..

mvn package

ssh paas@${serverAddress} "mkdir -p /home/paas/microservices/$microservicename/"

scp .\target\*.jar paas@${serverAddress}:/home/paas/microservices/$microservicename/
scp .\scripts\* paas@${serverAddress}:/home/paas/microservices/$microservicename/

ssh paas@${serverAddress} "cd /home/paas/microservices/$microservicename;chmod u+x *.sh;bash ./restart.sh"

mvn clean

cd scripts