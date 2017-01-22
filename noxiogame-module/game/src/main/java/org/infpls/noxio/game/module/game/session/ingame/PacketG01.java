package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG01 extends Packet {
  private final String name;
  private final int maxPlayers;
  public PacketG01(final String name, final int maxPlayers) {
    super("g01");
    this.name = name;
    this.maxPlayers = maxPlayers;
  }
  
  public String getName() { return name; } 
  public int getMaxPlayers() { return maxPlayers; }
}
