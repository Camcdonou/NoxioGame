package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Ball extends Pickup {
  private static final float HOLD_OFFSET = 0.475f, HOLD_STRENGTH = 0.0825f, HOLD_DAMPEN = 0.65f, HOLD_DIST_MIN = 0.05f, HOLD_DIST_MAX = 0.25f, FUMBLE_DISTANCE = 0.875f, PICKUP_RADIUS_OVERRIDE = 0.1f;
  private static final float HOVER_HEIGHT = 0.25f, VELOCITY_CAP = 0.55f;
  private static final int OOB_TIMER_MAX = 60;
  
  private int oobTimer;
  
  protected final Vec2 base;
  protected final Polygon field;
  protected boolean teamAttack, enemyAttack;  // Determines whether the flag can be hit by owning team or enemy team
  
  private Controller responsible; // Last player to touch the ball. Gets credit for scoring
    
  public Ball(final NoxioGame game, final int oid, final Vec2 position, final int team, final Polygon field) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    //bitIs = bitIs | GameObject.Types.FLAG;
    
    /* Vars */
    base = position;
    responsible = null;
    this.field = field;
    
    /* Settings */
    radius = 0.35f; weight = 0.5f; friction = 0.95f; invulnerable = true; bounciness = 1.0f;
    teamAttack = true; enemyAttack = true;

    /* Timers */
    oobTimer = 0;
  }
  
  @Override
  public void step() {
    super.step();
    
    /* Drop cooldown override */
    
    /* Fake gravity */
    if(held == null) {
      setHeight((height + height + HOVER_HEIGHT) / 3f);
      setVSpeed(getVSpeed() * .5f);
    }
    
    /* Velocity cap */
    if(velocity.magnitude() > VELOCITY_CAP) {
      velocity = velocity.normalize().scale(VELOCITY_CAP);
    }
    
    /* Out of bounds */
    if(!Intersection.pointInPolygon(position, field)) { oobTimer++; }
    else { oobTimer = 0; }
    
    if(oobTimer > OOB_TIMER_MAX) {
      kill();
      oobTimer = 0;
    }
  }
  
  @Override
  /* What to do on a step if this pickup is being held */
  protected void stepHeld() {
    immune = true;
    
    if(getThree().distance(held.getThree()) > FUMBLE_DISTANCE || held.stunTimer > 0) {
      held.drop();
    }
    else {
      final Vec2 heldPos = held.position.add(held.look.scale(HOLD_OFFSET));
      
      float holdDist = position.distance(heldPos);
      float strength = HOLD_STRENGTH * Math.max(0f, Math.min(1f, holdDist / HOLD_DIST_MAX));  // Incorrect. doesn't account for min
      Vec2 dir = heldPos.subtract(position).normalize();
      velocity = velocity.scale(HOLD_DAMPEN);
      velocity = velocity.add(dir.scale(strength));
            
      physics();
      
      setHeight(held.height + HOVER_HEIGHT);
      setVSpeed(0f);
    }
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
    if(p.position.distance(position) > p.getRadius() + PICKUP_RADIUS_OVERRIDE) { return false; }
    return pickup(p);
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(super.pickup(p)) {
      responsible = game.getControllerByObject(p);
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
  public void bounced() {
    dropCooldown = 0;
  }
  
  @Override
  public void dropped() {
    responsible = game.getControllerByObject(held);
    held.forceMovementCooldown();
    super.dropped();
  }
  
  @Override
  public void kill() {
    reset();
    announceReset();
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
    responsible = null;
  }
  
  public boolean onBase() { return position.equals(base); }
  
  public Controller getResponsible() {
    return responsible;
  }
  
  @Override
  public boolean isGlobal() { return true; }
  
  public void announceReset() {
    game.announce("brs");
  }
  
  @Override
  public String type() { return "bal"; }
}
