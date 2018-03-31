package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.dao.lobby.GameSettings;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class Deathmatch extends SoloGame {
    
  public Deathmatch(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 15, 1, 99));
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);

    if(announceKill(killer, victim)) {
      if(killer.score.getKills() >= scoreToWin) {
        if(killer.score.getDeaths() < 1) { killer.announce("pf"); killer.score.perfect(); }
        gameOver(killer.getUser() + " wins!", "[CUSTOM WIN MESSAGE]", killer.getCustomSound());

        final boolean bsort = true;
        final Controller[] cs = controllers.toArray(new Controller[0]);
        for(int i=0;i<cs.length-1;i++) {
          if(cs[i].score.getKills() < cs[i+1].score.getKills()) {
            final Controller swp = cs[i];
            cs[i] = cs[i+1]; cs[i+1] = swp;
          }
          if(i>=cs.length-1 && !bsort) { i=0; }
        }

        for(int i=0;i<cs.length;i++) {
          switch((int)(4f*(((float)i+1)/cs.length))) {
            case 1 : { cs[i].score.win(); break; }
            case 2 : { cs[i].score.neutral(); break; }
            case 3 : { cs[i].score.neutral(); break; }
            case 4 : { cs[i].score.lose(); break; }
          }
        }
      }
    }
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
      if(controllers.get(i).score.getKills() > newLead.score.getKills()) {
        newLead = controllers.get(i);
      }
    }
    if(lead == null || newLead.score.getKills() > lead.score.getKills() && newLead.score.getKills() < scoreToWin) {
      if(lead != null) { lead.announce("ll"); newLead.announce("gl"); }
      if(newLead.score.getKills() > 0) { lead = newLead; }
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
  
  @Override
  public int getScoreToWin() { return scoreToWin; }
}
