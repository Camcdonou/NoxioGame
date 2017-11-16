package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.dao.lobby.GameSettings;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class Deathmatch extends NoxioGame {
  
  private final int scoreToWin;
  
  public Deathmatch(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings);
    
    scoreToWin = settings.get("score_to_win", 15);
  }
  
  @Override
  protected void spawnPlayer(final Controller c, final Queue<String> q) {
    if(c.getControlled() != null || !c.respawnReady()) { return; } /* Already controlling an object */
    
    Vec2 sp;
    final List<NoxioMap.Spawn> spawns = map.getSpawns("player");
    if(spawns.isEmpty()) { sp = new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f); } // Fallback
    else {
      final List<NoxioMap.Spawn> crop = new ArrayList();
      for(int i=0;i<spawns.size();i++) {
        if(nearestPlayerDistance(spawns.get(i).getPos()) >= SPAWN_SAFE_RADIUS) {
          crop.add(spawns.get(i));
        }
      }
      if(crop.isEmpty()) {
        sp = spawns.get((int)(Math.random()*spawns.size())).getPos();
      }
      else {
        sp = crop.get((int)(Math.random()*crop.size())).getPos();
      }
    }
    
    int oid = createOid();
    Player player = makePlayerObject(q.remove(), sp);
    addObject(player);
    c.setControl(player);
  }
  
  /* Used to find the spawn point that is the "safest" to spawn at. */
  private float nearestPlayerDistance(final Vec2 P) {
    float d = 0f;
    for(int i=0;i<controllers.size();i++) {
      GameObject obj = controllers.get(i).getControlled();
      if(obj != null) {
        float k = obj.getPosition().distance(P);
        if(k < d) { d = k; }
      }
    }
    return d;
  }
  
  @Override
  public void requestTeamChange(final Controller c, final Queue<String> q) {
    /* No teams, ignore */
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    if(killer != null && victim != null) {
      killer.getScore().kill();
      victim.getScore().death();
      sendMessage(killer.getUser() + " killed " + victim.getUser() + ".");
      updateScore();
      if(killer.getScore().getKills() >= scoreToWin) { gameOver(killer.getUser() + " wins!"); }
    }
  }
  
  @Override
  public void updateScore() {
    final StringBuilder sb = new StringBuilder();
    sb.append("scr;");
    sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getUser()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getTeam()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getScore().getKills()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getScore().getDeaths()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getScore().getObjectives()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    
    update.add(sb.toString());
  }

  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    /* Deathmatch has no objective so this is ignored! */
  }
  
  @Override
  public String gametypeName() { return "Deathmatch"; }

  @Override
  public int objectiveBaseId() { return 0; }
}
