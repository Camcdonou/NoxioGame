package org.infpls.noxio.game.module.game.game;

import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Controller {
  private final NoxioGame game;
  private final String user, sid;
  private GameObject object;
  private Vec2 direction;
  private float speed;
  private final Score score;
  
  private static final float VIEW_DISTANCE = 25.0f; /* Anything farther than this is out of view and not updated */
  
  public Controller(final NoxioGame game, final String user, final String sid) {
    this.game = game;
    this.user = user;
    this.sid = sid;
    this.direction = new Vec2(); this.speed = 1.0f;
    this.score = new Score(user);
  }
  /* Generates a game update data for the player of this controller */
  /* Game updates are generated per controller so we can cull offscreen objects. */
  /* VIEW_DISTANCE is the value we use for screen culling */
  /* Table of different data structures that are generated --
      OBJ::UPDATE - obj;<int oid>;<vec2 pos>;<vec2 vel>; (VARIABLE LENGTH!!! allows any number of extra fields after the intial 3)
  */
  public void generateUpdateData(final StringBuilder sb) {
    if(object != null) {
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        if(obj.getPosition().distance(object.getPosition()) <= VIEW_DISTANCE) {
          obj.generateUpdateData(sb);
        }
      }
    }
  }
  
  public void handlePacket(Packet p) {
    switch(p.getType()) {
      case "i04" : { direction = ((PacketI04)p).getPos(); speed = 0.5f; break; }
      case "i05" : { direction = ((PacketI05)p).getPos(); speed = 1.0f; break; }
      case "i01" : { direction = null; break; }
      default : { /* @FIXME ERROR REPORT */ break; }
    }
  }
  
  public void step() {
    if(object != null && direction != null) {
      if(object.getType().equals("obj.mobile.player")) {
        Player p = (Player)object;
        final Vec2 move = direction.normalize().scale(speed);
        if(!(move.isNaN() || move.isZero())) { p.move(direction.normalize().scale(speed)); }
      }
    }
  }
  
  public void setControl(GameObject object) {
    this.object = object;
    game.lobby.sendPacket(new PacketI03(object.getOid()), sid);
  }
  
  public void objectDestroyed() {
    this.object = null;
    game.lobby.sendPacket(new PacketI03(-1), sid);
  }
  
  public void destroy() {
    if(this.object != null) {
      this.object.kill();
    }
  }
  
  public String getUser() { return user; }
  public String getSid() { return sid; }
  public GameObject getControlled() { return object; }
  public Score getScore() { return score; }
}
