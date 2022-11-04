package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;

public class Bumper extends GameObject {
  private static final int COOLDOWN = 15, STUN = 27;
  private static final float RADIUS = .45f, POWER = 0.975f, INIT_VELOCITY_SCALE = 0.35f;
  
  private boolean fxUpd; // When true we send a trigger for an effect to play on this object next frame
  
  private final Map<Player, Integer> bumping;
  
  public Bumper(final NoxioGame game, final int oid, final Vec2 position) {
    super(game, oid, position, 0);
    /* Bitmask Type */
    bitIs = bitIs | GameObject.Types.MAPOBJ;
    
    bumping = new HashMap();
    fxUpd = false;
  }
    
  @Override
  public void step() {
    Iterator<Player> iterator = bumping.keySet().iterator();
    while (iterator.hasNext()) {
        Player key = iterator.next();
        Integer time = bumping.get(key);
        if(time > 1) { 
          bumping.put(key, time-1);
        }
        else {
          bumping.remove(key);
          break; // I hate iterators. Removing this break crashes the lobby. Wish I could just use a list reeee
        }
    }
    
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      final Controller con = game.getControllerByObject(obj);
      if(con != null && obj.is(GameObject.Types.PLAYER) && !(obj instanceof Poly)) {
        final Player ply = (Player)obj;
        if(ply.position.distance(position) < ply.radius + RADIUS && ply.height < 1.15f && ply.height > -0.5f) {
          if(!bumping.containsKey(ply)) {
            Vec2 init = ply.velocity.scale(INIT_VELOCITY_SCALE);
            Vec2 dir = ply.position.subtract(position).normalize();
            Vec2 force = dir.scale(POWER).add(init);
            bumping.put(ply, COOLDOWN);
            ply.knockback(force);
            ply.stun(STUN, Mobile.HitStun.ElectricBlack, 0, Mobile.CameraShake.MEDIUM);
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
  public String type() { return "bmpr"; }
}
