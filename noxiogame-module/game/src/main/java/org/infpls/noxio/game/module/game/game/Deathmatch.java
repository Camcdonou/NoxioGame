package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Deathmatch extends NoxioGame {
  
  private int scoreToWin;
  
  public Deathmatch(final GameLobby lobby, final String mapName) throws IOException {
    super(lobby, mapName);
    
    scoreToWin = 20;
  }
  
  @Override
  protected void spawnPlayer(PacketI02 p) {
    Controller c = getController(p.getSrcSid());
    if(c.getControlled() != null) { return; } /* Already controlling an object */
    
    Vec2 sp;
    List<NoxioMap.Spawn> spawns = map.getSpawns("player");
    if(spawns.size() < 1) { sp = new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f); } // Fallback
    else {
      sp = spawns.get(0).getPos(); float d = averagePlayerDistance(sp);
      for(int i=1;i<spawns.size();i++) {
        float dn = averagePlayerDistance(spawns.get(i).getPos());
        if(dn > d) { sp = spawns.get(i).getPos(); }
      }
    }
    
    long oid = createOid();
    Player player = new Player(this, oid, sp);
    addObject(player);
    c.setControl(player);
  }
  
  /* Used to find the spawn point that is the "safest" to spawn at. */
  private float averagePlayerDistance(final Vec2 P) {
    float d = 0f; int j = 0;
    for(int i=0;i<controllers.size();i++) {
      GameObject obj = controllers.get(i).getControlled();
      if(obj != null) {
        d += obj.getPosition().distance(P);
        j++;
      }
    }
    return d/j;
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    final Controller victim = getControllerByObject(killed);
    if(killer != null && victim != null) {
      killer.getScore().kill();
      victim.getScore().death();
      lobby.sendPacket(new PacketG15(killer.getUser() + " killed " + victim.getUser() + "."));
      updateScore();
      if(killer.getScore().getKills() >= scoreToWin) { gameOver(killer.getUser() + " wins!"); }
    }
  }
  
  @Override
  public void updateScore() {
    final List<Score> scores = new ArrayList();
    for(int i = 0;i<controllers.size();i++) { scores.add(controllers.get(i).getScore()); }
    lobby.sendPacket(new PacketG14("Deathmatch", "First to " + scoreToWin + " kills wins!", scores, scoreToWin));
  }

  @Override
  public void reportObjective(Controller player, GameObject objective) {
    /* Deathmatch has no objective so this is ignored! */
  }
}
