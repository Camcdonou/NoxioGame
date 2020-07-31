package org.infpls.noxio.game.module.game.game.object; 

import java.util.ArrayList;
import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Cube extends Player {
  public static enum Permutation {
    CUB_N(0, UserUnlocks.Key.CHAR_CUBE, new Mobile.HitStun[]{Mobile.HitStun.Electric});
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    public final Mobile.HitStun[] hits;
    Permutation(int permutation, UserUnlocks.Key unlock, Mobile.HitStun[] hits) {
       this.permutation = permutation;
       this.unlock = unlock;
       this.hits = hits;
    }
  }
  
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 30, BLIP_REFUND_POWER = 5;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float BLIP_IMPULSE = 0.875f, BLIP_RADIUS = 0.6f;
  private static final int BOMB_COOLDOWN_LENGTH = 10, BOMB_POWER_MAX =  120, BOMB_POWER_COST = 60, BOMB_STUN_TIME = 30, BOMB_FUSE_LENGTH = 50;
  private static final float BOMB_IMPULSE = .575f, BOMB_POPUP = .145f, BOMB_RADIUS = .415f;
  
  private int blipCooldown, blipPower, bombCooldown, bombPower;
  private final List<Bomblet> bombs;
  private final Permutation cubePermutation;
  public Cube(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Cube(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    cubePermutation = perm;
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.735f;
    moveSpeed = 0.0380f; jumpHeight = 0.175f;
    
    /* State */
    bombs = new ArrayList();
    
    /* Timers */
    blipCooldown = 0; bombCooldown = 0;
    blipPower = BLIP_POWER_MAX; bombPower = BOMB_POWER_MAX;
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    super.timers();
    if(blipCooldown > 0) { blipCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
    if(bombCooldown > 0) { bombCooldown--; }
    if(bombPower < BOMB_POWER_MAX) { bombPower++; }
    
    for(int i=0;i<bombs.size();i++) {
      final Bomblet b = bombs.get(i);
      if(--b.time <= 0) {
        bombs.remove(b);
        detonate(b);
        i--;
      }
    }
  }
  
  @Override   /* Blip */
  public void actionA() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun((int)(BLIP_STUN_TIME*(((blipPower/BLIP_POWER_MAX)*0.75f)+0.25f)), cubePermutation.hits[0], this, Mobile.CameraShake.LIGHT);
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
  
  @Override   /* Bomb */
  public void actionB() {
    if(bombCooldown <= 0 && bombPower >= BOMB_POWER_COST) {
      bombCooldown = BOMB_COOLDOWN_LENGTH;
      bombPower -= BOMB_POWER_COST;
      
      effects.add("bmb");
      bombs.add(new Bomblet(position.copy(), BOMB_FUSE_LENGTH));
    }
  }
  
  private void detonate(Bomblet bomb) {
    final List<Mobile> hits = hitTest(bomb.position, BOMB_RADIUS);
    if(bomb.position.subtract(position).magnitude() <= BOMB_RADIUS + radius) { hits.add(this); } // Test bomb against self
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      final Vec2 normal = mob.getPosition().subtract(bomb.position).normalize();
      if(mob != this) {
        mob.stun(BOMB_STUN_TIME, cubePermutation.hits[0], this, Mobile.CameraShake.MEDIUM);
        mob.knockback(normal.scale(BOMB_IMPULSE), this);
        mob.popup(BOMB_POPUP, this);
      }
      else { // No self stun or kill credit on bomb jump
        effects.add(cubePermutation.hits[0].id);
        cameraShake(CameraShake.MEDIUM);
        mob.knockback(normal.scale(BOMB_IMPULSE));
        mob.popup(BOMB_POPUP);
        drop();
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
  public String type() { return "cub"; }
  
  private class Bomblet {
    public final Vec2 position;
    public int time;
    public Bomblet(Vec2 position, int time) {
      this.position = position;
      this.time = time;
    }
  }
}
