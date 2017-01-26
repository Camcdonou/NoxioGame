package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class NoxioGame {
  private final List<Controller> controllers; /* Player controller objects */
  public final List<GameObject> objects; /* All game objects */
  
  private int idGen; /* Used to generate OIDs for objects. */
  public NoxioGame() {
    controllers = new ArrayList();
    objects = new ArrayList();
  }
  
  public List<Packet> handlePackets(final List<Packet> packets) {
    List<Packet> updates = new ArrayList();
    for(int i=0;i<packets.size();i++) {
      Packet p = packets.get(i);
      Controller c = getController(p.getSrcSid());
      switch(p.getType()) {
        case "i00" : { if(c != null) { c.handlePacket(p); } break; }
        case "i01" : { if(c != null) { c.handlePacket(p); } break; }
        case "i02" : { spawnPlayer((PacketI02)p, updates);  break; }
        case "i10" : { if(c != null) { c.handlePacket(p); } break; }
        default : { /* @FIXME ERROR REPORT */ break; }
      }
    }
    return updates;
  }
  
  public void addObject(final GameObject obj) {
    objects.add(obj);
  }
  
  private void spawnPlayer(PacketI02 p, List<Packet> updates) {
    Controller c = getController(p.getSrcSid());
    if(c.getControlled() != null) { return; } /* Already controlling an object */
    
    long oid = createOid();
    Player player = new Player(this, oid, new Vec2((float)(Math.random()*500.0), (float)(Math.random()*350.0)));
    addObject(player);
    updates.add(new PacketG10(oid, player.getType(), player.getPosition(), player.getVelocity()));
    updates.add(c.setControl(player));
  }
  
  public List<Packet> step() {
    final List<Packet> updates = new ArrayList();
    for(int i=0;i<controllers.size();i++) {
      controllers.get(i).step();
    }
    for(int i=0;i<objects.size();i++) {
      GameObject obj = objects.get(i);
      if(obj.isDead()) {
        objects.remove(i--);
        obj.destroy();
        Controller c = getControllerByObject(obj);
        if(c != null) { updates.add(c.objectDestroyed()); }
        updates.add(new PacketG11(obj.getOid()));
      }
      else { obj.step(updates); }
    }
    return updates;
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
  
  public void join(final NoxioSession player) throws IOException {
    controllers.add(new Controller(player.getSessionId()));
    for(int i=0;i<objects.size();i++) { /* @FIXME potential for desynch or bad multi threading here */
      GameObject obj = objects.get(i);
      player.sendPacket(new PacketG10(obj.getOid(), obj.getType(), obj.getPosition(), obj.getVelocity()));
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
  
  public long createOid() { return idGen++; } /* @FIXME maybe use GUID instead... this could save bandwidth though... */
}
