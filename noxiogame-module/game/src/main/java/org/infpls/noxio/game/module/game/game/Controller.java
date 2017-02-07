package org.infpls.noxio.game.module.game.game;

import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;

public class Controller {
  private final NoxioGame game;
  private final String user, sid;
  private GameObject object;
  private Vec2 mouse;
  private Action action;
  private final Score score;
  public Controller(final NoxioGame game, final String user, final String sid) {
    this.game = game;
    this.user = user;
    this.sid = sid;
    this.mouse = new Vec2();
    this.score = new Score(user);
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
