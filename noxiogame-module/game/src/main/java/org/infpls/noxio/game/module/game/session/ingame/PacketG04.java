package org.infpls.noxio.game.module.game.session.ingame;

import java.util.List;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG04 extends Packet {
  private final List<String> players;
  public PacketG04(final List<String> players) {
    super("g04");
    this.players = players;
  }
  
  public List<String> getPlayers() { return players; }
}
