package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Player extends GameObject {
  private static final float SPEED = 2.1f, FRICTION = 0.77f;
  private static final float ACTION_ONE_SPEED = 7.8f, ACTION_TWO_SPEED = 5.6f, ACTION_THREE_SPEED = 7.2f, ACTION_FOUR_SPEED = 4.6f;
  
  private int cooldown;
  private Action action;
  public Player(final NoxioGame game, final long oid, final Vec2 position) {
    super(game, oid, "obj.player", position);
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
  }
  
  private void useActionOne(final List<Packet> updates) {
    final Vec2 dir = action.getTarget().subtract(position).normalize();
    final Bullet b = new Bullet(game, game.createOid(), position.copy(), velocity.add(dir.scale(ACTION_ONE_SPEED)), this);
    game.addObject(b);
    updates.add(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    cooldown = 0;
  }
  
  private void useActionTwo(final List<Packet> updates) {
    final Vec2 dir = action.getTarget().subtract(position).normalize();
    final Vec2[] shots = new Vec2[]{
      dir.lerp(dir.tangent(), 0.0f),
      dir.lerp(dir.tangent(), 0.5f),
      dir.lerp(dir.tangent(), 1.0f),
      dir.lerp(dir.tangent().inverse(), 0.5f),
      dir.lerp(dir.tangent().inverse(), 0.0f)
    };
    for(int i=0;i<shots.length;i++) {
      final Bullet b = new Bullet(game, game.createOid(), position.copy(), velocity.add(shots[i].scale(ACTION_TWO_SPEED)), this);
      game.addObject(b);
      updates.add(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    cooldown = 10;
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
      final Bullet b = new Bullet(game, game.createOid(), position.copy(), velocity.add(shots[i].scale(ACTION_THREE_SPEED)), this);
      game.addObject(b);
      updates.add(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    cooldown = 10;
  }
      
  private void useActionFour(final List<Packet> updates) {
    final Vec2 dir = action.getTarget().subtract(position).normalize();
    final Vec2[] shots = new Vec2[]{
      dir.lerp(dir.tangent(), 0.0f),
      dir.lerp(dir.tangent(), 0.25f),
      dir.lerp(dir.tangent(), 0.5f),
      dir.lerp(dir.tangent(), 0.75f),
      dir.lerp(dir.tangent(), 1.0f),
      dir.lerp(dir.tangent().inverse(), 0.0f),
      dir.lerp(dir.tangent().inverse(), 0.25f),
      dir.lerp(dir.tangent().inverse(), 0.5f),
      dir.lerp(dir.tangent().inverse(), 0.75f),
      dir.inverse().lerp(dir.tangent(), 0.25f),
      dir.inverse().lerp(dir.tangent(), 0.5f),
      dir.inverse().lerp(dir.tangent(), 0.75f),
      dir.inverse().lerp(dir.tangent().inverse(), 0.25f),
      dir.inverse().lerp(dir.tangent().inverse(), 0.5f),
      dir.inverse().lerp(dir.tangent().inverse(), 0.75f)
    };
    for(int i=0;i<shots.length;i++) {
      final Bullet b = new Bullet(game, game.createOid(), position.copy(), velocity.add(shots[i].scale(ACTION_FOUR_SPEED)), this);
      game.addObject(b);
      updates.add(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    cooldown = 10;
  }
  
  public void setAction(final Action action) {
    this.action = action;
  }
}
