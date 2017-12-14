package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;

public class Bomb extends Mobile {
  private static final float DETONATION_OUTER_RADIUS = 0.65f, DETONATION_IMPULSE = 0.65f;
  private static final int DETONATION_STUN_LENGTH = 30;
  
  private Player owner;                // Player who threw this bomb
  
  private final List<String> effects;  // List of actions performed that will be sent to the client on the next update

  private int detonationTimer;
  public Bomb(final NoxioGame game, final int oid, final Vec2 position, final int team, final int timer, final Player owner) {
    super(game, oid, "obj.mobile.bomb", position);
    
    this.owner = owner;
    
    effects = new ArrayList();
    
    /* Settings */
    radius = 0.1f; weight = 0.1f; friction = 0.725f;
    
    /* State */
    intangible = true;
    this.team = team;
    
    /* Timers */
    detonationTimer = timer;
  }
  
  @Override
  public void step() {
    physics();
    if(detonationTimer > 0) { detonationTimer--; }
    else { detonate(); }
  }
  
  @Override
  public void post() {
    effects.clear();
  }
   
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<float height>;<float vspeed>;<vec2 look>;<float speed>;<string name>;<int onBase>;<string[] effects>
     Note: onBase is an int where 1 is true, 0 is false. Booleans suck for networking with javscript
  */
  public void generateUpdateData(final StringBuilder sb) {
    final Controller c = game.getControllerByObject(this);
    final String name;
    if(c != null) { name = c.getUser(); }
    else { name = ""; }
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    sb.append(team); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  public void score(final Player p) {
    game.reportObjective(game.getControllerByObject(p), this);
  }
  
  @Override
  public void knockback(final Vec2 impulse, final Player player) {
    this.owner = player;
    super.knockback(impulse, player);
  }
  
  private void detonate() {
    if(getHeight() > -0.5) {
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        if(obj != this && obj.getType().startsWith("obj.mobile") && !obj.getType().equals("obj.mobile.bomb")) {
          final Mobile mob = (Mobile)obj;
          if(!mob.isIntangible() && mob.getPosition().distance(position) < mob.getRadius() + getRadius() + DETONATION_OUTER_RADIUS && mob.getHeight() > -0.5) {
            if(obj.getType().startsWith("obj.mobile.player")) {
              final Player ply = (Player)obj;
              ply.stun(DETONATION_STUN_LENGTH);
            }
            final Vec2 normal = mob.getPosition().subtract(position).normalize();
            mob.knockback(normal.scale(DETONATION_IMPULSE), this.owner);
            final Controller c = game.getControllerByObject(this);
            if(c != null) { mob.tag(c); }
          }
        }
      }
    }
    kill();
  }
  
  @Override
  public void kill() {
    dead = true;
  }
}
