package org.infpls.noxio.game.module.game.game;

import java.util.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.util.*;
import org.infpls.noxio.game.module.game.dao.user.UserData;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;

final public class Controller {
  private final NoxioGame game;
  private final String sid;   // Session ID of the player
  public final UserData user; // User info, settings, and unlocks
  
  private int team;                 // Team this player is on, -1 if no teams.
  private GameObject object;        // Object this controller is controlling
  
  private Vec2 direction;           // Player movement direction
  private float speed;              // Player movement speed
  private final List<String> action;
  
  private int respawnTimer;
  private int respawnPenalty;
  private boolean penalized, roundLock;
  
  public final Score score;    // Handles all stats and score related data
  
  private final List<String> update; // List of "impulse" updates on this frame. These are things that happen as single events such as whispers.
  
  private static final float VIEW_DISTANCE = 12.0f; /* Anything farther than this is out of view and not updated */
  
  public Controller(final NoxioGame game, final NoxioSession player) {
    this(game, player, -1);
  }
  
  public Controller(final NoxioGame game, final NoxioSession player, final int team) {
    this.game = game;
    sid = player.getSessionId();
    user = player.getUserData();
    
    direction = new Vec2(0.0f, 1.0f); speed = 0.0f;
    action = new ArrayList();

    respawnTimer = 0;
    respawnPenalty = 0;
    penalized = false;
    roundLock = false;
    
    score = new Score();
    update = new ArrayList();
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
      PLY::RNDINFO  - rnd;<string message>; // Message can be blank string to signal game is in normal play
      SYS::WHISPER  - wsp;<string txt>;
      SYS::ANNOUNCE - anc;<string code>;
      SYS::ADDCREDS - crd;<int credits>;
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
    
    int cc = score.getCreditChange();
    if(cc > 0) { sb.append("crd;"); sb.append(cc); sb.append(";"); }
    
    update.clear();
  }
  
  public void handlePacket(final String id, final Queue<String> q) {
    switch(id) {
      case "01" : { inputMouseNeutral(q); break; }
      case "04" : { inputMouse(q); break; }
      case "05" : { inputAction(q); break; }
      case "06" : { inputReset(q); break; }
      case "08" : { chatMessage(q); break; }
      default : { Oak.log(Oak.Level.WARN, "Invalid User Input: '" + id + "' User: '" + user + "'"); break; }
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
        game.gameOver("Game reset by lobby owner!", "What a dork!", null);
      }
      else {
        whisper("Only the lobby host can reset!");
      }
    }
    else { whisper("Only the lobby host can reset!"); } 
  }
  
  private void chatMessage(final Queue<String> q) {
    final String msg = q.remove();
    game.sendMessage(getDisplay() + " > " + msg);
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
  
  /* Called when this controller is either being removed from a player leaving the game or when the game has ended. */
  /* Returns stats accumulated in this game */
  public Score.Stats destroy() {
    if(object != null) {
      object.kill();
    }
    return score.getStats();
  }
  
  /* Returns custom sound file name IF this user has it unlocked and has one set */
  public String getCustomSound() {
    if(user.unlocks.has(UserUnlocks.Key.FT_SOUND) && user.settings.game.useCustomSound) {
       return user.settings.game.customSoundFile;
    }
    return "";
  }
  
  public void penalize() { penalized = true; respawnPenalty++; }
  public boolean respawnReady() { return respawnTimer<=0 && !roundLock; }
  public void setRound(final String info) { update.add("rnd;"+info+";"); roundLock = true; }
  public void clearRound() { update.add("rnd;;"); roundLock = false; }
  public void whisper(final String msg) { update.add("wsp;"+msg+";"); }
  public void announce(final String code) { update.add("anc;"+code+";"); }
  public void setClientTimer(String title, int time) { update.add("tim;" + title + ";" + time + ";"); }
  public String getUser() { return user.name; }
  public String getDisplay() { return user.display; }
  public String getSid() { return sid; }
  public int getTeam() { return team; }
  public void setTeam(final int t) { team = t; if(object!=null) { object.kill(); } game.sendMessage(getDisplay() + " joined " + (team==0?"Red":"Blue") + " Team."); }
  public GameObject getControlled() { return object; }
}
