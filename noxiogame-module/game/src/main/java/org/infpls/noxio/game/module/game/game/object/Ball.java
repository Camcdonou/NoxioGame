package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;

public class Ball extends Pickup {
  protected final Vec2 base;
  protected boolean teamAttack, enemyAttack;  // Determines whether the flag can be hit by owning team or enemy team
    
  public Ball(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    //bitIs = bitIs | GameObject.Types.FLAG;
    
    /* Vars */
    base = position;
    
    /* Settings */
    radius = 0.1f; weight = 0.5f; friction = 0.95f; invulnerable = true;
    teamAttack = true; enemyAttack = true;

    /* Timers */
  }
  
  @Override
  public void step() {
    super.step();
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
  
  public boolean canAttack(int atkrTeam) {
    return (atkrTeam == team && teamAttack) || (atkrTeam != team && enemyAttack);
  }
  
  @Override
  public boolean touch(Player p) {
    if(isHeld()) { return false; }
    return pickup(p);
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(super.pickup(p)) {
      if(p.team != team) { announceTaken(team); }
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
  
  public void announceTaken(int team) {
    ((TeamGame)game).announceTeam(team, "fs");
    ((TeamGame)game).announceTeam(team==0?1:0, "ft");
  }
  
  public void announceReset() {
    ((TeamGame)game).announceTeam(team, "ff");
  }
  
  public void announceReturn() {
    ((TeamGame)game).announceTeam(team, "ff");
  }
  
  @Override
  public String type() { return "bal"; }
}
