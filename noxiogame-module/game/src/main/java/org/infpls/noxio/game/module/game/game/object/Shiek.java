package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Shiek extends Player {
  private static final int FLASH_COOLDOWN_LENGTH = 30, MARK_COOLDOWN_LENGTH = 10, FLASH_STUN_LENGTH = 45, FLASH_CHARGE_LENGTH = 10;
  private static final int BANG_COOLDOWN_LENGTH = 25;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float FLASH_IMPULSE = 1.0f, FLASH_RADIUS = 0.65f;
  private static final float BANG_THROW_IMPULSE = 0.05f, BANG_THROW_POPUP_IMPULSE = 0.15f;
  
  private Vec2 mark;
  private boolean channelFlash;
  private int flashCooldown, bangCooldown;
  public Shiek(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Shiek(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.player.shiek", position, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.755f;
    moveSpeed = 0.0385f; jumpHeight = 0.175f;
    
    /* Timers */
    mark = null;
    channelFlash = false;
    flashCooldown = 0;
    bangCooldown = 0;
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    super.timers();
    if(channelFlash && channelTimer <= 0) { flash(); }
    if(flashCooldown > 0) { flashCooldown--; }
    if(bangCooldown > 0) { bangCooldown--; }
  }
  
  @Override   /* Bang */
  public void actionA() {
   if(bangCooldown <= 0) {
      bangCooldown = BANG_COOLDOWN_LENGTH;
      effects.add("atk");
      final Bomb ba, bb, bc;
      ba = new Bomb(game, game.createOid(), position, team, 58, this);
      bb = new Bomb(game, game.createOid(), position, team, 56, this);
      bc = new Bomb(game, game.createOid(), position, team, 60, this);
      ba.setVelocity(velocity.add(look.scale(BANG_THROW_IMPULSE)));
      bb.setVelocity(velocity.add(look.lerp(look.tangent(), 0.33f).normalize().scale(BANG_THROW_IMPULSE)));
      bc.setVelocity(velocity.add(look.lerp(look.tangent().inverse(), 0.33f).normalize().scale(BANG_THROW_IMPULSE)));
      ba.popup(BANG_THROW_POPUP_IMPULSE);
      bb.popup(BANG_THROW_POPUP_IMPULSE);
      bc.popup(BANG_THROW_POPUP_IMPULSE);
      game.addObject(ba);
      game.addObject(bb);
      game.addObject(bc);
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
    if(getHeight() > -0.5) {
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        if(obj != this && obj.getType().startsWith("obj.mobile")) {
          final Mobile mob = (Mobile)obj;
          if(!mob.isIntangible() && mob.getPosition().distance(mark) < mob.getRadius() + FLASH_RADIUS && mob.getHeight() > -0.5) {
            if(obj.getType().startsWith("obj.mobile.player")) {
              final Player ply = (Player)obj;
              ply.stun(FLASH_STUN_LENGTH);
            }
            final Vec2 normal = mob.getPosition().subtract(position).normalize();
            mob.knockback(normal.scale(FLASH_IMPULSE), this);
            final Controller c = game.getControllerByObject(this);
            if(c != null) { mob.tag(c); }
          }
        }
      }
    }
    setPosition(mark);
    setHeight(0f);
    setVSpeed(0f);
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
}
