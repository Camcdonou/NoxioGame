package org.infpls.noxio.game.module.game.game.object;

import org.infpls.noxio.game.module.game.game.NoxioGame;

public abstract class GameObject {
  /* Haha, next question please~ */
  public final class Types {
    public static final int MOBILE = 0b00000001;
    public static final int PLAYER = 0b00000010;
    public static final int PICKUP = 0b00000100;
    public static final int FLAG =   0b00001000;
    public static final int HILL =   0b00010000;
    public static final int BOMB =   0b00100000;
    public static final int ZONE =   0b01000000;
  }
  
  protected final NoxioGame game; /* Parent Game */
  
  protected final int oid;         /* Object ID */
  public int bitIs;                /* No touchy */
  
  public final int permutation;      /* Used to set special *alternate* visuals to an object. 0 always refers to the default. */
  public final int team;             /* Team id. -1 is 'no team', 0 is 'red team', 1 is 'blue team' */
  protected int color;               /* Sets custom color on this object, this value is interpreted differntly based on team. 0 is default. */
  
  protected boolean dead, destroyed;
  protected Vec2 position, velocity;
  public GameObject(final NoxioGame game, final int oid, final Vec2 position, final int permutation) {
    this(game, oid, position, permutation, -1);
  }
  public GameObject(final NoxioGame game, final int oid, final Vec2 position, final int permutation, final int team) {
    this.game = game;
    
    this.oid = oid;
    bitIs = 0x00000000;
    
    this.dead = false; this.destroyed = false;
    this.position = position;
    this.velocity = new Vec2();
    this.permutation = permutation;
    this.team = team;
    this.color = 0;
  }
  
  public abstract void step();
  public void post() {};
  public abstract void generateUpdateData(final StringBuilder sb);
  
  public void setPosition(final Vec2 a) { position = a; }
  public void setVelocity(final Vec2 a) { velocity = a; }
  
  public final int getOid() { return oid; }
  public final boolean is(final int flag) { return (bitIs & flag) == flag; }
  public abstract String type();
  public final int color() { return color; }
  public void setColor(int c) { color = c; }
  
  public final Vec2 getPosition() { return position; }
  public final Vec2 getVelocity() { return velocity; }
  
  public boolean isGlobal() { return false; }    /* If this returns true this object ignores fog of war and is globally visible. */
    
  public boolean alive() { return !dead; }         /* Object is dead but not ready to be deleted. */
  public boolean destroyed() { return destroyed; } /* If this method returns true the object is destroyed on the next game tick. */
  public void tag(final Player player) { }       /* When an offensive action hits this object the player who performed it is recorded and credited for points if it causes death. */
  public void kill() { dead = true; }            /* Marks as dead and does whatever dead things do */
  public void destroyx() { dead = true; destroyed = true; }    /* Mark as destroyed, ready to be deleted. */
  public void onDelete() { }
}
