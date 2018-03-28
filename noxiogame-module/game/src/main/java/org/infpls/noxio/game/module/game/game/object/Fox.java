package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Fox extends Player {
  public static enum Permutation {
    BOX_N(0, UserUnlocks.Key.CHAR_BOX),
    BOX_RED(1, UserUnlocks.Key.ALT_BOXRED),
    BOX_GLD(2, UserUnlocks.Key.ALT_BOXGOLD);
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    Permutation(int permutation, UserUnlocks.Key unlock) {
       this.permutation = permutation;
       this.unlock = unlock;
    }
  }
  
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 30;
  private static final int DASH_COOLDOWN_LENGTH = 15, DASH_POWER_MAX = 60, DASH_POWER_ADD = 30, DASH_STUN_TIME = 30;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float BLIP_IMPULSE = 0.875f, DASH_IMPULSE = 0.25f, BLIP_RADIUS = 0.6f;
  
  private int blipCooldown, dashCooldown, blipPower, dashPower;
  public Fox(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Fox(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    
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
        mob.stun((int)(BLIP_STUN_TIME*(((blipPower/BLIP_POWER_MAX)*0.75f)+0.25f)), this);
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
      if(dashPower >= DASH_POWER_MAX) { stun(DASH_STUN_TIME); }
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
