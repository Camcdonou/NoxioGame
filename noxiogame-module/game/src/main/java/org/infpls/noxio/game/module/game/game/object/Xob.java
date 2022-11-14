package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Xob extends Player {
  public static enum Permutation {
    XOB_N(0, UserUnlocks.Key.CHAR_XOB, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic});
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    public final Mobile.HitStun[] hits;
    Permutation(int permutation, UserUnlocks.Key unlock, Mobile.HitStun[] hits) {
       this.permutation = permutation;
       this.unlock = unlock;
       this.hits = hits;
    }
  }
  
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 27, BLIP_STUN_TIME = 30, BLIP_REFUND_POWER = 5;
  private static final int REWIND_COOLDOWN_LENGTH = 11, REWIND_POWER_MAX = 60, REWIND_POWER_ADD = 40, REWIND_LENGTH = 40, REWIND_DELAY = 4, REWIND_STUN = 45;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float BLIP_IMPULSE = 0.9f, REWIND_HIT_DAMPEN = 0.1f, REWIND_POPUP = 0.35f, BLIP_RADIUS = 0.6f, REWIND_RADIUS = 0.25f;
    
  private int blipCooldown, rewindCooldown, blipPower, rewindPower, rewindDelay;
  private boolean doRewind;
  private Vec3[] prevPos = new Vec3[REWIND_LENGTH];
  private final Permutation xobPermutation;
  public Xob(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Xob(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    xobPermutation = perm;
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* State */
    doRewind = false;
    for(int i=0;i<prevPos.length;i++) {
      prevPos[i] = this.position.concat(this.height);
    }
    
    /* Timers */
    blipCooldown = 0;
    rewindCooldown = 0;
    blipPower = BLIP_POWER_MAX;
    rewindPower = 0;
    rewindDelay = 0;
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(blipCooldown > 0) { blipCooldown--; }
    if(rewindCooldown > 0) { rewindCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
    if(rewindPower > 0) { rewindPower--; }
    if(doRewind) { rewindDelay++; }
    
    pre();
    rewind();
    
    // Not really a timer but we are doing it's updates on timer call for simplicity
    final Vec3[] roll = new Vec3[REWIND_LENGTH];
    roll[0] = position.concat(this.height);
    for(int i=0;i<REWIND_LENGTH-1;i++) {
      roll[i+1] = prevPos[i];
    }
    this.prevPos = roll;
  }

  @Override   /* Shine */
  public void actionA() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun((int)(BLIP_STUN_TIME*(((blipPower/BLIP_POWER_MAX)*0.75f)+0.25f)), xobPermutation.hits[0], this, Mobile.CameraShake.LIGHT);
        mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), this);
      }
      
      blipPower = 0;
      effects.add("atk");
      for(int i=0;i<hits.size();i++) {
        blipPower += BLIP_REFUND_POWER;
        effects.add("rfd");
      }
    }
  }
  
  @Override   /* Rewind */
  public void actionB() {
    if(rewindCooldown <= 0 && rewindPower + REWIND_POWER_ADD < REWIND_POWER_MAX && !doRewind) {
      doRewind = true;
      effects.add("glo");
    }
  }
  
  private void pre() {
    if(doRewind && rewindDelay == REWIND_DELAY-1) {
      effects.add("pre");
    }
  }
  
  private void rewind() {
    if(doRewind && rewindDelay >= REWIND_DELAY) {
      drop();
      rewindCooldown = REWIND_COOLDOWN_LENGTH;
      rewindPower += REWIND_POWER_ADD;
      setPosition(prevPos[REWIND_LENGTH-1].trunc());
      setHeight(prevPos[REWIND_LENGTH-1].z);
      
      final List<Mobile> hits = hitTest(position, REWIND_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun(REWIND_STUN, xobPermutation.hits[1], this, Mobile.CameraShake.MEDIUM);
        mob.setVelocity(mob.velocity.scale(REWIND_HIT_DAMPEN));
        mob.popup(REWIND_POPUP, this);
        effects.add("crt");
      }
      
      effects.add("mov");
      rewindDelay = 0;
      doRewind = false;
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
    doRewind = false;
    rewindDelay = 0;
  }
  
  @Override
  public String type() { return "xob"; }
}
