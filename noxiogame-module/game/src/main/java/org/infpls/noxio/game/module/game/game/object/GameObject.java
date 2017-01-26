package org.infpls.noxio.game.module.game.game.object;

import java.util.*;

import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.session.Packet;

public abstract class GameObject {
  protected final NoxioGame game; /* Parent Game */
  
  protected final long oid; /* Object ID */
  private final String type; /* Object Type */
  
  protected boolean dead;
  protected Vec2 position, velocity;
  public GameObject(final NoxioGame game, final long oid, final String type) {
    this.game = game;
    this.oid = oid; this.type = type;
    this.dead = false;
    this.position = new Vec2();
    this.velocity = new Vec2();
  }
  public GameObject(final NoxioGame game, final long oid, final String type, final Vec2 position) {
    this.game = game;
    this.oid = oid; this.type = type;
    this.dead = false;
    this.position = position;
    this.velocity = new Vec2();
  }
  public GameObject(final NoxioGame game, final long oid, final String type, final Vec2 position, final Vec2 velocity) {
    this.game = game;
    this.oid = oid; this.type = type;
    this.dead = false;
    this.position = position;
    this.velocity = velocity;
  }
  
  public abstract void step(final List<Packet> updates);
  
  public void setPosition(final Vec2 a) { position = a; }
  public void setVelocity(final Vec2 a) { velocity = a; }
  
  public final long getOid() { return oid; }
  public final String getType() { return type; }
  public final Vec2 getPosition() { return position; }
  public final Vec2 getVelocity() { return velocity; }
  
  public boolean isDead() { return dead; } /* If this method returns true the object is destroyed on the next game tick. */
  public void kill() { dead = true; } /* Marks as dead and does whatever dead things do */
  public void destroy() { } /* Called right before removing the object from the game */
}
