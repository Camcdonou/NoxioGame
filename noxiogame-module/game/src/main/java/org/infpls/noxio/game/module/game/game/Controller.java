package org.infpls.noxio.game.module.game.game;

import java.util.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.session.*;

final public class Controller {
  private final NoxioGame game;
  private final String user, sid;   // Username and Session ID of the player
  
  private int team;                 // Team this player is on, -1 if no teams.
  private GameObject object;        // Object this controller is controlling
  
  private Vec2 direction;           // Player movement direction
  private float speed;              // Player movement speed
  private final List<String> action;           /* @FIXME RENAME TO ACTION AND EFFECT LOL */
  
  private final Score score;        // DEPRECATED??
  
  private final List<String> whisper; /* Messages sent directly to this controller */
  
  private static final float VIEW_DISTANCE = 12.0f; /* Anything farther than this is out of view and not updated */
  
  public Controller(final NoxioGame game, final String user, final String sid) {
    this.game = game;
    this.user = user;
    this.sid = sid;
    
    this.direction = new Vec2(0.0f, 1.0f); this.speed = 0.0f;
    this.action = new ArrayList();

    this.score = new Score();
    this.whisper = new ArrayList();
    this.team = -1;
  }
  
  public Controller(final NoxioGame game, final String user, final String sid, final int team) {
    this.game = game;
    this.user = user;
    this.sid = sid;
    
    this.direction = new Vec2(0.0f, 1.0f); this.speed = 0.0f;
    this.action = new ArrayList();

    this.score = new Score();
    this.whisper = new ArrayList();
    this.team = team;
  }
  
  /* Generates a game update data for the player of this controller */
  /* Game updates are generated per controller so we can cull offscreen objects. */
  /* VIEW_DISTANCE is the value we use for screen culling */
  /* Table of different data structures that are generated --
      OBJ::UPDATE  - obj;<int oid>;<vec2 pos>;<vec2 vel>; (VARIABLE LENGTH!!! allows any number of extra fields after the intial 3)
      OBJ::HIDE    - hid;<int oid>;
      SYS::WHISPER - wsp;<string txt>;
  */
  public void generateUpdateData(final StringBuilder sb) {
    if(object != null) {
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        if(obj == object || obj.getPosition().distance(object.getPosition()) <= VIEW_DISTANCE) {
          obj.generateUpdateData(sb);
        }
        else {
          sb.append("hid;");
          sb.append(obj.getOid()); sb.append(";");
        }
      }
    }
    else { /* @FIXME when the controller has no object it disables the VIEW_DISTANCE cull */
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        obj.generateUpdateData(sb);
      }
    }
    for(int i=0;i<whisper.size();i++) {
      sb.append("wsp;"); sb.append(whisper.get(i)); sb.append(";");
    }
    whisper.clear();
  }
  
  public void handlePacket(Packet p) {
    switch(p.getType()) {
      case "i01" : { direction = ((PacketI01)p).getPos().normalize(); speed = 0.0f; break; }
      case "i04" : { direction = ((PacketI04)p).getPos().normalize(); speed = Math.min(Math.max(((PacketI04)p).getSpeed(), 0.33f), 1.0f); break; }
      case "i05" : { action.add(((PacketI05)p).getAbility()); break; }
      case "i06" : { final NoxioSession host = game.lobby.getHost(); if(host != null) { if(host.getSessionId().equals(sid)) { game.gameOver("Game reset by lobby owner!"); } else { whisper("Only the lobby host can reset!"); } } break; }
      default : { /* @FIXME ERROR REPORT */ break; }
    }
  }
  
  public void step() {
    if(object != null) {
      if(object.isDead()) { objectDestroyed(); return; }
      if(object.getType().equals("obj.mobile.player")) {
        Player p = (Player)object;
        p.setInput(direction, speed);
        for(int i=0;i<action.size();i++) { p.queueAction(action.get(i)); }
        action.clear();
      }
    }
  }
  
  public void setControl(GameObject object) {
    this.object = object;
    game.lobby.sendPacket(new PacketI03(object.getOid()), sid);
  }
  
  private void objectDestroyed() {
    this.object = null;
    game.lobby.sendPacket(new PacketI03(-1), sid);
  }
  
  public void destroy() {
    if(this.object != null) {
      this.object.kill();
    }
  }
  
  public void whisper(final String msg) { whisper.add(msg); }
  public String getUser() { return user; }
  public String getSid() { return sid; }
  public int getTeam() { return team; }
  public void setTeam(final int t) { team = t; if(object!=null) { object.kill(); } }
  public GameObject getControlled() { return object; }
  public Score getScore() { return score; }
}
