package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;

public abstract class Flag extends Pickup {
  protected final Vec2 base;
  protected boolean teamAttack, enemyAttack;  // Determines whether the flag can be hit by owning team or enemy team
    
  public Flag(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    bitIs = bitIs | Types.FLAG;
    
    /* Vars */
    base = position;
    
    /* Settings */
    radius = 0.1f; weight = 0.5f; friction = 0.725f; invulnerable = true; bounciness = 0.5f;
    teamAttack = false; enemyAttack = true;

    /* Timers */
  }
  
  @Override
  public void step() {
    super.step();
        
    if(!isHeld() && !onBase()) { resetCooldown++; }
    else { resetCooldown = 0; }
    if(resetCooldown >= RESET_COOLDOWN_TIME) { reset(); announceReset(); }
  }

  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<float height>;<float vspeed>;<int onBase>;<string[] effects>
     Note: onBase is an int where 1 is true, 0 is false.
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
  
  @Override
  public boolean canAttack(int atkrTeam) {
    return (atkrTeam == team && teamAttack) || (atkrTeam != team && enemyAttack);
  }
  
  @Override
  public boolean touch(Player p) {
    if(isHeld()) { return false; }
    if(p.team != team) { return pickup(p); }
    else { return flagReturn(p); }
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(super.pickup(p)) {
      setVelocity(new Vec2());
      setHeight(0f);
      setVSpeed(0f);
      return true;
    }
    return false;
  }
  
  public boolean flagReturn(Player p) {
    if(onBase()) { return false; }
    kill();
    return false;
  }
  
  public void score(final Player p) {
    game.reportObjective(game.getControllerByObject(p), this);
  }
  
  @Override
  public void kill() {
    reset();
    announceReturn();
  }
  
  @Override
  public void destroyx() { kill(); }
  
  protected void reset() {
    if(held != null) { held.drop(); }
    setPosition(base);
    setVelocity(new Vec2());
    setHeight(0f);
    setVSpeed(0f);
    dropCooldown = 0;
    resetCooldown = 0;
  }
  
  public boolean onBase() { return position.equals(base); }
  
  @Override
  public boolean isGlobal() { return true; }
  
  public abstract void announceTaken(int team);
  public abstract void announceReset();
  public abstract void announceReturn();
  
  @Override
  public String type() { return "flg"; }
}
