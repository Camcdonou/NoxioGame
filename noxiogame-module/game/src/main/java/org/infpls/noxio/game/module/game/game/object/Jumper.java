package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;

public class Jumper extends GameObject {
  private static final int COOLDOWN = 15;
  private static final float RADIUS = .1f, POWER = 0.5f, BOOST = 1.15f;
  
  private boolean fxUpd; // When true we send a trigger for an effect to play on this object next frame
  
  private final Map<Player, Integer> jumping;
  
  public Jumper(final NoxioGame game, final int oid, final Vec2 position) {
    super(game, oid, position, 0);
    /* Bitmask Type */
    bitIs = bitIs | GameObject.Types.MAPOBJ;
    
    jumping = new HashMap();
    fxUpd = false;
  }
    
  @Override
  public void step() {
    Iterator<Player> iterator = jumping.keySet().iterator();
    while (iterator.hasNext()) {
        Player key = iterator.next();
        Integer time = jumping.get(key);
        if(time > 1) { 
          jumping.put(key, time-1);
        }
        else {
          jumping.remove(key);
          break; // I hate iterators. Removing this break crashes the lobby. Wish I could just use a list reeee
        }
    }
    
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      final Controller con = game.getControllerByObject(obj);
      if(con != null && obj.is(GameObject.Types.PLAYER) && !(obj instanceof Poly)) {
        final Player ply = (Player)obj;
        if(ply.position.distance(position) < ply.radius + RADIUS && ply.height < 0.5f && ply.height > -0.5f) {
          if(!jumping.containsKey(ply)) {
            ply.setVSpeed(POWER);
            ply.setVelocity(ply.velocity.scale(BOOST));
            jumping.put(ply, COOLDOWN);
            fx();
          }
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
  public String type() { return "jmpr"; }
}
