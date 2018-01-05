package org.infpls.noxio.game.module.game.game;

import java.util.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.util.*;

final public class Controller {
  private final NoxioGame game;
  private final String user, sid;   // Username and Session ID of the player
  
  private int team;                 // Team this player is on, -1 if no teams.
  private GameObject object;        // Object this controller is controlling
  
  private Vec2 direction;           // Player movement direction
  private float speed;              // Player movement speed
  private final List<String> action;
  
  private int respawnTimer;
  private int respawnPenalty;
  private boolean penalized;
  
  private final Score score;        // DEPRECATED?? no????
  
  private final List<String> update; // List of "impulse" updates on this frame. These are things that happen as single events such as whispers.
  
  private static final float VIEW_DISTANCE = 12.0f; /* Anything farther than this is out of view and not updated */
  
  public Controller(final NoxioGame game, final String user, final String sid) {
    this(game, user, sid, -1);
  }
  
  public Controller(final NoxioGame game, final String user, final String sid, final int team) {
    this.game = game;
    this.user = user;
    this.sid = sid;
    
    this.direction = new Vec2(0.0f, 1.0f); this.speed = 0.0f;
    this.action = new ArrayList();

    this.respawnTimer = 0;
    this.respawnPenalty = 0;
    this.penalized = false;
    
    this.score = new Score();
    this.update = new ArrayList();
    this.team = team;
  }
  
  /* Generates a game update data for the player of this controller */
  /* Game updates are generated per controller so we can cull offscreen objects. */
  /* VIEW_DISTANCE is the value we use for screen culling */
  /* Table of different data structures that are generated --
      OBJ::UPDATE   - obj;<int oid>;<vec2 pos>;<vec2 vel>; (VARIABLE LENGTH!!! allows any number of extra fields after the intial 3)
      OBJ::HIDE     - hid;<int oid>;
      PLY::CONTROL  - ctl;<int oid>;
      PLY::RSPWNTMR - rst;<int time>;
      SYS::WHISPER  - wsp;<string txt>;
      SYS::ANNOUNCE - anc;<string code>;
  */
  public void generateUpdateData(final StringBuilder sb) {
    if(object != null) {
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        if(obj == object || obj.isGlobal() || obj.getPosition().distance(object.getPosition()) <= VIEW_DISTANCE) {
          obj.generateUpdateData(sb);
        }
        else {
          sb.append("hid;");
          sb.append(obj.getOid()); sb.append(";");
        }
      }
    }
    else { /* @FIXME: Fully flesh out spectating. */
      for(int i=0;i<game.objects.size();i++) {
        GameObject obj = game.objects.get(i);
        obj.generateUpdateData(sb);
      }
    }
    
    for(int i=0;i<update.size();i++) {
      sb.append(update.get(i));
    }
    update.clear();
  }
  
  public void handlePacket(final String id, final Queue<String> q) {
    switch(id) {
      case "01" : { inputMouseNeutral(q); break; }
      case "04" : { inputMouse(q); break; }
      case "05" : { inputAction(q); break; }
      case "06" : { inputReset(q); break; }
      case "08" : { chatMessage(q); break; }
      default : { Oak.log("Invalid User Input '" + id + "' " + user + "@Controller.handlePacket", 1); break; }
    }
  }
  
  /* Handles i00->01 */
  private void inputMouseNeutral(final Queue<String> q) {
    final Vec2 pos = Parse.vec2(q.remove());
    direction = pos.normalize(); speed = 0.0f;
  }
  
  /* Handles i00->04 */
  private void inputMouse(final Queue<String> q) {
    final Vec2 pos = Parse.vec2(q.remove());
    final float spd = Parse.f(q.remove());
    direction = pos.normalize(); speed = Math.min(Math.max(spd, 0.33f), 1.0f);
  }
  
  /* Handles i00->05 */
  private void inputAction(final Queue<String> q) {
    final String[] a = q.remove().split(",");
    for(int i=0;i<a.length;i++) {
      action.add(a[i]);
    }
  }
  
  /* Handles i00->06 */
  private void inputReset(final Queue<String> q) {
    final NoxioSession host = game.lobby.getHost();
    if(host != null) {
      if(host.getSessionId().equals(sid)) {
        game.gameOver("Game reset by lobby owner!");
      }
      else {
        whisper("Only the lobby host can reset!");
      }
    }
    else { whisper("Only the lobby host can reset!"); } 
  }
  
  private void chatMessage(final Queue<String> q) {
    final String msg = q.remove();
    game.sendMessage(this.user + " > " + msg);
  }
  
  public void step() {
    if(respawnTimer > 0) { respawnTimer--; }
    if(object != null) {
      if(object.isDead()) { objectDestroyed(); return; }
      if(object.is(GameObject.Types.PLAYER)) {
        Player p = (Player)object;
        p.setInput(direction, speed);
        for(int i=0;i<action.size();i++) { p.queueAction(action.get(i)); }
        action.clear();
      }
    }
    else { 
      action.clear();
    }
  }
  
  public void setControl(GameObject obj) {
    object = obj;
    update.add("ctl;"+obj.getOid()+";");
  }
  
  private void objectDestroyed() {
    object = null;
    respawnTimer = game.respawnTime + (penalized?(game.penaltyTime * respawnPenalty):0);
    penalized = false;
    update.add("ctl;-1;");
    update.add("rst;"+respawnTimer+";");
  }
  
  public void destroy() {
    if(object != null) {
      object.kill();
    }
  }
  
  public void penalize() { penalized = true; respawnPenalty++; }
  public boolean respawnReady() { return respawnTimer<=0; }
  public void whisper(final String msg) { update.add("wsp;"+msg+";"); }
  public void announce(final String code) { update.add("anc;"+code+";"); }
  public String getUser() { return user; }
  public String getSid() { return sid; }
  public int getTeam() { return team; }
  public void setTeam(final int t) { team = t; if(object!=null) { object.kill(); } game.sendMessage(getUser() + " joined " + (team==0?"Red":"Blue") + " Team."); }
  public GameObject getControlled() { return object; }
  public Score getScore() { return score; }
}
