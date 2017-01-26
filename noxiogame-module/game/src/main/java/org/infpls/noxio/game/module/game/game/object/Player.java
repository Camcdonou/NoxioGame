package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Player extends GameObject {
  private static final float SPEED = 1.2f, FRICTION = 0.90f;
  private static final float ACTION_ONE_SPEED = 3.3f;
  
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
    if(action != null) {
      switch(action.getAction()) {
         case "q" : { useActionOne(); break; }
         case "w" : { useActionTwo(); break; }
         case "e" : { useActionThree(); break; }
         case "r" : { useActionFour(); break; }
         default : { break; } /* @FIXME ERROR REPORTING */
      }
      action = null;
    }
  }
  
  private void useActionOne() {
    Vec2 dir = position.subtract(action.getTarget()).normalize();
    Bullet b = new Bullet(game, game.createOid(), position.copy(), dir.scale(ACTION_ONE_SPEED));
    game.addObject(b);
  }
  
  private void useActionTwo() {
    /* Do a thing */
  }
    
  private void useActionThree() {
    /* Do a thing */
  }
      
  private void useActionFour() {
    /* Do a thing */
  }
  
  public void setAction(final Action action) {
    this.action = action;
  }
}
