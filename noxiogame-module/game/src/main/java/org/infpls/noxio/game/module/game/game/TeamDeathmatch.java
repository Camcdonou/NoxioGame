package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class TeamDeathmatch extends TeamGame {
  
  public TeamDeathmatch(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));
  }
  
  private Controller topPlayer() {
    Controller top = controllers.get(0);
    for(int i=1;i<controllers.size();i++) {
      if(top.score.getKills() < controllers.get(i).score.getKills()) { top = controllers.get(i); }
    }
    return top;
  }
  
  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }
    final Controller victim = getControllerByObject(killed);
    
    if(announceKill(killer, victim)) {
      scores[killer.getTeam()==0?0:1]++;
    }
    
    updateScore();
    announceObjective();
        
    int winr;
    if(scores[0] >= scoreToWin) {
      final Controller top = topPlayer();
      gameOver("Red Team wins!", "MVP -> " + top.getDisplay() + " [CUSTOM WIN MESSAGE]", top.getCustomSound());
      winr = 0;
    }
    else if(scores[1] >= scoreToWin) {
      final Controller top = topPlayer();
      gameOver("Blue Team wins!", "MVP -> " + top.getDisplay() + " [CUSTOM WIN MESSAGE]", top.getCustomSound());
      winr = 1;
    }
    else { return; }
    if(scores[winr==0?1:0] == 0) {
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam() == winr) { controllers.get(i).announce("pf"); controllers.get(i).score.perfect(); }
        else { controllers.get(i).announce("hu"); controllers.get(i).score.humiliation(); }
      }
    }
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getTeam() == winr) { controllers.get(i).score.win(); }
      else { controllers.get(i).score.lose(); }
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
