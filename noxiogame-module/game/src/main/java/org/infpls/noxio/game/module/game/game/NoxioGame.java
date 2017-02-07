package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;

public class NoxioGame {
  public final GameLobby lobby;
  
  private final int scoreToWin;
  private boolean gameOver;
  private int resetTimer;
  
  private final List<Controller> controllers; /* Player controller objects */
  public final List<GameObject> objects; /* All game objects */
  
  private int idGen; /* Used to generate OIDs for objects. */
  public NoxioGame(final GameLobby lobby) {
    this.lobby = lobby;
    
    controllers = new ArrayList();
    objects = new ArrayList();
    
    gameOver = false;
    scoreToWin = 10; /* @FIXME Ayy Lmao */
  }
  
  public void handlePackets(final List<Packet> packets) {
    for(int i=0;i<packets.size();i++) {
      Packet p = packets.get(i);
      Controller c = getController(p.getSrcSid());
      if(c != null) {
        switch(p.getType()) {
          case "i00" : { c.handlePacket(p); break; }
          case "i01" : { c.handlePacket(p); break; }
          case "i02" : { spawnPlayer((PacketI02)p);  break; }
          case "i10" : { c.handlePacket(p); break; }
          default : { /* @FIXME ERROR REPORT */ break; }
        }
      }
      else {
        /* @FIXME */
      }
    }
  }
  
  public void addObject(final GameObject obj) {
    objects.add(obj);
  }
  
  private void spawnPlayer(PacketI02 p) {
    Controller c = getController(p.getSrcSid());
    if(c.getControlled() != null) { return; } /* Already controlling an object */
    
    long oid = createOid();
    Player player = new Player(this, oid, new Vec2((float)(Math.random()*500.0), (float)(Math.random()*350.0)));
    addObject(player);
    lobby.sendPacket(new PacketG10(oid, player.getType(), player.getPosition(), player.getVelocity()));
    c.setControl(player);
  }
  
  public void step() {
    if(!gameOver) {
      for(int i=0;i<controllers.size();i++) {
        final Controller cont = controllers.get(i);
        cont.step();
        lobby.sendPacket(new PacketG14(cont.getScore())); /* @FIXME this is such fucking cancer omfg */
        if(cont.getScore().getKills() >= scoreToWin) {
          lobby.sendPacket(new PacketG16(cont.getUser()));
          gameOver = true; resetTimer = 150;
        }
      }
    }
    else {
     if(resetTimer > 0) { resetTimer--; }
    }
    benchmarkBlaster(); /* @FIXME DEBUG */
    for(int i=0;i<objects.size();i++) {
      GameObject obj = objects.get(i);
      if(obj.isDead()) {
        objects.remove(i--);
        obj.destroy();
        Controller c = getControllerByObject(obj);
        if(c != null) { c.objectDestroyed(); }
        lobby.sendPacket(new PacketG11(obj.getOid()));
      }
      else { obj.step(); }
    }
  }
  
  /* Benchmark Blaster */
  private int bmbRot = 0; private boolean alt = false;
  private void benchmarkBlaster() {
    if(alt=!alt) { return; }
    Vec2 pos = new Vec2(500,300);
    Vec2 dir = new Vec2((float)Math.sin(bmbRot*0.05), (float)Math.cos(bmbRot*0.05));
    Vec2 rand = new Vec2((float)Math.sin(bmbRot*-0.3174), (float)Math.cos(bmbRot*-0.3174));
    
    Bullet b;
    if(lobby.getInfo().getName().equals("BenchmarkA")) {
      b = new Bullet(this, createOid(), pos.copy(), dir.scale(10.0f), null);
      addObject(b);
      lobby.sendPacket(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    if(lobby.getInfo().getName().equals("BenchmarkA") || lobby.getInfo().getName().equals("BenchmarkB")) {
      b = new Bullet(this, createOid(), pos.copy(), dir.inverse().scale(10.0f), null);
      addObject(b);
      lobby.sendPacket(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    if(lobby.getInfo().getName().equals("BenchmarkA") || lobby.getInfo().getName().equals("BenchmarkB") || lobby.getInfo().getName().equals("BenchmarkC")) {
      b = new Bullet(this, createOid(), pos.copy(), rand.scale(6.5f), null);
      addObject(b);
      lobby.sendPacket(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    if(lobby.getInfo().getName().equals("BenchmarkD")) {
      for(int i=0;i<4;i++) {
        Vec2 random = new Vec2((float)(Math.random()*2)-1.0f, (float)(Math.random()*2)-1.0f);
        b = new Bullet(this, createOid(), pos.copy(), random.normalize().scale(8.5f), null);
        addObject(b);
        lobby.sendPacket(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
      }
    }
    if(lobby.getInfo().getName().equals("BenchmarkE")) {
      Vec2 vert = new Vec2(1.0f, 0);
      Vec2 horiz = new Vec2(0, 1.0f);
      
      int i = bmbRot % 40;
      b = bmbRot % 2  == 1 ?
              new Bullet(this, createOid(), new Vec2(i*25.0f, 0), horiz.normalize().scale(10.0f), null) :
              new Bullet(this, createOid(), new Vec2(0, i*25.0f), vert.normalize().scale(10.0f), null) ;
      addObject(b);
      lobby.sendPacket(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
      
      i = bmbRot % 60;
      b = bmbRot % 2  == 1 ?
              new Bullet(this, createOid(), new Vec2(i*10.0f, 0), horiz.normalize().scale(10.0f), null) :
              new Bullet(this, createOid(), new Vec2(0, i*10.0f), vert.normalize().scale(10.0f), null) ;
      addObject(b);
      lobby.sendPacket(new PacketG10(b.getOid(), b.getType(), b.getPosition(), b.getVelocity()));
    }
    bmbRot++;
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
  
  public void reportKill(GameObject killer, GameObject killed) {
    Controller killerCont = getControllerByObject(killer);
    Controller killedCont = getControllerByObject(killed);
    if(killerCont != null && killedCont != null) {
      if(killerCont == killedCont) { killedCont.getScore().death(); lobby.sendPacket(new PacketG15(killerCont.getUser() + " committed suicide.")); }
      else { killedCont.getScore().death(); killerCont.getScore().kill(); lobby.sendPacket(new PacketG15(killerCont.getUser() + " killed " + killedCont.getUser() + ".")); }
    }
  }
  
  public void join(final NoxioSession player) throws IOException {
    controllers.add(new Controller(this, player.getUser(), player.getSessionId()));
    lobby.sendPacket(new PacketG18(scoreToWin), player);
    for(int i=0;i<objects.size();i++) {
      GameObject obj = objects.get(i);
      lobby.sendPacket(new PacketG10(obj.getOid(), obj.getType(), obj.getPosition(), obj.getVelocity()), player);
    }
    for(int i=0;i<controllers.size();i++) {
      lobby.sendPacket(new PacketG14(controllers.get(i).getScore()), player);
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
  
  public void close() {
    /* Do things! */
  }
  
  public boolean isGameOver() { return resetTimer < 1 && gameOver; }
  public long createOid() { return idGen++; } /* @FIXME maybe use GUID instead... this could save bandwidth though... */
}
