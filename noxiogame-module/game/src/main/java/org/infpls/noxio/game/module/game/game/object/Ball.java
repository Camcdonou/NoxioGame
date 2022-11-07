package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Ball extends Pickup {
  private static final float HOLD_OFFSET = 0.475f, HOLD_STRENGTH = 0.0825f, HOLD_DAMPEN = 0.65f, HOLD_DIST_MIN = 0.05f, HOLD_DIST_MAX = 0.25f, FUMBLE_DISTANCE = 0.875f, PICKUP_RADIUS_OVERRIDE = 0.1f;
  private static final float HOVER_HEIGHT = 0.25f, VELOCITY_CAP = 0.55f;
  private static final int OOB_TIMER_MAX = 75, SCORE_TIMER_MAX = 55, PASS_TIMER_MAX = 45, BOOST_TIMER_MAX = 90;
  private static final float MIN_PASS_DISTANCE = 2f, TOSS_FORCE_BASE_MULT = 1.05f, PASS_BOOST = 1.6f;
  
  private int oobTimer, scoreTimer, passTimer, boostTimer;
  private float passDistance;
  
  protected final Vec2 base;
  protected final Polygon field;
  protected boolean teamAttack, enemyAttack;  // Determines whether the ball can be hit by owning team or enemy team
  
  private Controller responsible; // Last player to touch the ball. Gets credit for scoring
    
  public Ball(final NoxioGame game, final int oid, final Vec2 position, final int team, final Polygon field) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    //bitIs = bitIs | GameObject.Types.FLAG;
    
    /* Vars */
    base = position;
    responsible = null;
    this.field = field;
    passDistance = 0f;
    
    /* Settings */
    radius = 0.325f; weight = 0.5f; friction = 0.95f; invulnerable = true; bounciness = 1.0f;
    teamAttack = true; enemyAttack = true;

    /* Timers */
    oobTimer = 0;
    scoreTimer = 0;
    boostTimer = 0;
    passTimer = 0;
  }
  
  @Override
  public void step() {
    final Vec2 lastPos = position;
    
    super.step();
    
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
    
    /* Score timer -- When the ball enters a goal there is a short period before it resets. During this period is is disabled and force dropped */
    if(scoreTimer > 0) {
      if(--scoreTimer == 0) { reset(); }
      if(held != null) { held.drop(); }
    }
    
    if(passTimer > 0) { passTimer--; }
    if(boostTimer > 0) { boostTimer--; }
    passDistance += lastPos.distance(position);
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
    if(!inPlay()) { return false; }
    if(p.position.distance(position) > p.getRadius() + PICKUP_RADIUS_OVERRIDE) { return false; }
    return pickup(p);
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(super.pickup(p)) {
      if(responsible != null && responsible.getTeam() == p.team && responsible.getControlled() != p && passTimer > 0 && passDistance > MIN_PASS_DISTANCE) {
        boostTimer = BOOST_TIMER_MAX;
        held.effects.add("pbst");
      }
      responsible = game.getControllerByObject(p);
      return true;
    }
    return false;
  }

  public void scored() {
    scoreTimer = SCORE_TIMER_MAX;
    effects.add("bals");
  }
  
  @Override
  public void bounced() {
    dropCooldown = 0;
  }
  
  @Override
  public void dropped() {
    boostTimer = 0;
    responsible = game.getControllerByObject(held);
    held.forceMovementCooldown();
    super.dropped();
  }
  
  @Override
  public void tossed() {
    passTimer = PASS_TIMER_MAX;
    passDistance = 0f;
    
    setVelocity(held.velocity.scale(0.5f));
    
    float force = Player.TOSS_IMPULSE * TOSS_FORCE_BASE_MULT;
    if(isBoosted()) { held.effects.add("tbst"); force *= PASS_BOOST; }
    
    setVelocity(held.velocity.scale(0.5f).add(held.look.scale(force)));
    popup(Player.TOSS_POPUP);
    
    dropped();
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
    scoreTimer = 0;
    oobTimer = 0;
    effects.add("balr");
  }
  
  public boolean onBase() { return position.equals(base); }
  
  public boolean inPlay() { return scoreTimer == 0; }
  public boolean isBoosted() { return boostTimer > 0; }
  
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
