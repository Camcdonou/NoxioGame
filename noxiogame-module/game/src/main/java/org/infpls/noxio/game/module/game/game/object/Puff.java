package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.game.*;

public class Puff extends Player {
  private static final int REST_COOLDOWN_LENGTH = 15, REST_STUN_LENGTH = 45, REST_SLEEP_LENGTH = 99;
  private static final int POUND_COOLDOWN_LENGTH = 30, POUND_STUN_LENGTH = 25, POUND_CHANNEL_TIME = 9, POUND_HIT_DELAY = 4;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float REST_IMPULSE = 2.55f, POUND_DASH_IMPULSE = 0.5f, POUND_IMPULSE = 0.225f, POUND_POPUP = 0.175f, POUND_RADIUS = 0.45f, POUND_OFFSET = 0.33f;
  
  private Vec2 poundDirection;
  private boolean channelSleep, channelPound, delayPound;
  private int restCooldown, poundCooldown, delayTimer;
  public Puff(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Puff(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    poundDirection = new Vec2(1, 0);
    channelSleep = false;
    channelPound = false;
    restCooldown = 0;
    poundCooldown = 0;
    delayTimer = 0;
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(channelSleep && channelTimer <= 0) { wake(); }
    if(channelPound && channelTimer <= 0) { poundDash(); }
    if(delayTimer > 0) { delayTimer--; }
    if(delayPound && delayTimer <= 0) { pound(); }
    if(restCooldown > 0) { restCooldown--; }
    if(poundCooldown > 0) { poundCooldown--; }
    
  }

  @Override   /* Rest */
  public void actionA() {
    if(restCooldown <= 0) {
      restCooldown = REST_COOLDOWN_LENGTH;
      effects.add("atk");
      
      final List<Mobile> hits = hitTest(position, getRadius());
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun(REST_STUN_LENGTH, this);
        mob.knockback(normal.scale(REST_IMPULSE), this);
        effects.add("hta");
      }
      
      sleep();
    }
  }
  
  @Override   /* Pound */
  public void actionB() {
    if(poundCooldown <= 0) {
      poundCooldown = POUND_COOLDOWN_LENGTH;
      effects.add("mov");
      channelPound = true;
      channelTimer = POUND_CHANNEL_TIME;
    }
  }
  
  /* The dash of pound */
  public void poundDash() {
    effects.add("pnd");
    channelPound = false;
    drop();
    setVelocity(velocity.add(look.scale(POUND_DASH_IMPULSE)));
    delayPound = true;
    delayTimer = POUND_HIT_DELAY;
    poundDirection = look;
  }
  
  /* The delayed hitbox of pound */
  public void pound() {
    effects.add("pnh");
    delayPound = false;
    final Vec2 poundPos = position.add(poundDirection.scale(POUND_OFFSET));

    final List<Mobile> hits = hitTest(poundPos, getRadius());
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      final Vec2 normal = mob.getPosition().subtract(position).normalize();
      mob.stun(POUND_STUN_LENGTH, this);
      mob.knockback(normal.scale(POUND_IMPULSE), this);
      mob.popup(POUND_POPUP, this);
      effects.add("htb");
    }
  }
  
  private void sleep() {
    channelSleep = true;
    channelTimer = REST_SLEEP_LENGTH;
  }
  
  private void wake() {
    channelSleep = false;
    channelTimer = 0;
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
    if(channelSleep) { wake(); }
    channelPound = false;
    channelTimer = 0;
    delayPound = false;
    delayTimer = 0;
    poundCooldown = 0;
  }
  
  @Override
  public String type() { return "puf"; }
}
