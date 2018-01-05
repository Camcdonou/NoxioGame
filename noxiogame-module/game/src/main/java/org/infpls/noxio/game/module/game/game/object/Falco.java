package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.game.*;

public class Falco extends Player {
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 35;
  private static final int DASH_COOLDOWN_LENGTH = 45, CHARGE_TIME_LENGTH = 20;
  private static final int TAUNT_COOLDOWN_LENGTH = 10;
  private static final float BLIP_IMPULSE = 0.45f, BLIP_POPUP_IMPULSE = 0.25f, DASH_IMPULSE = 0.45f, DASH_POPOUP_IMPULSE = 0.25f, BLIP_RADIUS = 0.6f, DASH_FALL_DAMPEN_MULT = 0.15f;
  
  private boolean channelDash;
  private int blipCooldown, dashCooldown, blipPower, dashPower;
  public Falco(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Falco(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.1f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    channelDash = false;
    blipCooldown = 0;
    dashCooldown = 0;
    blipPower = BLIP_POWER_MAX;
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(channelDash && channelTimer <= 0) { dash(); }
    else if(channelDash) { setVSpeed(getVSpeed()*DASH_FALL_DAMPEN_MULT); }
    if(blipCooldown > 0) { blipCooldown--; }
    if(dashCooldown > 0) { dashCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
  }
  
  @Override /* Pop Shine */
  public void actionA() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun(BLIP_STUN_TIME*(blipPower/BLIP_POWER_MAX), this);
        mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), this);
        mob.popup(BLIP_POPUP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f), this);
      }
      
      blipPower = 0;
      effects.add("atk");
    }
  }
  
  @Override /* Firebird */
  public void actionB() {
    if(dashCooldown <= 0) {
      dashCooldown = DASH_COOLDOWN_LENGTH;
      channelDash = true;
      channelTimer = CHARGE_TIME_LENGTH;
      effects.add("chr");
    }
  }
  
  public void dash() {
    channelDash = false;
    drop();
    setVelocity(velocity.add(look.scale(DASH_IMPULSE)));
    setVSpeed(DASH_POPOUP_IMPULSE);
    effects.add("mov");
  }
  
  @Override
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
  
  @Override
  public void stun(int time) {
    super.stun(time);
    channelDash = false;
    channelTimer = 0;
    dashCooldown = 0;
  }
  
  @Override
  public String type() { return "flc"; }
}
