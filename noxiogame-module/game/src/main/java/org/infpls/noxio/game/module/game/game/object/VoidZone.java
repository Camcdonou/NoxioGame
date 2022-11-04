package org.infpls.noxio.game.module.game.game.object;

import org.infpls.noxio.game.module.game.game.*;

public class VoidZone extends GameObject {
  private static final float SUCC_STRENGTH = 0.115f;
  private static final float UNSAFE_RANGE = 0.05f;
  
  private boolean fxUpd; // When true we send a trigger for an effect to play on this object next frame
  
  private final float size;   // Size of black hole
  
  public VoidZone(final NoxioGame game, final int oid, final Vec2 position, final int s) {
    super(game, oid, position, 0);
    /* Bitmask Type */
    bitIs = bitIs | GameObject.Types.MAPOBJ;
    
    fxUpd = false;
    size = Math.max(2.5f, (float)s);
  }
    
  @Override
  public void step() {
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      final Controller con = game.getControllerByObject(obj);
      if(con != null && obj.is(GameObject.Types.MOBILE) && !(obj instanceof Poly)) {
        final Mobile mob = (Mobile)obj;
        
        float dist = mob.position.distance(position);
        if(dist < size) {
          float str = (float)Math.pow(1f - Math.max(0f, Math.min(1f, dist/size)), 1.95f);
          Vec2 dir = position.subtract(mob.position).normalize();
          Vec2 force = dir.scale(SUCC_STRENGTH*str);
          
          if(dist < UNSAFE_RANGE + mob.radius) {
            mob.destroyx();
            fx();
          }

          mob.knockback(force);
        }
      }
    }
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
  public String type() { return "void"; }
}
