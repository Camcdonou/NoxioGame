package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Bullet extends GameObject {
  private static final float FRICTION = 0.99f;
  private final GameObject owner;
  private int life;
  public Bullet(final NoxioGame game, final long oid, final Vec2 position, final Vec2 velocity, final GameObject owner) {
    super(game, oid, "obj.bullet", position, velocity);
    this.owner = owner;
    life = 256;
  }
  
  @Override
  public void step(final List<Packet> updates) {
    /* @FIXME only update if a change actually happens */
    setPosition(position.add(velocity));
    setVelocity(velocity.scale(FRICTION));
    updates.add(new PacketG12(oid, position, velocity));
    
    hitDetect(updates);
    
    if(life-- <= 0) {
      kill();
    }
  }
  
  public void hitDetect(final List<Packet> updates) {
    for(int i=0;i<game.objects.size();i++) {
      GameObject obj = game.objects.get(i);
      if(obj.getType().equals("obj.player") & obj.getPosition().distance(position) < 10) {
        if(obj == owner && life > 230) {
          /* Safe! */
        }
        else {
          obj.kill();
        }
      }
    }
  }
}
