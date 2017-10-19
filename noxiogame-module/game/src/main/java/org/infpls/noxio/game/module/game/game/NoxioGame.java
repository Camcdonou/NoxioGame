package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.util.Oak;

public abstract class NoxioGame {
  
  public final GameLobby lobby;
  
  public final int respawnTime;       // Number of frames that a respawn takes
  public final int penaltyTime;       // Number of extra frames you wait if penalized for team kill or w/e
  
  private boolean gameOver;
  private int resetTimer;
   
  public final NoxioMap map;
  
  protected final List<Controller> controllers; // Player controller objects 
  public final List<GameObject> objects;        // Objects populating the game world
    
  private int idGen; /* Used to generate OIDs for objects. */
  public NoxioGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    this.lobby = lobby;
    
    this.map = map;
    
    controllers = new ArrayList();
    objects = new ArrayList();
    
    created = new ArrayList();
    deleted = new ArrayList();
    update = new ArrayList();
    
    respawnTime = settings.get("respawn_time", 30);
    penaltyTime = settings.get("penalty_time", 90);
    
    gameOver = false;
  }
    
  public void handlePackets(final List<Packet> packets) {
    for(int i=0;i<packets.size();i++) {
      Controller c = getController(packets.get(i).getSrcSid());
      if(packets.get(i).getType().equals("i00") && c != null) {
        final PacketI00 p = (PacketI00)(packets.get(i));
        final String[] spl = p.getData().split(";");
        final Queue<String> queue = new LinkedList(Arrays.asList(spl));
        for(int j=0;j<queue.size();j++) {
          final String id = queue.remove();
          switch(id) {
            case "02" : { spawnPlayer(c, queue);  break; }
            case "07" : { requestTeamChange(c, queue); break; }
            default : { c.handlePacket(id, queue); break; }
          }
        }
      }
      else {
        Oak.log("Invalid User Input '" + packets.get(i).getType() + "' " + (c!=null?c.getUser():"<NULL_CONTROLLER>") + "@NoxioGame.handlePackets", 1);
      }
    }
  }
  
  public void step() {
    if(!gameOver) {
      for(int i=0;i<controllers.size();i++) {
        final Controller cont = controllers.get(i);
        cont.step();
      }
    }
    else {
     if(resetTimer > 0) { resetTimer--; }
    }
    for(int i=0;i<objects.size();i++) {
      final GameObject obj = objects.get(i);
      if(obj.isDead()) {
        deleteObject(obj); i--;
        obj.destroy();
      }
      else { obj.step(); }
    }
  }
  
  /* Generates the game update packet for all non-localized updates. */
  /* EX: chat messages, object creation, object deletion */
  /* Arrays are commas seperated value lists such as 1,54,6,23,12 or big,fat,booty,blaster
  /* Table of different data structures that are generated --
      OBJ::CREATE   -  crt;<int oid>;<string type>;<vec2 pos>;<vec2 vel>;
      OBJ::DELETE   -  del;<int oid>;<vec2 pos>;
      SYS::SCORE    -  scr;<String gametype>;<String description>;<String[] players>;<String[] scores>;<float[] meter>;<float[] r>;<float[] g>;<float[] b>;
      SYS::MESSAGE  -  msg;<String message>;
      SYS::GAMEOVER -  end;<String winner>;
      DBG::TICK     -  tck;<long tick>;<long sent>;
  */
  private final List<GameObject> created, deleted;  // List of objects create/deleted on this frame. These changes come first in update data.
  protected final List<String> update;                // List of "impulse" updates on this frame. These are things that happen as single events such as chat messages.
  public void generateUpdatePackets(final long tick) {
    final StringBuilder sba = new StringBuilder(); // Append to start
    final StringBuilder sbb = new StringBuilder(); // Append to end
    
    sba.append("tck;"); sba.append(tick); sba.append(";");
    for(int i=0;i<created.size();i++) {
      final GameObject obj = created.get(i);
      sba.append("crt"); sba.append(";");
      sba.append(obj.getOid()); sba.append(";");
      sba.append(obj.getType()); sba.append(";");
      obj.getPosition().toString(sba); sba.append(";");
      obj.getVelocity().toString(sba); sba.append(";");
    }
    for(int i=0;i<deleted.size();i++) {
      final GameObject obj = deleted.get(i);
      sba.append("del"); sba.append(";");
      sba.append(obj.getOid()); sba.append(";");
      obj.getPosition().toString(sba); sba.append(";");
    }
    for(int i=0;i<update.size();i++) {
      sbb.append(update.get(i));
    }
    
    created.clear(); deleted.clear(); update.clear();
    
    final String globalPreData = sba.toString();
    final String globalPostData = sbb.toString();
    for(int i=0;i<controllers.size();i++) {
      final StringBuilder sbc = new StringBuilder();
      sbc.append(globalPreData);
      controllers.get(i).generateUpdateData(sbc);
      sbc.append(globalPostData);
      Packet p = new PacketG10(sbc.toString());
      lobby.sendPacket(p, controllers.get(i).getSid());
    }
  }
  
  /* Called after sending data to clients. Used for final cleanup of each tick. */
  public void post() {
    for(int i=0;i<objects.size();i++) {
      objects.get(i).post();
    }
  }
  
  protected void generateJoinPacket(NoxioSession player) {
    final StringBuilder sb = new StringBuilder();
    for(int i=0;i<objects.size();i++) {
      final GameObject obj = objects.get(i);
      sb.append("crt"); sb.append(";");
      sb.append(obj.getOid()); sb.append(";");
      sb.append(obj.getType()); sb.append(";");
      obj.getPosition().toString(sb); sb.append(";");
      obj.getVelocity().toString(sb); sb.append(";");
    }
    lobby.sendPacket(new PacketG10(sb.toString()), player);
  }
  
  public final void addObject(final GameObject obj) {
    objects.add(obj);
    created.add(obj);
  }
  
  public final void deleteObject(final GameObject obj) {
    objects.remove(obj);
    deleted.add(obj);
  }
  
  /* Returns controller for given SID */
  public Controller getController(final String sid) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getSid().equals(sid)) {
        return controllers.get(i);
      }
    }
    return null;
  }
  
  /* Returns controller for given Object */
  public Controller getControllerByObject(final GameObject obj) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getControlled() == obj) {
        return controllers.get(i);
      }
    }
    return null;
  }

  public static float SPAWN_SAFE_RADIUS = 5.0f;
  protected abstract void spawnPlayer(final Controller c, final Queue<String> q);
  
  public void join(final NoxioSession player) throws IOException {
    controllers.add(new Controller(this, player.getUser(), player.getSessionId()));
    generateJoinPacket(player);
    updateScore();
  }
  
  public void leave(final NoxioSession player) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getSid().equals(player.getSessionId())) {
        controllers.get(i).destroy();
        controllers.remove(i);
        return;
      }
    }
    updateScore();
  }
  
  public abstract void requestTeamChange(final Controller controller, final Queue<String> q);
  
  public abstract void reportKill(final Controller killer, final GameObject killed);
  public abstract void reportObjective(final Controller player, final GameObject objective);
  public abstract void updateScore();
  
  /* Send a message that will appear in all players chatlog */
  public void sendMessage(final String msg) {
    update.add("msg;"+msg+";");
  }
  
  public void gameOver(String msg) {
    update.add("end;"+msg+";");
    gameOver = true; resetTimer = 150;
  }
  
  public void close() {
    /* Do things! */
  }
  
  public boolean isGameOver() { return resetTimer < 1 && gameOver; }
  public final int createOid() { return idGen++; }
  public abstract String gametypeName();
  
  public class ScoreBoard {
    public final String name, score;
    public final float meter;
    public final Color3 color;
    public ScoreBoard(final String name, final String score, final float meter, final Color3 color) {
      this.name = name;
      this.score = score;
      this.meter = meter;
      this.color = color;
    }
  }
}
