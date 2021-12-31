package org.infpls.noxio.game.module.game.game.object;

import org.infpls.noxio.game.module.game.game.NoxioGame;

public abstract class Pickup extends Mobile {
  protected Player held;               // Player holding this flag or null
  
  protected int lastHeld;
  protected int dropCooldown, resetCooldown;
  protected final static int DROP_COOLDOWN_TIME = 45, RESET_COOLDOWN_TIME = 900;
  private final static float KNOCKBACK_REDUCTION_MULT = 0.4f;
  
  public Pickup(final NoxioGame game, final int oid, final Vec2 position, final int permutation, final int team) {
    super(game, oid, position, permutation, team);
    /* Bitmask Type */
    bitIs = bitIs | Types.PICKUP;
    
    /* Vars */
    held = null;
    
    /* Settings */
    radius = 0.25f; weight = 0.5f; friction = 0.725f;
    
    /* State */
    intangible = true;
    immune = false;
    
    /* Timers */
    dropCooldown = 0; lastHeld = -1;
  }
  
  @Override
  public void step() {
    if(dropCooldown > 0) { dropCooldown--; }
    if(held != null) { stepHeld(); return; }
    
    immune = false; physics();
  }
  
  /* What to do on a step if this pickup is being held */
  protected void stepHeld() {
    immune = true;
    setPosition(held.getPosition());
    setVelocity(new Vec2());
    setHeight(held.getHeight());
  }
  
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<float height>;<float vspeed>;<string[] effects>
  */
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  /* Called when a player touches this pickup */
  public boolean touch(Player p) {
    if(isHeld()) { return false; }
    return pickup(p);
  }
  
  protected boolean pickup(Player p) {
    if(p.getHolding() != null) { return false; }
    if(dropCooldown > 0 && lastHeld == p.getOid()) { return false; }
    held = p;
    lastHeld = held.getOid();
    return true;
  }
  
  public void dropped() {
    dropCooldown = DROP_COOLDOWN_TIME;
    held.holding = null;
    held = null;
  }
  
  @Override
  public void kill() {
    if(held != null) { held.drop(); }
    dead = true;
  }
  
  @Override
  public void destroyx() {
    kill();
    destroyed = true;
  }
  
  public boolean isHeld() { return held!=null; }
  public Player getHeld() { return held; }
  
  @Override
  public void knockback(final Vec2 impulse, final Player p) { super.knockback(impulse.scale(KNOCKBACK_REDUCTION_MULT), p); }
  
  @Override
  public void popup(final float power, final Player p) { super.popup(power*KNOCKBACK_REDUCTION_MULT, p); }
}
