package org.infpls.noxio.game.module.game.dao.server;

import java.util.List;
import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/* This class is basically just access to the noxio.properties file. 
   Contains various settings and information.
*/

@Component
public class InfoDao {
  
  @Autowired
  private DaoContainer dao;
  
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
  
  public AdvInfo getAdvInfo() {
    final List<String> users = dao.getUserDao().getOnlineUserList();
    final List<String> lobbies = dao.getLobbyDao().getOnlineLobbyList();
    return new AdvInfo(name, location, address, users, lobbies);
  }
    
  public class AdvInfo {
    public final String name, location, address;
    public final List<String> users, lobbies;
    public AdvInfo(final String name, final String location, final String address, final List users, final List lobbies) {
      this.name = name;
      this.location = location;
      this.address = address;
      this.users = users;
      this.lobbies = lobbies;
    }
  }
}
