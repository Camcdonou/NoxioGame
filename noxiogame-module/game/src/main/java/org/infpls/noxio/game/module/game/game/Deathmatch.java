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
  public void requestTeamChange(final Controller controller) {
    /* No teams, ignore */
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
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
    final List<ScoreBoard> scores = new ArrayList();
    for(int i = 0;i<controllers.size();i++) {
      final Controller c = controllers.get(i);
      final Score s = controllers.get(i).getScore();
      scores.add(
        new ScoreBoard(c.getUser(), s.getKills() + "/" + s.getDeaths(), (float)s.getKills()/scoreToWin, new Color3())
      );
    }
    lobby.sendPacket(new PacketG14("Deathmatch", "First to " + scoreToWin + " kills wins!", scores));
  }

  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    /* Deathmatch has no objective so this is ignored! */
  }
  
  @Override
  public String gametypeName() { return "Deathmatch"; }
}
