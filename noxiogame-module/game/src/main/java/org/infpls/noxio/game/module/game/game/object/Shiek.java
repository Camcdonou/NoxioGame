package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Shiek extends Player {
  public static enum Permutation {
    VOX_N(0, UserUnlocks.Key.CHAR_VOXEL, new Mobile.HitStun[]{Mobile.HitStun.Electric}),
    VOX_VO(1, UserUnlocks.Key.ALT_VOXVO, new Mobile.HitStun[]{Mobile.HitStun.Electric}),
    VOX_GRN(2, UserUnlocks.Key.ALT_VOXGREEN, new Mobile.HitStun[]{Mobile.HitStun.ElectricGreen}),
    VOX_RB(3, UserUnlocks.Key.ALT_VOXRAINBOW, new Mobile.HitStun[]{Mobile.HitStun.ElectricRainbow}),
    VOX_GLD(4, UserUnlocks.Key.ALT_VOXGOLD, new Mobile.HitStun[]{Mobile.HitStun.ElectricPurple}),
    VOX_BLK(5, UserUnlocks.Key.ALT_VOXBLACK, new Mobile.HitStun[]{Mobile.HitStun.ElectricBlack}),
    VOX_LT(6, UserUnlocks.Key.ALT_VOXLOOT, new Mobile.HitStun[]{Mobile.HitStun.Electric});
    
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
  private static final int FLASH_COOLDOWN_LENGTH = 30, MARK_COOLDOWN_LENGTH = 10, FLASH_STUN_LENGTH = 45, FLASH_CHARGE_LENGTH = 7, FLASH_PENALTY_LENGTH = 5;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float FLASH_IMPULSE = 1.0f, FLASH_RADIUS = 0.8f;
  private static final float BLIP_IMPULSE = 0.875f, BLIP_RADIUS = 0.6f;
  
  private Vec2 mark;
  private boolean channelFlash;
  private int flashCooldown, blipCooldown, blipPower;
  private final Permutation shiekPermutation;
  public Shiek(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Shiek(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    shiekPermutation = perm;
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.735f;
    moveSpeed = 0.0380f; jumpHeight = 0.175f;
    
    /* Timers */
    mark = null;
    channelFlash = false;
    flashCooldown = 0;
    blipCooldown = 0;
    blipPower = BLIP_POWER_MAX;
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    super.timers();
    if(channelFlash && channelTimer <= 0) { flash(); }
    if(flashCooldown > 0) { flashCooldown--; }
    if(blipCooldown > 0) { blipCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
  }
  
  @Override   /* Blip */
  public void actionA() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun((int)(BLIP_STUN_TIME*(((blipPower/BLIP_POWER_MAX)*0.75f)+0.25f)), shiekPermutation.hits[0], this);
        mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), this);
      }
      
      blipPower = 0;
      effects.add("atk");
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
      mob.stun(FLASH_STUN_LENGTH, shiekPermutation.hits[0], this);
      mob.knockback(normal.scale(FLASH_IMPULSE), this);
    }
    
    mark = null;
    
    channelTimer = FLASH_PENALTY_LENGTH;
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
  public void stun(int time, Mobile.HitStun type) {
    super.stun(time, type);
    channelFlash = false;
    channelTimer = 0;
    flashCooldown = 0;
  }
  
  @Override
  public String type() { return "vox"; }
}
