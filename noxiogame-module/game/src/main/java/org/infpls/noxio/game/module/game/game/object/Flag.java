package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;

public class Flag extends Mobile {
  private final Vec2 base;
  private Player held;               // Player holding this flag or null
  
  private final List<String> effects;      // List of actions performed that will be sent to the client on the next update
  
  private long lastHeld;
  private int dropCooldown, resetCooldown;
  private final static int DROP_COOLDOWN_TIME = 45, RESET_COOLDOWN_TIME = 900;
  public Flag(final NoxioGame game, final long oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.flag", position, true, 0.1f, 0.5f, 0.725f);
    
    base = position;
    held = null;
    setTeam(team);
    
    effects = new ArrayList();
    
    dropCooldown = 0;
    lastHeld = -1;
  }
  
  @Override
  public void step() {
    if(held==null) { physics(); }                                                                     // Object physics and collision
    else { setPosition(held.getPosition()); setVelocity(new Vec2()); setHeight(held.getHeight()); }   // Or not!
    
    if(isHeld()) {
      for(int i=0;i<game.objects.size();i++) {
        if(game.objects.get(i).getType().equals("obj.mobile.flag")) {
          final Flag f = (Flag)(game.objects.get(i));
          if(f.getTeam() != getTeam() && f.onBase() && f.getPosition().distance(position) < f.getRadius()+held.getRadius()) { f.reportObjective(held); kill(); }
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
    final Controller c = game.getControllerByObject(this);
    final String name;
    if(c != null) { name = c.getUser(); }
    else { name = ""; }
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    sb.append(getTeam()); sb.append(";");
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
    if(getTeam() == p.getTeam()) { return false; }
    held = p;
    lastHeld = held.getOid();
    return true;
  }
  
  public void reset(final Player p) {
    if(getTeam() == p.getTeam() && !isHeld()) {
      kill();
    }
  }
  
  public void drop() {
    if(isHeld()) { if(held.getHolding() == this) { held.drop(); } }
    dropCooldown = DROP_COOLDOWN_TIME;
    held = null;
  }
  
  public void reportObjective(final Player p) {
    game.reportObjective(game.getControllerByObject(p), this);
  }
  
  @Override
  public void knockback(final Vec2 impulse, final Player player) {
    if(getTeam() != player.getTeam()) {
      setVelocity(velocity.add(impulse));
    }
  }
  
  @Override
  public void kill() {
    drop();
    setPosition(base);
    setVelocity(new Vec2());
    setHeight(0f);
    setVSpeed(0f);
    dropCooldown = 0;
  }
  
  public boolean isHeld() { return held!=null; }
  public boolean onBase() { return position.equals(base); }
}
