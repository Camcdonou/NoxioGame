package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;
import org.infpls.noxio.game.module.game.util.Oak;

public class Marth extends Player {
  private static final int SLASH_COOLDOWN_LENGTH = 20, SLASH_COMBO_LENGTH = 3, SLASH_COMBO_DEGEN = 90, SLASH_STUN_LENGTH = 15, SLASH_COMBO_STUN_LENGTH = 25;
  private static final float SLASH_RANGE = 1.0f, SLASH_ANGLE = 120f, SLASH_SEGMENT_DISTANCE=5f, SLASH_IMPULSE = 0.45f, SLASH_COMBO_IMPULSE = 1.05f;
  private static final int COUNTER_COOLDOWN_LENGTH = 45, COUNTER_ACTIVE_LENGTH = 7, COUNTER_LAG_LENGTH = 30;
  private static final float COUNTER_MULTIPLIER = 1.33f, COUNTER_ANGLE = 45f, COUNTER_SEGMENT_DISTANCE=5f, COUNTER_RANGE = 1.25f;
  private static final int TAUNT_COOLDOWN_LENGTH = 60;
  
  private boolean counter, counterHit;
  private float counterKnock, counterPop;
  private int counterStun;
  private Vec2 counterDirection;
  private int combo;
  private int slashCooldown, counterCooldown, counterTimer, comboTimer, tauntCooldown;
  public Marth(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Marth(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.player.marth", position, team);
    
    /* Settings */
    radius = 0.5f; weight = 0.9f; friction = 0.705f;
    moveSpeed = 0.0355f; jumpHeight = 0.175f;
    
    /* Timers */
    combo = 0;
    slashCooldown = 0;
    counterCooldown = 0;
    counterTimer = 0;
    comboTimer = 0;
    tauntCooldown = 0;
    counterKnock = 0f;
    counterPop = 0f;
    counterStun = 0;
    counterDirection = null;
  }
  
  /* Applies player inputs to the object. */
  @Override
  public void movement() {
    if(counter) { return; }    // Can't act during counter (overriding defaults here)
    if(isGrounded()) { setVelocity(velocity.add(look.scale(moveSpeed*speed))); }
    else { setVelocity(velocity.add(look.scale(moveSpeed*speed).scale(AIR_CONTROL))); } // Reduced control while airborne
  }
  
  /* Performs action. */
  @Override
  public void actions() {
    if(counter) { return; }    // Can't act during counter
    for(int i=0;i<action.size();i++) {
      switch(action.get(i)) {
        case "atk" : { slash(); break; }
        case "mov" : { counter(); break; }
        case "tnt" : { taunt(); break; }
        case "jmp" : { jump(); break; }
        default : { Oak.log("Invalid action input::"  + action.get(i) + " @Marth.actions", 1); break; }
      }
    }
    action.clear();
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    if(slashCooldown > 0) { slashCooldown--; }
    if(counterCooldown > 0) { counterCooldown--; }
    if(comboTimer > 0) { comboTimer--; }
    else if(comboTimer < 1 && combo > 0) { combo--; comboTimer = combo>0?SLASH_COMBO_DEGEN:0; }
    if(counterHit) { riposte(); }
    if(counterTimer > 0) { counterTimer--; }
    else if(counterTimer < 1 && counter) { counter = false; }
    if(tauntCooldown > 0) { tauntCooldown--; }
    if(stunTimer > 0) { stunTimer--; }
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
    if(counterDirection!=null) { counterDirection.toString(sb); }
    else { sb.append("1,0"); } sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  /* Generatese a polygon to use as the hitbox for the slash then tests objects against it to see if it hit */
  public void slash() {
    if(slashCooldown <= 0) {
      slashCooldown = SLASH_COOLDOWN_LENGTH;
      final boolean isCombo = combo >= SLASH_COMBO_LENGTH;
      effects.add("atk");
      if(isCombo) { combo = 0; comboTimer = 0; effects.add("cmb"); }
      if(getHeight() > -0.5) {
        final float rad = (float)(Math.PI/180);
        final Polygon hitbox;
        final List<Vec2> verts = new ArrayList();
        final Vec2 start = look.scale(SLASH_RANGE).rotate(SLASH_ANGLE*rad*-0.5f);
        verts.add(position);
        for(float d=0f;d<SLASH_ANGLE*rad;d+=SLASH_SEGMENT_DISTANCE*rad) {
          verts.add(start.rotate(d).add(position));
        }
        hitbox = new Polygon(verts.toArray(new Vec2[0]));
        for(int i=0;i<game.objects.size();i++) {
          if(game.objects.get(i).getType().startsWith("obj.mobile")&&game.objects.get(i)!=this) {
            Mobile mob = (Mobile)game.objects.get(i);
            final Intersection.Instance inst = Intersection.polygonCircle(mob.getPosition(), hitbox, mob.getRadius());
            if(inst != null) {
              if(mob.getType().startsWith("obj.mobile.player")) {
                final Player ply = (Player)mob;
                ply.stun(isCombo?SLASH_COMBO_STUN_LENGTH:SLASH_STUN_LENGTH);
                combo++; comboTimer = SLASH_COMBO_DEGEN;
              }
              final Vec2 normal = mob.getPosition().subtract(position).normalize();
              mob.knockback(normal.scale(isCombo?SLASH_COMBO_IMPULSE:SLASH_IMPULSE), this);
              final Controller c = game.getControllerByObject(this);
              if(c != null) { mob.tag(c); }
            }
          }
        }
      }
      if(combo > SLASH_COMBO_LENGTH) { combo = SLASH_COMBO_LENGTH; }
      if(combo == SLASH_COMBO_LENGTH) { effects.add("rdy"); }
    }
  }
  
  public void counter() {
    if(counterCooldown <= 0) {
      counterCooldown = COUNTER_COOLDOWN_LENGTH;
      counterTimer = COUNTER_LAG_LENGTH;
      counterDirection = null;
      counter = true;
      counterHit = false;
      effects.add("cnt");
    }
  }
  
  public void riposte() {
    effects.add("rip");
    if(getHeight() > -0.5) {
      final float rad = (float)(Math.PI/180);
      final Vec2 dir = counterDirection!=null?counterDirection:look;
      final Polygon hitbox;
      final List<Vec2> verts = new ArrayList();
      final Vec2 start = dir.normalize().scale(COUNTER_RANGE).rotate(COUNTER_ANGLE*rad*-0.5f);
      verts.add(position);
      for(float d=0f;d<COUNTER_ANGLE*rad;d+=COUNTER_SEGMENT_DISTANCE*rad) {
        verts.add(start.rotate(d).add(position));
      }
      hitbox = new Polygon(verts.toArray(new Vec2[0]));
      for(int i=0;i<game.objects.size();i++) {
        if(game.objects.get(i).getType().startsWith("obj.mobile")&&game.objects.get(i)!=this) {
          Mobile mob = (Mobile)game.objects.get(i);
          final Intersection.Instance inst = Intersection.polygonCircle(mob.getPosition(), hitbox, mob.getRadius());
          if(inst != null) {
            if(mob.getType().startsWith("obj.mobile.player")) {
              final Player ply = (Player)mob;
              if(counterStun > 0) { ply.stun((int)(counterStun * COUNTER_MULTIPLIER)); }
              combo++; comboTimer = SLASH_COMBO_DEGEN;
            }
            final Vec2 normal = mob.getPosition().subtract(position).normalize();
            if(counterKnock > 0) { mob.knockback(normal.scale(counterKnock * COUNTER_MULTIPLIER), this); }
            if(counterPop > 0) { mob.popup(counterPop * COUNTER_MULTIPLIER); }
            final Controller c = game.getControllerByObject(this);
            if(c != null) { mob.tag(c); }
          }
        }
      }
    }
    if(combo > SLASH_COMBO_LENGTH) { combo = SLASH_COMBO_LENGTH; }
    if(combo == SLASH_COMBO_LENGTH) { effects.add("rdy"); }
    
    counterHit = false;
    counter = false;
    counterTimer = 0;
    counterCooldown = 5;
    counterKnock = 0f;
    counterPop = 0f;
    counterStun = 0;
  }
  
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
  
  @Override
  public void knockback(final Vec2 impulse, final Player player) {
    if(counter && COUNTER_LAG_LENGTH-counterTimer < COUNTER_ACTIVE_LENGTH) { counterHit = true; counterKnock = impulse.magnitude(); counterDirection = player.getPosition().subtract(position).normalize(); return; } // Immune to stun/popup/knockback during counters active frames
    setVelocity(velocity.add(impulse));
  }
  
  @Override
  public void popup(float power) {
    if(counter && COUNTER_LAG_LENGTH-counterTimer < COUNTER_ACTIVE_LENGTH) { counterHit = true; counterPop = power; return; } // Immune to stun/popup/knockback during counters active frames
    setVSpeed(getVSpeed() + (power > 0.0f ? power : 0.0f));
  }
  
  @Override
  public void stun(int time) {
    if(counter && COUNTER_LAG_LENGTH-counterTimer < COUNTER_ACTIVE_LENGTH) { counterHit = true; counterStun = time; return; } // Immune to stun/popup/knockback during counters active frames
    stunTimer = time;
    effects.add("stn");
  }
  
}
