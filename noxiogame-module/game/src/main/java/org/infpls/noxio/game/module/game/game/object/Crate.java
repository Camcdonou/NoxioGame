package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Crate extends Player {
  public static enum Permutation {
    CRT_N(0, UserUnlocks.Key.CHAR_CRATE, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Fire}),
    CRT_VO(1, UserUnlocks.Key.ALT_CRATEVO, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Fire}),
    CRT_ORN(2, UserUnlocks.Key.ALT_CRATEORANGE, new Mobile.HitStun[]{Mobile.HitStun.ElectricOrange, Mobile.HitStun.Fire}),
    CRT_RB(3, UserUnlocks.Key.ALT_CRATERAINBOW, new Mobile.HitStun[]{Mobile.HitStun.ElectricRainbow, Mobile.HitStun.FireRainbow}),
    CRT_GLD(4, UserUnlocks.Key.ALT_CRATEGOLD, new Mobile.HitStun[]{Mobile.HitStun.ElectricPurple, Mobile.HitStun.FirePurple}),
    CRT_DEL(5, UserUnlocks.Key.ALT_CRATEDELTA, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.FireBlue}),
    CRT_FIR(6, UserUnlocks.Key.ALT_CRATEFIRE, new Mobile.HitStun[]{Mobile.HitStun.Fire, Mobile.HitStun.Fire}),
    CRT_BLK(7, UserUnlocks.Key.ALT_CRATEBLACK, new Mobile.HitStun[]{Mobile.HitStun.ElectricBlack, Mobile.HitStun.FireBlack}),
    CRT_LT(8, UserUnlocks.Key.ALT_CRATELOOT, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Fire});
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    public final Mobile.HitStun[] hits;
    Permutation(int permutation, UserUnlocks.Key unlock, Mobile.HitStun[] hits) {
       this.permutation = permutation;
       this.unlock = unlock;
       this.hits = hits;
    }
  }
  
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 35, BLAST_STUN_TIME = 25, BLIP_REFUND_POWER = 5;
  private static final int DASH_COOLDOWN_LENGTH = 45, CHARGE_TIME_LENGTH = 20;
  private static final int TAUNT_COOLDOWN_LENGTH = 60;
  private static final float BLIP_IMPULSE = 0.4875f, BLIP_POPUP_IMPULSE = 0.215f, DASH_IMPULSE = 0.425f, DASH_POPOUP_IMPULSE = 0.225f, BLIP_RADIUS = 0.6f, BLAST_RADIUS = 0.75f, BLAST_IMPULSE = 0.6f, DASH_FALL_DAMPEN_MULT = 0.15f;
  private static final float CRITICAL_MULT = 1.55f;
  private static final int CRITICAL_WINDOW_LENGTH = 20;
  
  private boolean channelDash;
  private int blipCooldown, dashCooldown, blipPower, dashPower, criticalTimer;
  private final Permutation cratePermutation;
  public Crate(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Crate(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    cratePermutation = perm;
    
    /* Settings */
    radius = 0.5f; weight = 1.1f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    channelDash = false;
    blipCooldown = 0;
    dashCooldown = 0;
    criticalTimer = 0;
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
    if(criticalTimer > 0) { criticalTimer--; }
  }
  
  @Override /* Pop Shine */
  public void actionA() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      final boolean isCrit = criticalTimer > 0;
      
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun((int)(BLIP_STUN_TIME*(blipPower/BLIP_POWER_MAX)*(isCrit?CRITICAL_MULT:1.0f)), cratePermutation.hits[0], this, isCrit?Mobile.CameraShake.HEAVY:Mobile.CameraShake.LIGHT);
        mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)*(isCrit?CRITICAL_MULT:1.0f)), this);
        mob.popup(BLIP_POPUP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f), this);
      }
      
      blipPower = 0;
      effects.add("atk");
      for(int i=0;i<hits.size();i++) {
        blipPower += BLIP_REFUND_POWER;
        effects.add("rfd");
      }
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
  
  @Override
  public void forceMovementCooldown() { dashCooldown = DASH_COOLDOWN_LENGTH; }
  
  public void dash() {
    channelDash = false;
    criticalTimer = CRITICAL_WINDOW_LENGTH;
    drop();
    setVelocity(velocity.add(look.scale(DASH_IMPULSE)));
    setVSpeed(DASH_POPOUP_IMPULSE);
    
    final List<Mobile> hits = hitTest(position, BLAST_RADIUS);
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      final Vec2 normal = mob.getPosition().subtract(position).normalize();
      mob.stun(BLAST_STUN_TIME, cratePermutation.hits[1], this, Mobile.CameraShake.LIGHT);
      mob.knockback(normal.scale(BLAST_IMPULSE), this);
    }
    
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
  public void stun(int time, Mobile.HitStun type, int impact, Mobile.CameraShake shake) {
    super.stun(time, type, impact, shake);
    channelDash = false;
    channelTimer = 0;
    dashCooldown = 0;
  }
  
  @Override
  public String type() { return "crt"; }
}
