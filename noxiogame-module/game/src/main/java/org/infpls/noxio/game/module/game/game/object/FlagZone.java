package org.infpls.noxio.game.module.game.game.object;

import org.infpls.noxio.game.module.game.game.*;

public class FlagZone extends GameObject {
  
  public final Vec2 size;
  
  public FlagZone(final NoxioGame game, final int oid, final Vec2 position, final int team, final Vec2 size) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    bitIs = bitIs | Types.ZONE;
    
    /* Vars */
    this.size = size;
  }
    
  @Override
  public void step() { /* Just kinda sit their pls */ }
  
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<int team>;<vec2 pos>;<vec2 size>;
  */
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    size.toString(sb); sb.append(";");
  }
  
  @Override
  public boolean isGlobal() { return true; }
  
  @Override
  public String type() { return "flz"; }
}
