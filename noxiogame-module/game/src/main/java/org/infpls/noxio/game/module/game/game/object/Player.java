package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Player extends GameObject {
  private static final float SPEED = 1.1f, FRICTION = 0.88f;
  private static final float ACTION_ONE_SPEED = 8.8f, ACTION_TWO_SPEED = 6.6f, ACTION_THREE_SPEED = 8.2f, SHINE_RADIUS = 55.0f;
  
  private int cooldown, burstCooldown, spawnProtection;
  private Action action;
  public Player(final NoxioGame game, final long oid, final Vec2 position) {
    super(game, oid, "obj.player", position);
    cooldown = 0; burstCooldown = 0; spawnProtection = 66;
  }
  
  public void move(final Vec2 direction) {
    setVelocity(velocity.add(direction.scale(SPEED)));
  }
  
  @Override
  public void step(final List<Packet> updates) {
    /* @FIXME only update if a change actually happens */
    
    /* Movement */
    setPosition(position.add(velocity));
    setVelocity(velocity.scale(FRICTION));
    updates.add(new PacketG12(oid, position, velocity));
    
    /* Action */
    if(action != null && cooldown < 1) {
      switch(action.getAction()) {
         case "q" : { useActionOne(updates); break; }
         case "w" : { useActionTwo(updates); break; }
         case "e" : { useActionThree(updates); break; }
         case "r" : { useActionFour(updates); break; }
         default : { break; } /* @FIXME ERROR REPORTING */
      }
      action = null;
    }
    if(cooldown > 0) { cooldown--; }
    if(burstCooldown > 0) { burstCooldown--; }
    if(spawnProtection > 0) { spawnProtection--; }
  }
  
  private void useActionOne(final List<Packet> updates) {
    if(burstCooldown < 8) {
      final Vec2 dir = action.getTarget().subtract(position).normalize();
      final Bullet b = new Bullet(game, game.createOid(), position.copy(), dir.scale(ACTION_ONE_SPEED), this);
      game.addObject(b);
      updates.add(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
      burstCooldown+=2;
      if(burstCooldown >= 8) {
        cooldown=15;
      }
    }
  }
  
  private void useActionTwo(final List<Packet> updates) {
    final Vec2 dir = action.getTarget().subtract(position).normalize();
    final Vec2[] shots = new Vec2[]{
      dir.lerp(dir.tangent(), 0.0f),
      dir.lerp(dir.tangent(), 0.33f),
      dir.lerp(dir.tangent(), 0.66f),
      dir.lerp(dir.tangent(), 1.0f),
      dir.lerp(dir.tangent().inverse(), 0.66f),
      dir.lerp(dir.tangent().inverse(), 0.33f),
      dir.lerp(dir.tangent().inverse(), 0.0f)
    };
    for(int i=0;i<shots.length;i++) {
      final Bullet b = new Bullet(game, game.createOid(), position.copy(), shots[i].scale(ACTION_TWO_SPEED), this);
      game.addObject(b);
      updates.add(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    cooldown = 30;
  }
    
  private void useActionThree(final List<Packet> updates) {
    final Vec2 dir = action.getTarget().subtract(position).normalize();
    final Vec2[] shots = new Vec2[]{
      dir.lerp(dir.tangent(), 0.8f),
      dir.lerp(dir.tangent(), 0.9f),
      dir.lerp(dir.tangent(), 1.0f),
      dir.lerp(dir.tangent().inverse(), 0.9f),
      dir.lerp(dir.tangent().inverse(), 0.8f)
    };
    for(int i=0;i<shots.length;i++) {
      final Bullet b = new Bullet(game, game.createOid(), position.copy(), shots[i].scale(ACTION_THREE_SPEED), this);
      game.addObject(b);
      updates.add(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    cooldown = 15;
  }
      
  private void useActionFour(final List<Packet> updates) {
    for(int i=0;i<game.objects.size();i++) {
      GameObject obj = game.objects.get(i);
      if(obj.getPosition().distance(position) < SHINE_RADIUS) {
        if(obj.getType().equals("obj.player")) {
          if(obj == this) {
            /* Safe! */
          }
          else {
            Packet p = obj.kill(this);
            if(p != null) { updates.add(p); }
          }
        }
        else if(obj.getType().equals("obj.bullet")) {
          obj.setVelocity(obj.getPosition().subtract(position).normalize().scale((obj.getVelocity().magnitude()*1.5f)+3.0f));
        }
      }
    }
    updates.add(new PacketG13(oid));
    cooldown = 30;
  }
  
  public void setAction(final Action action) {
    if(cooldown < 5) {
      this.action = action;
    }
  }
  
  @Override
  public PacketG15 kill(GameObject killer) {
    if(spawnProtection < 1) { dead = true; return game.reportKill(killer, this); }
    return null;
  }
  
  @Override
  public void kill() {
    if(spawnProtection < 1) { dead = true; }
  }
}
