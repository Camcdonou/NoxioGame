package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Bullet extends GameObject {
  private static final float FRICTION = 0.99f;  
  private int life;
  public Bullet(final NoxioGame game, final long oid, final Vec2 position, final Vec2 velocity) {
    super(game, oid, "obj.bullet", position, velocity);
    life = 256;
  }
  
  @Override
  public void step(final List<Packet> updates) {
    /* @FIXME only update if a change actually happens */
    setPosition(position.add(velocity));
    setVelocity(velocity.scale(FRICTION));
    updates.add(new PacketG12(oid, position, velocity));
    
    life--;
    if(life <= 0) {
      kill();
    }
  }
}
