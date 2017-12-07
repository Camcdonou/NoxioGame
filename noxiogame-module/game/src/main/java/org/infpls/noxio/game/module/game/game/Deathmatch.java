package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.dao.lobby.GameSettings;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class Deathmatch extends SoloGame {
    
  public Deathmatch(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 15));
  }

  private boolean firstBlood = false; // Flag to check if first blood has been awarded or not!
  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    if(killer != null && victim != null && killer != victim) {
      if(!firstBlood) { announce("fb," + killer.getUser()); firstBlood = true; }
      announceKill(killer, victim);
      if(killer.getScore().getKills() >= scoreToWin) {
        gameOver(killer.getUser() + " wins!");
        if(killer.getScore().getDeaths() < 1) { killer.announce("pf"); }
      }
    }
    else if(victim != null) { victim.getScore().death(); }
    updateScore();
    announceObjective();
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) { /* Deathmatch has no objectives. */ }

  private Controller lead = null;     // Points to player who was in the lead last time we announced objective. If this changes we inform players.
  @Override
  public void announceObjective() {
    if(controllers.size() < 1) { return; } // No players
    Controller newLead = controllers.get(0);
    for(int i=1;i<controllers.size();i++) {
      if(controllers.get(i).getScore().getKills() > newLead.getScore().getKills()) {
        newLead = controllers.get(i);
      }
    }
    if(lead == null || newLead.getScore().getKills() > lead.getScore().getKills() && newLead.getScore().getKills() < scoreToWin) {
      if(lead != null) { lead.announce("ll"); newLead.announce("gl"); }
      if(newLead.getScore().getKills() > 0) { lead = newLead; }
    }
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    getController(player.getSessionId()).announce("dm");
  }
  
  @Override
  public String gametypeName() { return "Deathmatch"; }

  @Override
  public int objectiveBaseId() { return 0; }
}
