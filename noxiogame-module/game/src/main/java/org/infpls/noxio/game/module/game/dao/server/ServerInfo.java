package org.infpls.noxio.game.module.game.dao.server;

public class ServerInfo {
  private final String name, description, location;
  ServerInfo(String name, String description, String location) {
    this.name = name; this.description = description; this.location = location;
  }
  
  public String getName() { return name; }
  public String getDescription() { return description; }
  public String getLocation() { return location; }
}
