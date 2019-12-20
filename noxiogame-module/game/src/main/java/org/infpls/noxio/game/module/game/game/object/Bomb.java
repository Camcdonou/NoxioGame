package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Bomb extends Pickup {
  private static final int DETONATION_TIME = 150, DETONATION_STUN = 90;
  private static final float DETONATION_ZONE_DISTANCE = 1.5f, DETONATION_RADIUS = 5.5f, DETONATION_FORCE = 0.9f, DETONATION_POPUP = 0.5f;

  
  protected final Vec2 base;
  private Player lastHeldPlayer;
  
  private boolean armed, detonated;
  private int detonationTimer;
    
  public Bomb(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    bitIs = bitIs | Types.BOMB;
    
    /* Vars */
    base = position;
    
    /* Settings */
    radius = 0.25f; weight = 0.5f; friction = 0.725f;

    /* Timers */
    armed = false; detonated = false;
    detonationTimer = 0;
  }
  
  @Override
  public void step() {
    super.step();
    
    if(detonated) {
      kill();
    }
    if(armed && detonationTimer-- <= 0) {
      detonate();
    }
    
    if(!isHeld() && !onBase()) { resetCooldown++; }
    else { resetCooldown = 0; }
    if(resetCooldown >= RESET_COOLDOWN_TIME) { kill(); }
  }

  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<float height>;<float vspeed>;<int onBase>;<int detTimer>;<string[] effects>
     Note: onBase is an int where 1 is true, 0 is false.
     Note: detTimer is an int+bool where -1 means its not armed and any positive number is the number of frames till detonation
  */
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    sb.append(onBase()?1:0); sb.append(";");
    sb.append(armed?detonationTimer:-1); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  @Override
  public boolean touch(Player p) {
    if(isHeld()) { return false; }
    if(p.team == team) { return pickup(p); }
    else { return bombReturn(p); }
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(super.pickup(p)) {
      lastHeldPlayer = p;
      return true;
    }
    return false;
  }
  
  public boolean bombReturn(Player p) {
    if(onBase()) { return false; }
    kill();
    return false;
  }
  
  public void arm() {
    if(armed) { return; }
    
    armed = true;
    detonationTimer = DETONATION_TIME;
    effects.add("arm");
  }
  
  private void detonate() {
    armed = false; detonated = true;
    effects.add("det");
    
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      
      if(obj.is(GameObject.Types.ZONE)) {
        final BombZone zone = (BombZone)obj;
        final boolean inside = Intersection.pointInPolygon(getPosition(), zone.hitbox);
        final Intersection.Instance inst = Intersection.polygonCircle(getPosition(), zone.hitbox, DETONATION_ZONE_DISTANCE);
        if(inside || inst != null) {
          score();
        }
      }
      
      if(obj.is(GameObject.Types.MOBILE) && obj != this) {
        final Mobile mob = (Mobile)obj;
        if(getPosition().distance(mob.getPosition()) <= DETONATION_RADIUS+mob.getRadius()) {
          final Vec2 normal = mob.getPosition().subtract(getPosition()).normalize();
          mob.stun(DETONATION_STUN, Mobile.HitStun.Fire, 0);
          mob.knockback(normal.scale(DETONATION_FORCE));
          mob.popup(DETONATION_POPUP);
        }
      }
    }
  }
  
  private void score() {
    game.reportObjective(game.getControllerByObject(lastHeldPlayer), this);
    destroyx();
  }
  
  @Override
  public void kill() {
    reset();
    game.announce(team==0?"rfr":"bfr");
  }
  
  protected void reset() {
    if(held != null) { held.drop(); }
    setPosition(base);
    setVelocity(new Vec2());
    setHeight(0f);
    setVSpeed(0f);
    dropCooldown = 0;
    resetCooldown = 0;
    armed = false; detonated = false;
    detonationTimer = 0;
  }
  
  public boolean onBase() { return position.equals(base); }
  
  @Override
  public boolean isGlobal() { return true; }
  
  @Override
  public String type() { return "bmb"; }
}
