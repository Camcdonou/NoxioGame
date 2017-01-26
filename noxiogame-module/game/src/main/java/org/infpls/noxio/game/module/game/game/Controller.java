package org.infpls.noxio.game.module.game.game;

import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Controller {
  private final String sid;
  private GameObject object;
  private Vec2 mouse;
  private Action action;
  public Controller(final String sid) {
    this.sid = sid;
    this.mouse = new Vec2();
  }
  
  public void handlePacket(Packet p) {
    switch(p.getType()) {
      case "i00" : { mouse = ((PacketI00)p).getPos();  break; }
      case "i01" : { mouse = null; break; }
      case "i10" : { PacketI10 pp = (PacketI10)p; action = new Action(pp.getAction(), pp.getPos()); break; }
      default : { /* @FIXME ERROR REPORT */ break; }
    }
  }
  
  public void step() {
    /* @FIXME this is not a good way to setup how controls work */
    if(object != null && mouse != null) {
      if(object.getType().equals("obj.player")) {
        Player p = (Player)object;
        p.move(mouse.subtract(p.getPosition()).normalize());
      }
    }
    if(object != null && action != null) {
      if(object.getType().equals("obj.player")) {
        Player p = (Player)object;
        p.setAction(action);
      }
      action = null;
    }
  }
  
  public Packet setControl(GameObject object) {
    this.object = object;
    return new PacketI03(object.getOid()).setSrcSid(sid);
  }
  
  public Packet objectDestroyed() {
    this.object = null;
    return new PacketI03(-1).setSrcSid(sid);
  }
  
  public void destroy() {
    if(this.object != null) {
      this.object.kill();
    }
  }
  
  public String getSid() { return sid; }
  public GameObject getControlled() { return object; }
}
