package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;

public class Telenter extends GameObject {
  private static final float RADIUS = .1f, TELEPORT_SPEED = .75f;
  
  private boolean fxUpd; // When true we send a trigger for an effect to play on this object next frame
  
  private final Telexit exit;
  private final List<Player> teleporting;
  
  public Telenter(final NoxioGame game, final int oid, final Vec2 position, final Telexit ex) {
    super(game, oid, position, 0);
    /* Bitmask Type */
    bitIs = bitIs | GameObject.Types.MAPOBJ;
    
    exit = ex;
    teleporting = new ArrayList();
    fxUpd = false;
  }
    
  @Override
  public void step() {
    teleporting();
    
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      final Controller con = game.getControllerByObject(obj);
      if(con != null && obj.is(GameObject.Types.PLAYER) && !(obj instanceof Poly)) {
        final Player ply = (Player)obj;
        if(ply.position.distance(position) < ply.radius + RADIUS && ply.height < 0.5f && ply.height > -0.5f) {
          teleport(ply);
        }
      }
    }
  }
  
  /* Begin a teleoportation */
  private void teleport(Player ply) {
    ply.setHeight(-1000f);  // Setting the players height to 100 to make them 'invisible'. Janky hack but it works
    ply.impact(1);        // We impact the player every frame during teleportation to remove their control for the duration
    teleporting.add(ply);
    fx();
  }
  
  /* Progress all teleportations */
  private void teleporting() {
    for(int i=0;i<teleporting.size();i++) {
      Player ply = teleporting.get(i);
      if(ply.position.distance(exit.position) > TELEPORT_SPEED) {
        final Vec2 dir = exit.position.subtract(ply.position).normalize();
        final Vec2 move = dir.scale(TELEPORT_SPEED);
        ply.setPosition(ply.position.add(move));
        ply.setHeight(-1000f);
        ply.impact(1);
      }
      else {
        ply.setPosition(exit.position);
        ply.setHeight(0f);
        exit.fx();
        teleporting.remove(i--);
      }
    }
  }
  
  /* Play visual effect */
  public void fx() {
    fxUpd = true;
  }
  
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<int team>;<vec2 pos>;<vec2 size>;
  */
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    sb.append(fxUpd?1:0);
    sb.append(";");
    fxUpd = false;
  }
  
  @Override
  public boolean isGlobal() { return true; }
  
  @Override
  public void kill() { }
  
  @Override
  public void destroyx() { }
  
  @Override
  public String type() { return "teln"; }
}
