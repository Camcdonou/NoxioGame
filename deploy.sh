echo [-- Shutdown Tomcat Server --]
/home/inferno/Tomcat/bin/shutdown.sh

echo [-- Compile Assets --]
rm -r /home/inferno/dev/NoxioAsset/asset/map/min
java -jar /home/inferno/dev/NoxioAsset/dist/NoxioAssetConverter.jar maps /home/inferno/dev/NoxioAsset/asset/map/

echo [-- Copy Assets --]
rm -r /home/inferno/dev/NoxioGame/noxiogame-core/src/main/resources/map
cp -r /home/inferno/dev/NoxioAsset/asset/map/min /home/inferno/dev/NoxioGame/noxiogame-core/src/main/resources
mv /home/inferno/dev/NoxioGame/noxiogame-core/src/main/resources/min /home/inferno/dev/NoxioGame/noxiogame-core/src/main/resources/map

echo [-- Build WAR --]
mvn clean install

echo [-- Copy WAR --]
rm /home/inferno/Tomcat/webapps/nxg.war
cp noxiogame-core/target/NoxioGame-1.0.war /home/inferno/Tomcat/webapps/nxg.war

echo [-- Start Tomcat Server --]
/home/inferno/Tomcat/bin/startup.sh

