package org.infpls.noxio.game.module.game.dao.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServerInfoDao {
  
    @Value("${noxio.game.server.name}")
    private String name;
    @Value("${noxio.game.server.description}")
    private String description;
    @Value("${noxio.game.server.location}")
    private String location;
    
    @Value("${noxio.auth.server.address}")
    private String address;
    @Value("${noxio.auth.server.port}")
    private int port;
    
    public ServerInfo getServerInfo() {
      return new ServerInfo(name, description, location);
    }
    
    public String getAuthServerAddress() { 
      return address + ":" + port;
    }
}
