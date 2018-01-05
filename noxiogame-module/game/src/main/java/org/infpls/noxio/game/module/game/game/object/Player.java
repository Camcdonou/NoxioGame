package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;
import org.infpls.noxio.game.module.game.util.Oak;

public abstract class Player extends Mobile {
  private static final int TAG_CREDIT_GRACE_PERIOD = 300;
  protected static final float VERTICAL_HIT_TEST_LENIENCY = 1.25f;
  protected static final float AIR_CONTROL = 0.2f, MULTI_JUMP_DAMPEN = 0.2f;
  
  protected boolean jumped;                  // Flags a jump so players can only jump once, either off the ground or mid air
  protected float moveSpeed, jumpHeight;
  
  protected Vec2 look;                       // Normalized direction player is facing
  protected float speed;                     // Current scalar of max movement speed <0.0 to 1.0>
  protected final List<String> action;       // Action to be performed on the next frame
  protected final List<String> effects;      // List of actions performed that will be sent to the client on the next update
  
  protected boolean ultimate;
  
  protected Flag holding;
  
  protected int channelTimer, tauntCooldown, stunTimer;
  public Player(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position);
    /* Bitmask Type */
    bitIs = bitIs | Types.PLAYER;
    
    /* Vars */
    look =  new Vec2(0.0f, 1.0f);
    speed = 0.0f;
    
    action = new ArrayList();
    effects = new ArrayList();

    holding = null;
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* State */
    this.ultimate = false;
    this.team = team;
    
    /* Timers */
    stunTimer = 0;
    channelTimer = 0;
    tauntCooldown =  0;
  }
  
  /* Sets player inputs. These will be processed on the next step() */
  public void setInput(final Vec2 dir, final float s) {
    if(stunTimer > 0) { speed = 0; return; }
    speed = s;
    if(!dir.isNaN()) { look = dir; }
  }
  
  /* Sets player action. This will be processed on the next step() */
  public void queueAction(final String a) {
    if(stunTimer > 0) { return; }
    action.add(a);
  }
  
  /* Applies player inputs to the object. */
  public void movement() {
    if(channelTimer > 0) { return; }
    if(grounded) { setVelocity(velocity.add(look.scale(moveSpeed*speed))); }
    else { setVelocity(velocity.add(look.scale(moveSpeed*speed).scale(AIR_CONTROL))); } // Reduced control while airborne
  }
  
  /* Performs action. */
  public final void actions() {
    if(grounded && jumped) { jumped = false; }
    for(int i=0;i<action.size()&&channelTimer<=0;i++) {
      switch(action.get(i)) {
        case "atk" : { actionA(); break; }
        case "mov" : { actionB(); break; }
        case "tnt" : { taunt(); break; }
        case "jmp" : { jump(); break; }
        default : { Oak.log("Invalid action input::"  + action.get(i) + " @Player.actions:::" + type(), 1); break; }
      }
    }
    action.clear();
  }
  
  
  
  /* Updates various timers */
  public void timers() {
    if(stunTimer > 0) { stunTimer--; }
    if(channelTimer > 0) { channelTimer--; }
    if(tauntCooldown > 0) { tauntCooldown--; }
  }
  
  @Override
  public void step() {
    movement();   // Apply player movement input
    physics();    // Object physics and collision
    actions();    // Perform action
    pickup();     // Picking up objects like flags
    timers();     // Updates timers and flags for various things
  }
  
  /* Pickup flag or whatever if you move over it */
  public void pickup() {
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      if(obj.is(Types.FLAG)) {
        final Flag flag = (Flag)(obj);
        if(holding==null && flag.getPosition().distance(position) < flag.getRadius()+getRadius()) {
          if(flag.pickup(this)) { holding = flag; }
        }
        if(flag.getPosition().distance(position) < flag.getRadius()+getRadius() && !flag.onBase()) {
          if(flag.team == team) { flag.kill(); }
        }
      }
    }
  }
  
  /* Do not call this?!?!?!? */
  public void drop() {
    if(holding==null) { return; }
    final Flag f = holding;
    holding = null;
    f.drop();
  }

  @Override
  public void post() {
    effects.clear();
  }
   
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<float height>;<float vspeed>;<vec2 look>;<float speed>;<string name>;<string[] effects>
  */
  public void generateUpdateData(final StringBuilder sb) {
    final Controller c = game.getControllerByObject(this);
    final String name = c!=null?c.getUser():"";
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    sb.append(team); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    look.toString(sb); sb.append(";");
    sb.append(speed); sb.append(";");
    sb.append(name); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  public abstract void actionA();
  public abstract void actionB();
  public abstract void taunt();
  
  public void jump() {
    if(jumped) { return; }
    if(grounded) {
      popup(jumpHeight);
    }
    else {
      setVSpeed(getVSpeed()*MULTI_JUMP_DAMPEN);
      popup(jumpHeight);
      effects.add("air");
    }
    jumped = true;
    effects.add("jmp");
  }
  
  /* Used for 'ultimate lifeform' gamemode. Simply sends an effect id to the client to flag this player as the ultimate lifeform. */
  public final void ultimate() {
    effects.add("ult");
    ultimate = true;
  }
  
  /* Test given circle at <vec2 p> w/ radius <float r> against other players and return hits */ 
  public List<Mobile> hitTest(final Vec2 p, final float r) {
    final List<Mobile> hits = new ArrayList();
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      if(obj != this && obj.is(Types.MOBILE)) {
        final Mobile mob = (Mobile)obj;
        final float cr = mob.getRadius() + r;
        if(p.distance(mob.getPosition()) <= cr && Math.abs(getHeight()-mob.getHeight()) <= cr*VERTICAL_HIT_TEST_LENIENCY) {
          hits.add(mob);
        }
      }
    }
    return hits;
  }
  
  /* Test given hitbox <Polygon poly> against other players and return hits */ 
  public List<Mobile> hitTest(final Polygon p) {
    final List<Mobile> hits = new ArrayList();
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      if(obj != this && obj.is(Types.MOBILE)) {
        final Mobile mob = (Mobile)obj;
        final float cr = mob.getRadius() + getRadius();
        if(!immune && Math.abs(getHeight()-mob.getHeight()) <= cr*VERTICAL_HIT_TEST_LENIENCY) {
          final boolean full = Intersection.pointInPolygon(mob.getPosition(), p);
          final Intersection.Instance inst = Intersection.polygonCircle(mob.getPosition(), p, mob.getRadius());
          if(full || inst != null) {
            hits.add(mob);
          }
        }
      }
    }
    return hits;
  }
  
  @Override
  public boolean isGlobal() { return ultimate; }
  
  @Override
  public void stun(int time) {
    stunTimer = time;
    effects.add("stn");
  }
  
  @Override
  public void kill() {
    dead = true;
    drop();
    game.reportKill(tagTime-game.getFrame()<=TAG_CREDIT_GRACE_PERIOD?tagged:null, this);
  }
  
  public Flag getHolding() { return holding; }
}
