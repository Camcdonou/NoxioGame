package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class TeamDeathmatch extends TeamGame {
  
  public TeamDeathmatch(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));
  }
  
  private boolean firstBlood = false; // Flag to check if first blood has been awarded or not!
  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory (BUGGED SEE FUNCTION)
    final Controller victim = getControllerByObject(killed);
    if(killer != null && victim != null && killer != victim) {
      if(!firstBlood) { announce("fb," + killer.getUser()); firstBlood = true; }
      if(killer.getTeam() != victim.getTeam()) { scores[killer.getTeam()==0?0:1]++; }
      announceKill(killer, victim);
    }
    else if(victim != null) { victim.getScore().death(); }
    updateScore();
    announceObjective();
    int winr;
    if(scores[0] >= scoreToWin) { gameOver("Red Team wins!"); winr = 0; }
    else if(scores[1] >= scoreToWin) { gameOver("Blue Team wins!"); winr = 1; }
    else { return; }
    if(scores[winr==0?1:0] == 0) {
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam() == winr) { controllers.get(i).announce("pf"); }
        else { controllers.get(i).announce("hu"); }
      }
    }
  }

  @Override
  public void reportObjective(final Controller player, final GameObject objective) { /* Deathmatch has no objective so this is ignored! */ }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { con.announce("tdm"); }
  }
  
  @Override
  public String gametypeName() { return "Team Deathmatch"; }
  
  @Override
  public int objectiveBaseId() { return 0; }
  
  @Override
  public int getScoreToWin() { return scoreToWin; }
}
