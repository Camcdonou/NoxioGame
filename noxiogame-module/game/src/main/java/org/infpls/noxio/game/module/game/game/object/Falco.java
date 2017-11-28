package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Oak;

public class Falco extends Player {
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 35;
  private static final int DASH_COOLDOWN_LENGTH = 45, CHARGE_TIME_LENGTH = 20;
  private static final int TAUNT_COOLDOWN_LENGTH = 10;
  private static final float BLIP_IMPULSE = 0.45f, BLIP_POPUP_IMPULSE = 0.25f, DASH_IMPULSE = 0.45f, DASH_POPOUP_IMPULSE = 0.25f, BLIP_OUTER_RADIUS = 0.1f;
  
  private boolean charge;
  private int chargeTimer;
  private int blipCooldown, dashCooldown, tauntCooldown, blipPower, dashPower;
  public Falco(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Falco(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.player.falco", position, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.1f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    charge = false;
    chargeTimer = 0;
    blipCooldown = 0;
    dashCooldown = 0;
    tauntCooldown = 0;
    blipPower = BLIP_POWER_MAX;
    dashPower = 0;
  }
  
  /* Applies player inputs to the object. */
  @Override
  public void movement() {
    if(charge) { return; }    // Can't act during charge (overriding defaults here)
    if(isGrounded()) { setVelocity(velocity.add(look.scale(moveSpeed*speed))); }
    else { setVelocity(velocity.add(look.scale(moveSpeed*speed).scale(AIR_CONTROL))); } // Reduced control while airborne
  }
  
  /* Performs action. */
  @Override
  public void actions() {
    if(charge) { return; }    // Can't act during charge
    for(int i=0;i<action.size();i++) {
      switch(action.get(i)) {
        case "atk" : { blip(); break; }
        case "mov" : { charge(); break; }
        case "tnt" : { taunt(); break; }
        case "jmp" : { jump(); break; }
        default : { Oak.log("Invalid action input::"  + action.get(i) + " @Falco.actions", 1); break; }
      }
    }
    action.clear();
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    if(chargeTimer > 0 ) { setVSpeed(getVSpeed()*0.05f); chargeTimer--; }
    if(charge && chargeTimer < 1) { dash(); }
    if(blipCooldown > 0) { blipCooldown--; }
    if(dashCooldown > 0) { dashCooldown--; }
    if(tauntCooldown > 0) { tauntCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
    if(dashPower > 0) { dashPower--; }
    if(stunTimer > 0) { stunTimer--; }
  }

  public void blip() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      if(getHeight() > -0.5) {
        for(int i=0;i<game.objects.size();i++) {
          GameObject obj = game.objects.get(i);
          if(obj != this && obj.getType().startsWith("obj.mobile")) {
            final Mobile mob = (Mobile)obj;
            if(mob.getPosition().distance(position) < mob.getRadius() + getRadius() + BLIP_OUTER_RADIUS && mob.getHeight() > -0.5) {
              if(obj.getType().startsWith("obj.mobile.player")) {
                final Player ply = (Player)obj;
                ply.stun(BLIP_STUN_TIME*(blipPower/BLIP_POWER_MAX));
              }
              final Vec2 normal = mob.getPosition().subtract(position).normalize();
              mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), this);
              mob.popup(BLIP_POPUP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f));
              final Controller c = game.getControllerByObject(this);
              if(c != null) { mob.tag(c); }
            }
          }
        }
      }
      blipPower = 0;
      effects.add("atk");
    }
  }
  
  public void charge() {
    if(dashCooldown <= 0) {
      dashCooldown = DASH_COOLDOWN_LENGTH;
      chargeTimer = CHARGE_TIME_LENGTH;
      charge = true;
      effects.add("chr");
    }
  }
  
  public void dash() {
    charge = false;
    drop();
    setVelocity(velocity.add(look.scale(DASH_IMPULSE)));
    setVSpeed(DASH_POPOUP_IMPULSE);
    effects.add("mov");
  }
  
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
  
  @Override
  public void stun(int time) {
    stunTimer = time;
    effects.add("stn");
    charge = false;
    chargeTimer = 0;
    dashCooldown = 0;
  }
}
