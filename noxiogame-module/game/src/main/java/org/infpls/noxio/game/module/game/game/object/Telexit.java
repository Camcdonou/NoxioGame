package org.infpls.noxio.game.module.game.game.object;

import org.infpls.noxio.game.module.game.game.*;

public class Telexit extends GameObject {
  private static final float RADIUS = .1f;
  
  private boolean fxUpd; // When true we send a trigger for an effect to play on this object next frame
  
  public Telexit(final NoxioGame game, final int oid, final Vec2 position) {
    super(game, oid, position, 0);
    /* Bitmask Type */
    bitIs = bitIs | GameObject.Types.MAPOBJ;
  }
    
  @Override
  public void step() {
    /* Literally does nothing but sit here lol */
  }
  
  /* Play visual effect */
  public void fx() {
    fxUpd = true;
  }
  
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<int team>;<vec2 pos>;<vec2 size>;
  */
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    sb.append(fxUpd?1:0);
    sb.append(";");
    fxUpd = false;
  }
  
  @Override
  public boolean isGlobal() { return true; }
  
  @Override
  public void kill() { }
  
  @Override
  public void destroyx() { }
  
  @Override
  public String type() { return "telx"; }
}
