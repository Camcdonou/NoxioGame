package org.infpls.noxio.game.module.game.session.login;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketL01 extends Packet {
  private final String name, location, description;
  public PacketL01(final String name, final String location, final String description) {
    super("l01");
    this.name = name; this.location = location; this.description = description;
  }
  
  public String getName() { return name; }
  public String getLocation() { return location; }
  public String getDescription() { return description; }    
}
