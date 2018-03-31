package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;

public class Flag extends Mobile {
  private final Vec2 base;
  private Player held;               // Player holding this flag or null
  
  private final List<String> effects;      // List of actions performed that will be sent to the client on the next update
  
  private int lastHeld;
  private int dropCooldown, resetCooldown;
  private final static int DROP_COOLDOWN_TIME = 45, RESET_COOLDOWN_TIME = 900;
  private final static float KNOCKBACK_REDUCTION_MULT = 0.4f;
  public Flag(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    bitIs = bitIs | Types.FLAG;
    
    /* Vars */
    effects = new ArrayList();
    
    base = position;
    held = null;
    
    /* Settings */
    radius = 0.1f; weight = 0.5f; friction = 0.725f;
    
    /* State */
    intangible = true;
    immune = false;
    
    /* Timers */
    dropCooldown = 0; lastHeld = -1;
  }
  
  @Override
  public void step() {
    if(held==null) { immune = false; physics(); }                                                                     // Object physics and collision
    else { immune = true; setPosition(held.getPosition()); setVelocity(new Vec2()); setHeight(held.getHeight()); }   // Or not!
    
    if(isHeld()) {
      for(int i=0;i<game.objects.size();i++) {
        final GameObject obj = game.objects.get(i);
        if(obj.is(Types.FLAG)) {
          final Flag f = (Flag)(obj);
          if(f.team != team && f.onBase() && f.getPosition().distance(position) < f.getRadius()+held.getRadius()) { f.score(held); reset(); }
        }
      }
    }
    
    if(!isHeld() && !onBase()) { resetCooldown++; }
    else { resetCooldown = 0; }
    
    if(dropCooldown > 0) { dropCooldown--; }
    if(resetCooldown >= RESET_COOLDOWN_TIME) { kill(); }
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
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    sb.append(onBase()?1:0); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  public boolean pickup(final Player p) {
    if(dropCooldown > 0 && lastHeld == p.getOid()) { return false; }
    if(isHeld()) { return false; }
    if(team == p.team) { return false; }
    if(onBase()) { game.announce(team==0?"rft":"bft"); }
    held = p;
    lastHeld = held.getOid();
    return true;
  }
  
  public void drop() {
    if(isHeld()) { if(held.getHolding() == this) { held.drop(); } }
    dropCooldown = DROP_COOLDOWN_TIME;
    held = null;
  }
  
  public void score(final Player p) {
    game.reportObjective(game.getControllerByObject(p), this);
  }
  
  private void reset() {
    drop();
    setPosition(base);
    setVelocity(new Vec2());
    setHeight(0f);
    setVSpeed(0f);
    dropCooldown = 0;
  }
  
  @Override
  public void kill() {
    reset();
    game.announce(team==0?"rfr":"bfr");
  }
  
  @Override
  public void knockback(final Vec2 impulse, final Player p) { if(p.team != team) { super.knockback(impulse.scale(KNOCKBACK_REDUCTION_MULT), p); } }
  @Override
  public void stun(final int time, final Player p) { if(p.team != team) { super.stun(time, p); } }
  @Override
  public void popup(final float power, final Player p) { if(p.team != team) { super.popup(power*KNOCKBACK_REDUCTION_MULT, p); } }
  
  public boolean isHeld() { return held!=null; }
  public boolean onBase() { return position.equals(base); }
  
  @Override
  public boolean isGlobal() { return true; }
  
  @Override
  public String type() { return "flg"; }
}
