package org.infpls.noxio.game.module.game.game.object; 

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
  
  private final static int GEN_COOLDOWN_LENGTH = 10, TAUNT_COOLDOWN_LENGTH = 30;

  private int genCooldown;
  public Inferno(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Inferno(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0350f; jumpHeight = 0.250f;
    
    /* Timers */
    genCooldown = 0;
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(genCooldown > 0) { genCooldown--; }
  }
  
  @Override   /* Inferno Attack */
  public void actionA() {
    if(genCooldown <= 0) {
      genCooldown = GEN_COOLDOWN_LENGTH;
      kill();
    }
  }
  
  @Override   /* Inferno Dash */
  public void actionB() {
    if(genCooldown <= 0) {
      genCooldown = GEN_COOLDOWN_LENGTH;
      effects.add("mov");
      setVelocity(velocity.add(new Vec2((float)(Math.random()-0.5), (float)(Math.random()-0.5)).normalize().scale(0.5f)));
      stun(30);
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
  public String type() { return "inf"; }
}
