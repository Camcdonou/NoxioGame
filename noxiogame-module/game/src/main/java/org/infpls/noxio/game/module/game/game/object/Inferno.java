package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Inferno extends Player {
  public static enum Permutation {
    INF_N(0, UserUnlocks.Key.CHAR_INFERNO);
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    Permutation(int permutation, UserUnlocks.Key unlock) {
    this.permutation = permutation;
     this.unlock = unlock;
    }
  }
  
  private final static int GEN_COOLDOWN_LENGTH = 13, TAUNT_COOLDOWN_LENGTH = 30, DASH_LENGTH = 7;
  private final static float HURT_JUMP_BONUS = 0.0275f, HURT_SELF_MULT = 0.2f, OOF_RADIUS = 0.475f, DASH_IMPULSE = 0.325f, DASH_HIT_IMPULSE = 0.175f, GOOMBA_THRESHOLD = -0.245f, GOOMBA_DOOM_THRESHOLD = -0.5f;
  private final static int GOOMBA_STUN = 20;
  private final static float HURT_JUMP_FALLOFF_MAX = 0.435f, HURT_BONUS_MULT_MIN = 4.75f, HURT_BONUS_MULT_MAX = 0.25f, GOOMBA_IMPULSE = 0.4f, GROWTH_RATE = 1.05f;
  private int genCooldown;
  private int dashTimer;
  
  public Inferno(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Inferno(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.04f; jumpHeight = JUMP_HEIGHT; recoveryJumpHeight = RECOVERY_JUMP_HEIGHT;
    
    /* Timers */
    genCooldown = 0;
    dashTimer = 0;
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(genCooldown > 0) { genCooldown--; }
    if(dashTimer > 0) { dashing(); dashTimer--; }
    goomba();
  }
  
  @Override   /* Inferno Attack */
  public void actionA() {
    if(genCooldown <= 0) {
      genCooldown = GEN_COOLDOWN_LENGTH;
      destroyx();
      
      final List<Mobile> hits = hitTest(position, OOF_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun(1, Mobile.HitStun.Generic, this, Mobile.CameraShake.HEAVY);
        if(mob.is(GameObject.Types.PLAYER)) {
          ((Player)mob).impact(1);
          ((Player)mob).doom();
        }
        impact(1);
        effects.add("scr");
        
      }
    }
  }
  
  @Override   /* Inferno Dash */
  public void actionB() {
    if(genCooldown <= 0) {
      genCooldown = GEN_COOLDOWN_LENGTH;
      effects.add("mov");
      setVelocity(velocity.add(new Vec2((float)(Math.random()-0.5), (float)(Math.random()-0.5)).normalize().scale(DASH_IMPULSE)));
      popup(DASH_IMPULSE*0.5f);
      super.stun(5, Mobile.HitStun.Generic, 0, Mobile.CameraShake.LIGHT);
      hurted(0.2f);
      dashTimer = DASH_LENGTH;
    }
  }
  
  public void dashing() {
      final List<Mobile> hits = hitTest(position, radius);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.knockback(normal.scale(DASH_HIT_IMPULSE), this);
      }
  }
  
  public void goomba() {
    if(getVSpeed() > GOOMBA_THRESHOLD) { return; }
    final List<Mobile> hits = hitTest(position, radius);
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      final Vec2 normal = mob.getPosition().subtract(position).normalize();
      if((Math.abs(mob.height-height) < 0.625 || Math.abs(mob.height-height) < Math.abs(getVSpeed())*1.1) && height > mob.height) {
        mob.stun(GOOMBA_STUN, Mobile.HitStun.Generic, this, Mobile.CameraShake.HEAVY);
        if(getVSpeed() < GOOMBA_DOOM_THRESHOLD) {
          effects.add("scr");
          if(mob.is(GameObject.Types.PLAYER)) {
            ((Player)mob).impact(2);
            ((Player)mob).doom();
          }
        }
        else {
          if(mob.is(GameObject.Types.PLAYER)) {
            ((Player)mob).impact(2);
          }
          mob.knockback(normal.scale(GOOMBA_IMPULSE), this);
        }
        impact(2);
        stun(20, Mobile.HitStun.Generic, 0, Mobile.CameraShake.HEAVY);
      }
    }
  }
  
  @Override
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
  
  @Override
  public void stun(int time, Mobile.HitStun type, int impact, Mobile.CameraShake shake) {
    super.stun(time, type, impact, shake);
    hurted(1f);
    dashTimer = 0;
  }
  
  /* I got hurted now please give me daddies jumpies ;( */
  public void hurted(float mult) {
    float r = Math.max(0f, Math.min(1f, jumpHeight / HURT_JUMP_FALLOFF_MAX));
    float mix = (HURT_BONUS_MULT_MIN * (1f - r)) + (HURT_BONUS_MULT_MAX * r);
    
    jumpHeight += HURT_JUMP_BONUS * mult;
    recoveryJumpHeight += HURT_JUMP_BONUS * 0.35f * mult;
  }
  
  @Override
  public void killCredit(Player player) {
    super.killCredit(player);
    grow();
  }
  
/* OM NOM NOM */
  public void grow() {
    radius *= GROWTH_RATE;
    weight *= GROWTH_RATE*1f;
    effects.add("gro");
  }
  
  @Override
  public String type() { return "inf"; }
}
