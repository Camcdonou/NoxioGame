echo [-- Shutdown Tomcat Server --]
/home/inferno/Tomcat/bin/shutdown.sh

echo [-- Build WAR --]
mvn clean install

echo [-- Copy WAR --]
rm /home/inferno/Tomcat/webapps/noxiogame.war
cp noxiogame-core/target/NoxioGame-1.0.war /home/inferno/Tomcat/webapps/noxiogame.war

echo [-- Start Tomcat Server --]
/home/inferno/Tomcat/bin/startup.sh

