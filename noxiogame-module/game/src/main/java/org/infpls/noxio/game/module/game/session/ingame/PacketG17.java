package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.game.NoxioMap;

public class PacketG17 extends Packet {
  private final String name;
  private final int maxPlayers;
  private final NoxioMap map;
  public PacketG17(final String name, final int maxPlayers, final NoxioMap map) {
    super("g17");
    this.name = name;
    this.maxPlayers = maxPlayers;
    this.map = map;
  }
  
  public String getName() { return name; } 
  public int getMaxPlayers() { return maxPlayers; }
  public NoxioMap getMap() { return map; }
}