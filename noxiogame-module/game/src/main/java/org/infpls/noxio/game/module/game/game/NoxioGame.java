package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.lobby.*;

public abstract class NoxioGame {
  public final GameLobby lobby;
  
  public final int respawnTime;       // Number of frames that a respawn takes
  public final int penaltyTime;       // Number of extra frames you wait if penalized for team kill or w/e
  
  private boolean gameOver;
  private int resetTimer;
   
  public final NoxioMap map;
  
  protected final List<Controller> controllers; /* Player controller objects */
  public final List<GameObject> objects; /* All game objects */
  
  private int idGen; /* Used to generate OIDs for objects. */
  public NoxioGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    this.lobby = lobby;
    
    this.map = map;
    
    controllers = new ArrayList();
    objects = new ArrayList();
    
    created = new ArrayList();
    deleted = new ArrayList();
    
    respawnTime = settings.get("respawn_time", 30);
    penaltyTime = settings.get("penalty_time", 90);
    
    gameOver = false;
  }
    
  public void handlePackets(final List<Packet> packets) {
    for(int i=0;i<packets.size();i++) {
      Packet p = packets.get(i);
      Controller c = getController(p.getSrcSid());
      if(c != null) {
        switch(p.getType()) {
          case "i01" : { c.handlePacket(p); break; }
          case "i02" : { spawnPlayer(c);  break; }
          case "i04" : { c.handlePacket(p); break; }
          case "i05" : { c.handlePacket(p); break; }
          case "i06" : { c.handlePacket(p); break; }
          case "i07" : { requestTeamChange(c); break; }
          default : { /* @FIXME ERROR REPORT */ break; }
        }
      }
      else {
        /* @FIXME */
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
  /* Table of different data structures that are generated --
      OBJ::CREATE   -  crt;<int oid>;<string type>;<vec2 pos>;<vec2 vel>;
      OBJ::DELETE   -  del;<int oid>;
  */
  private final List<GameObject> created, deleted;
  public void generateUpdatePackets() {
    final StringBuilder sb = new StringBuilder();
    for(int i=0;i<created.size();i++) {
      final GameObject obj = created.get(i);
      sb.append("crt"); sb.append(";");
      sb.append(obj.getOid()); sb.append(";");
      sb.append(obj.getType()); sb.append(";");
      obj.getPosition().toString(sb); sb.append(";");
      obj.getVelocity().toString(sb); sb.append(";");
    }
    for(int i=0;i<deleted.size();i++) {
      final GameObject obj = deleted.get(i);
      sb.append("del"); sb.append(";");
      sb.append(obj.getOid()); sb.append(";");
    }
    
    created.clear(); deleted.clear();
    
    final String globalData = sb.toString();
    for(int i=0;i<controllers.size();i++) {
      final StringBuilder sbc = new StringBuilder();
      sbc.append(globalData);
      controllers.get(i).generateUpdateData(sbc);
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

  protected abstract void spawnPlayer(final Controller c);
  
  public void join(final NoxioSession player) throws IOException {
    controllers.add(new Controller(this, player.getUser(), player.getSessionId()));
    for(int i=0;i<objects.size();i++) {
      GameObject obj = objects.get(i);
      generateJoinPacket(player);
    }
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
  
  public abstract void requestTeamChange(final Controller controller);
  
  public abstract void reportKill(final Controller killer, final GameObject killed);
  public abstract void reportObjective(final Controller player, final GameObject objective);
  public abstract void updateScore();
  
  /* Send a message that will appear in all players chatlog */
  public void sendMessage(final String msg) {
    lobby.sendPacket(new PacketG15(msg));
  }
  
  public void gameOver(String message) {
    lobby.sendPacket(new PacketG16(message));
    gameOver = true; resetTimer = 150;
  }
  
  public void close() {
    /* Do things! */
  }
  
  public boolean isGameOver() { return resetTimer < 1 && gameOver; }
  public final long createOid() { return idGen++; } /* @FIXME maybe use GUID instead... this could save bandwidth though... */
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
