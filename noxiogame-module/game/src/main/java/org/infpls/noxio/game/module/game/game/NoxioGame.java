package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;

public abstract class NoxioGame {
  public final GameLobby lobby;
  
  private boolean gameOver;
  private int resetTimer;
  
  public final NoxioMap map;
  
  protected final List<Controller> controllers; /* Player controller objects */
  public final List<GameObject> objects; /* All game objects */
  
  private int idGen; /* Used to generate OIDs for objects. */
  public NoxioGame(final GameLobby lobby, final String mapName) throws IOException {
    this.lobby = lobby;
    
    map = new NoxioMap(mapName);
    
    controllers = new ArrayList();
    objects = new ArrayList();
    
    created = new ArrayList();
    deleted = new ArrayList();
    
    gameOver = false;
  }
    
  public void handlePackets(final List<Packet> packets) {
    for(int i=0;i<packets.size();i++) {
      Packet p = packets.get(i);
      Controller c = getController(p.getSrcSid());
      if(c != null) {
        switch(p.getType()) {
          case "i01" : { c.handlePacket(p); break; }
          case "i02" : { spawnPlayer((PacketI02)p);  break; }
          case "i04" : { c.handlePacket(p); break; }
          case "i05" : { c.handlePacket(p); break; }
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
      GameObject obj = objects.get(i);
      if(obj.isDead()) {
        deleteObject(obj); i--;
        obj.destroy();
        Controller c = getControllerByObject(obj);
        if(c != null) { c.objectDestroyed(); }
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
  
  private void generateJoinPacket(NoxioSession player) {
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
  private Controller getController(final String sid) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getSid().equals(sid)) {
        return controllers.get(i);
      }
    }
    return null;
  }
  
  /* Returns controller for given Object */
  private Controller getControllerByObject(final GameObject obj) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getControlled() == obj) {
        return controllers.get(i);
      }
    }
    return null;
  }

  private void spawnPlayer(PacketI02 p) {
    Controller c = getController(p.getSrcSid());
    if(c.getControlled() != null) { return; } /* Already controlling an object */
    
    long oid = createOid();
    Player player = new Player(this, oid, new Vec2(5.5f, 5.5f));
    addObject(player);
    c.setControl(player);
  }
  
  public void join(final NoxioSession player) throws IOException {
    controllers.add(new Controller(this, player.getUser(), player.getSessionId()));
    for(int i=0;i<objects.size();i++) {
      GameObject obj = objects.get(i);
      generateJoinPacket(player);
    }
  }
  
  public void leave(final NoxioSession player) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getSid().equals(player.getSessionId())) {
        controllers.get(i).destroy();
        controllers.remove(i);
        return;
      }
    }
  }
  
  public void gameOver() {
    lobby.sendPacket(new PacketG16());
    gameOver = true; resetTimer = 150;
  }
  
  public void close() {
    /* Do things! */
  }
  
  public boolean isGameOver() { return resetTimer < 1 && gameOver; }
  public final long createOid() { return idGen++; } /* @FIXME maybe use GUID instead... this could save bandwidth though... */
}
