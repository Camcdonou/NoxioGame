package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Fox extends Player {
  public static enum Permutation {
    BOX_N(0, UserUnlocks.Key.CHAR_BOX, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic}),
    BOX_VO(1, UserUnlocks.Key.ALT_BOXVO, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic}),
    BOX_RED(2, UserUnlocks.Key.ALT_BOXRED, new Mobile.HitStun[]{Mobile.HitStun.ElectricRed, Mobile.HitStun.Generic}),
    BOX_RB(3, UserUnlocks.Key.ALT_BOXRAINBOW, new Mobile.HitStun[]{Mobile.HitStun.ElectricRainbow, Mobile.HitStun.Generic}),
    BOX_GLD(4, UserUnlocks.Key.ALT_BOXGOLD, new Mobile.HitStun[]{Mobile.HitStun.ElectricPurple, Mobile.HitStun.Generic}),
    BOX_DEL(5, UserUnlocks.Key.ALT_BOXDELTA, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic}),
    BOX_HIT(6, UserUnlocks.Key.ALT_BOXHIT, new Mobile.HitStun[]{Mobile.HitStun.Generic, Mobile.HitStun.Generic}),
    BOX_FOR(7, UserUnlocks.Key.ALT_BOXFOUR, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic}),
    BOX_BLD(8, UserUnlocks.Key.ALT_BOXBLOOD, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic}),
    BOX_LT(9, UserUnlocks.Key.ALT_BOXLOOT, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic});
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    public final Mobile.HitStun[] hits;
    Permutation(int permutation, UserUnlocks.Key unlock, Mobile.HitStun[] hits) {
       this.permutation = permutation;
       this.unlock = unlock;
       this.hits = hits;
    }
  }
  
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 30;
  private static final int DASH_COOLDOWN_LENGTH = 15, DASH_POWER_MAX = 60, DASH_POWER_ADD = 30, DASH_STUN_TIME = 30;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float BLIP_IMPULSE = 0.875f, DASH_IMPULSE = 0.25f, BLIP_RADIUS = 0.6f;
  
  private int blipCooldown, dashCooldown, blipPower, dashPower;
  private final Permutation foxPermutation;
  public Fox(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Fox(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    foxPermutation = perm;
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    blipCooldown = 0;
    dashCooldown = 0;
    blipPower = BLIP_POWER_MAX;
    dashPower = 0;
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(blipCooldown > 0) { blipCooldown--; }
    if(dashCooldown > 0) { dashCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
    if(dashPower > 0) { dashPower--; }
  }

  @Override   /* Shine */
  public void actionA() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun((int)(BLIP_STUN_TIME*(((blipPower/BLIP_POWER_MAX)*0.75f)+0.25f)), foxPermutation.hits[0], this, Mobile.CameraShake.LIGHT);
        mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), this);
      }
      
      blipPower = 0;
      effects.add("atk");
    }
  }
  
  @Override   /* Wavedash */
  public void actionB() {
    if(dashCooldown <= 0 && dashPower < DASH_POWER_MAX) {
      drop();
      dashCooldown = DASH_COOLDOWN_LENGTH;
      dashPower += DASH_POWER_ADD;
      setVelocity(velocity.add(look.scale(DASH_IMPULSE)));
      effects.add("mov");
      if(dashPower >= DASH_POWER_MAX) { stun(DASH_STUN_TIME, foxPermutation.hits[1], 0, Mobile.CameraShake.LIGHT); }
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
  public String type() { return "box"; }
}
