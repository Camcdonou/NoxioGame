package org.infpls.noxio.game.module.game.game.object; 


import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Bullet extends GameObject {
  private static final float FRICTION = 0.99f, RADIUS = 12.5f;
  private GameObject owner;
  private int life;
  public Bullet(final NoxioGame game, final long oid, final Vec2 position, final Vec2 velocity, final GameObject owner) {
    super(game, oid, "obj.bullet", position, velocity);
    this.owner = owner;
    life = 128;
  }
  
  @Override
  public void step() {
    /* @FIXME only update if a change actually happens */
    setPosition(position.add(velocity));
    setVelocity(velocity.scale(FRICTION));
    game.lobby.sendPacket(new PacketG12(oid, position, velocity));
    
    hitDetect();
    
    if(life-- <= 0) {
      kill();
    }
  }
  
  public void hitDetect() {
    for(int i=0;i<game.objects.size();i++) {
      GameObject obj = game.objects.get(i);
      if(obj.getPosition().distance(position) < RADIUS && !obj.isDead()) {
        if(obj.getType().equals("obj.player")) {
          if(obj == owner && life > 100) {
            /* Safe! */
          }
          else {
            obj.kill(owner);
          }
        }
        else if(obj.getType().equals("obj.bullet")) {
          if(obj == this || ((Bullet)obj).getOwner() == owner) {
            /* Safe! */
          }
          else {
            obj.kill();
            this.kill();
          }
        }
      }
    }
  }
  
  public void resetLife() { life=128; }
  public void setOwner(final GameObject owner) { this.owner = owner; }
  public GameObject getOwner() { return owner; }
}
