package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Shiek extends Player {
  public static enum Permutation {
    VOX_N(0, UserUnlocks.Key.CHAR_VOXEL),
    VOX_GRN(1, UserUnlocks.Key.ALT_VOXELGREEN);
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    Permutation(int permutation, UserUnlocks.Key unlock) {
       this.permutation = permutation;
       this.unlock = unlock;
    }
  }
  
  private static final int FLASH_COOLDOWN_LENGTH = 30, MARK_COOLDOWN_LENGTH = 10, FLASH_STUN_LENGTH = 45, FLASH_CHARGE_LENGTH = 10;
  private static final int BANG_COOLDOWN_LENGTH = 22, BANG_POWER_USE = 15, BANG_POWER_MAX = 80;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float FLASH_IMPULSE = 1.0f, FLASH_RADIUS = 0.65f;
  private static final float BANG_THROW_IMPULSE = 0.15f, BANG_THROW_V_IMPULSE = -0.07f;
  
  private Vec2 mark;
  private boolean channelFlash;
  private int flashCooldown, bangCooldown, bangPower;
  public Shiek(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Shiek(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.755f;
    moveSpeed = 0.0385f; jumpHeight = 0.175f;
    
    /* Timers */
    mark = null;
    channelFlash = false;
    flashCooldown = 0;
    bangCooldown = 0;
    bangPower = BANG_POWER_MAX;
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    super.timers();
    if(channelFlash && channelTimer <= 0) { flash(); }
    if(flashCooldown > 0) { flashCooldown--; }
    if(bangCooldown > 0) { bangCooldown--; }
    if(bangPower < BANG_POWER_MAX) { bangPower++; }
  }
  
  @Override   /* Bang */
  public void actionA() {
   if(bangCooldown <= 0) {
      bangCooldown = BANG_COOLDOWN_LENGTH;
      effects.add("atk");
      final int count = Math.max(1, Math.min(3, bangPower / BANG_POWER_USE));
      bangPower -= Math.max(count*BANG_POWER_USE, BANG_POWER_USE);
      if(bangPower < 0) { bangPower = 0; }
     
      if(count > 2) {
        final Bomb ba, bb, bc;
        ba = new Bomb(game, game.createOid(), position, team, 15, this);
        bb = new Bomb(game, game.createOid(), position, team, 11, this);
        bc = new Bomb(game, game.createOid(), position, team, 19, this);
        ba.setVelocity(velocity.add(look.scale(BANG_THROW_IMPULSE)));
        bb.setVelocity(velocity.add(look.lerp(look.tangent(), 0.375f).normalize().scale(BANG_THROW_IMPULSE)));
        bc.setVelocity(velocity.add(look.lerp(look.tangent().inverse(), 0.375f).normalize().scale(BANG_THROW_IMPULSE)));
        ba.setHeight(radius*2f);
        bb.setHeight(radius*2f);
        bc.setHeight(radius*2f);
        ba.setVSpeed(BANG_THROW_V_IMPULSE);
        bb.setVSpeed(BANG_THROW_V_IMPULSE);
        bc.setVSpeed(BANG_THROW_V_IMPULSE);
        game.addObject(ba);
        game.addObject(bb);
        game.addObject(bc);
      }
      if(count == 2) {
        final Bomb ba, bb;
        ba = new Bomb(game, game.createOid(), position, team, 15, this);
        bb = new Bomb(game, game.createOid(), position, team, 11, this);
        ba.setVelocity(velocity.add(look.lerp(look.tangent(), 0.65f).normalize().scale(BANG_THROW_IMPULSE)));
        bb.setVelocity(velocity.add(look.lerp(look.tangent().inverse(), 0.65f).normalize().scale(BANG_THROW_IMPULSE)));
        ba.setHeight(radius*2f);
        bb.setHeight(radius*2f);
        ba.setVSpeed(BANG_THROW_V_IMPULSE);
        bb.setVSpeed(BANG_THROW_V_IMPULSE);
        game.addObject(ba);
        game.addObject(bb);
      }
      else {
        final Bomb ba;
        ba = new Bomb(game, game.createOid(), position, team, 15, this);
        ba.setVelocity(velocity.add(look.scale(BANG_THROW_IMPULSE)));
        ba.setHeight(radius*2f);
        ba.setVSpeed(BANG_THROW_V_IMPULSE);
        game.addObject(ba);
      }
      
   }
  }
  
  @Override   /* Flash */
  public void actionB() {
    if(flashCooldown <= 0) {
      if(mark == null) {
        flashCooldown = MARK_COOLDOWN_LENGTH;
        
        final List<Polygon> floors = game.map.getNearFloors(position, radius);
        boolean overFloor = collideFloors(position, floors);
        
        if(overFloor && getHeight() > -0.5) {
          effects.add("mrk");
          mark = getPosition();
        }
        else {
          effects.add("nom");
        }
      }
      else {
        flashCooldown = FLASH_COOLDOWN_LENGTH;
        channelFlash = true;
        channelTimer = FLASH_CHARGE_LENGTH;
        effects.add("chr");
      }
    }
  }
  
  public void flash() {
    effects.add("fsh");
    channelFlash = false;
    drop();
    setPosition(mark);
    setHeight(0f);
    setVSpeed(0f);
    
    final List<Mobile> hits = hitTest(mark, FLASH_RADIUS);
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      final Vec2 normal = mob.getPosition().subtract(mark).normalize();
      mob.stun(FLASH_STUN_LENGTH, this);
      mob.knockback(normal.scale(FLASH_IMPULSE), this);
    }
    
    mark = null;
  }
  
  /* This fuction is simple and just returns a boolean true/false if the object is over solid ground */
  private boolean collideFloors(final Vec2 pos, final List<Polygon> floors) {
    for(int i=0;i<floors.size();i++) {
      if(Intersection.pointInPolygon(position, floors.get(i))) {
        return true;
      }
      else {
        final Intersection.Instance inst = Intersection.polygonCircle(pos, floors.get(i), radius);
        if(inst != null && inst.distance < (radius*0.5)) { return true; }
      }
    }
    return false;
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
    channelFlash = false;
    channelTimer = 0;
    flashCooldown = 0;
  }
  
  @Override
  public String type() { return "vox"; }
}
