package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;

public class Player extends Mobile {
  private static final float MAX_SPEED = 0.025f; // Max movement speed
  private static final int BLIP_COOLDOWN_LENGTH = 10, DASH_COOLDOWN_LENGTH = 15, TAUNT_COOLDOWN_LENGTH = 30, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 30, DASH_POWER_MAX = 60, DASH_POWER_ADD = 30, DASH_STUN_TIME = 30;
  private static final float BLIP_IMPULSE = 0.85f, DASH_IMPULSE = 0.25f, BLIP_OUTER_RADIUS = 0.1f;
  
  private Vec2 look;                       // Normalized direction player is facing
  private float speed;                     // Current scalar of max movement speed <0.0 to 1.0>
  private String action;                   // Action to be performed on the next frame
  private final List<String> effects;      // List of actions performed that will be sent to the client on the next update
  
  private int blipCooldown, dashCooldown, tauntCooldown, blipPower, dashPower;
  private int stunTimer;
  private int spawnProtection;
  public Player(final NoxioGame game, final long oid, final Vec2 position) {
    super(game, oid, "obj.mobile.player", position, 0.5f, 1.0f, 0.88f);
    
    look =  new Vec2(0.0f, 1.0f);
    speed = 0.0f;
    
    action = null;
    effects = new ArrayList();
    
    blipCooldown = 0; dashCooldown = 0; tauntCooldown = 0;
    blipPower = BLIP_POWER_MAX; dashPower = 0;
    
    stunTimer = 0;
    spawnProtection = 66;
  }
  
  /* Sets player inputs. These will be processed on the next step() */
  public void setInput(final Vec2 dir, final float s) {
    if(stunTimer > 0) { speed = 0; return; }
    speed = s;
    if(!dir.isNaN()) { look = dir; }
  }
  
  /* Sets player action. This will be processed on the next step() */
  public void setAction(final String action) {
    if(stunTimer > 0) { return; }
    this.action = action;
  }
  
  /* Applies player inputs to the object. */
  public void movement() {
    setVelocity(velocity.add(look.scale(MAX_SPEED*speed)));
  }
  
  /* Performs action. */
  public void actions() {
    if(action == null) { return; }
    switch(action) {
      case "blip" : { blip(); break; }
      case "dash" : { dash(); break; }
      case "taunt" : { taunt(); break; }
      default : { break; }
    }
    action = null;
  }
  
  @Override
  public void step() {
    movement();   // Apply player movement input
    physics();    // Object physics and collision
    actions();    // Perform action
    
    /* Timers */
    if(blipCooldown > 0) { blipCooldown--; }
    if(dashCooldown > 0) { dashCooldown--; }
    if(tauntCooldown > 0) { tauntCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
    if(dashPower > 0) { dashPower--; }
    if(stunTimer > 0) { stunTimer--; }
    if(spawnProtection > 0) { spawnProtection--; }
  }
  
  @Override
  public void post() {
    effects.clear();
  }
   
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<vec2 look>;<float speed>;<string[] effects>
  */
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    look.toString(sb); sb.append(";");
    sb.append(speed); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  public void blip() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        if(obj != this && obj.getType().startsWith("obj.mobile")) {
          Mobile mob = (Mobile)obj;
          if(mob.getPosition().distance(position) < mob.getRadius() + getRadius() + BLIP_OUTER_RADIUS) {
            if(obj.getType().startsWith("obj.mobile.player")) {
              Player ply = (Player)obj;
              ply.stun(BLIP_STUN_TIME*(blipPower/BLIP_POWER_MAX));
            }
            final Vec2 normal = mob.getPosition().subtract(position).normalize();
            mob.setVelocity(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)));
          }
        }
      }
      blipPower = 0;
      effects.add("blip");
    }
  }
  
  public void dash() {
    if(dashCooldown <= 0 && dashPower < DASH_POWER_MAX) {
      dashCooldown = DASH_COOLDOWN_LENGTH;
      dashPower += DASH_POWER_ADD;
      setVelocity(velocity.add(look.scale(DASH_IMPULSE)));
      effects.add("dash");
      if(dashPower >= DASH_POWER_MAX) { stun(DASH_STUN_TIME); }
    }
  }
  
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("taunt");
    }
  }
  
  public void stun(int time) {
    stunTimer = time;
    effects.add("stun");
  }
  
  @Override
  public void kill(GameObject killer) {
    if(spawnProtection < 1) { dead = true; }
  }
  
  @Override
  public void kill() { dead = true; }
}
