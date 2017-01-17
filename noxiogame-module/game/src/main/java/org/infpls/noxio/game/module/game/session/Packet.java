package org.infpls.noxio.game.module.game.session;

public class Packet {
  private final String type;
  public Packet(final String type) { this.type = type; }
  public final String getType() { return type; }
}
