package org.infpls.noxio.game.module.game.game.object;

import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.game.Controller;

public abstract class GameObject {
  protected final NoxioGame game; /* Parent Game */
  
  protected final int oid;        /* Object ID */
  private final String type;      /* Object Type */
  
  protected int team;             /* Team id for various uses, -1 is "No Team" id. */
  
  protected boolean dead;
  protected Vec2 position, velocity;
  public GameObject(final NoxioGame game, final int oid, final String type, final Vec2 position) {
    this(game, oid, type, position, new Vec2());
  }
  public GameObject(final NoxioGame game, final int oid, final String type, final Vec2 position, final Vec2 velocity) {
    this.game = game;
    this.oid = oid; this.type = type;
    this.dead = false;
    this.position = position;
    this.velocity = velocity;
    this.team = -1;
  }
  
  public abstract void step();
  public void post() {};
  public abstract void generateUpdateData(final StringBuilder sb);
  
  public void setPosition(final Vec2 a) { position = a; }
  public void setVelocity(final Vec2 a) { velocity = a; }
  
  public final int getOid() { return oid; }
  public final String getType() { return type; }
  public final Vec2 getPosition() { return position; }
  public final Vec2 getVelocity() { return velocity; }
  
  public boolean isGlobal() { return false; } /* If this returns true this object ignores fog of war and is globally visible. */
  
  public boolean isDead() { return dead; } /* If this method returns true the object is destroyed on the next game tick. */
  public void tag(final Controller player) { } /* When an offensive action hits this object the player who performed it is recorded and credited for points if it causes death. */
  public void kill() { dead = true; } /* Marks as dead and does whatever dead things do */
  public void destroy() { } /* Called right before removing the object from the game */
}
