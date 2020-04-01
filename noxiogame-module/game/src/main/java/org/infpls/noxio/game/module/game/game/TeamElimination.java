package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.dao.lobby.GameSettings;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class TeamElimination extends TeamRoundGame {
  
  public TeamElimination(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99), false);
  }
  
  @Override
  public void step() {
    super.step();
    gameState();
  }
  
  @Override
  protected boolean spawnPlayer(final Controller c, final Queue<String> q) {
    if(super.spawnPlayer(c, q)) { scores[c.getControlled().team]++; updateScore(); return true; }
    else { return false; }
  }
  
  private Controller topPlayer() {
    Controller top = controllers.get(0);
    for(int i=1;i<controllers.size();i++) {
      if(top.score.getKills() < controllers.get(i).score.getKills()) { top = controllers.get(i); }
    }
    return top;
  }
  
  /* Checks to see if the game is over: 'when the round is started, spawning is over, and only 1 or 0 players remain */
  /* Also checks if a team has 1 player remaining for Last Man Standing announcement */
  private boolean lmsBlu = false, lmsRed = false;
  private void gameState() {
    if(isGameOver()) { return; }
    if(graceOver) {
      int winner;
      if(scores[1] < 1) {
        final Controller top = topPlayer();
        gameOver("Red Team wins!", top.getMessageB(), top.getCustomSound());
        winner = 0;
      }
      else if(scores[0] < 1) {
        final Controller top = topPlayer();
        gameOver("Blue Team wins!", top.getMessageB(), top.getCustomSound());
        winner = 1;
      }
      else { return; }
      
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam() == winner) { controllers.get(i).score.win(); }
        else { controllers.get(i).score.lose(); }
      }
      if(controllers.size() >= 4) {
        Controller lblu = null, lred = null;
        int blu = 0, red = 0;
        for(int i=0;i<controllers.size();i++) {
          final Controller c = controllers.get(i);
          if(c.getTeam() == 0) { red++; lred = c; }
          else { blu++; lblu = c; }
        }
        if(!lmsBlu && blu == 1 && lblu != null) { lblu.announce("lms"); lmsBlu = true; }
        if(!lmsRed && red == 1 && lred != null) { lred.announce("lms"); lmsRed = true; }
      }
    }
    if(scores[0] < 1 && scores[1] < 1 && graceOver) {
      gameOver("Draw!", "You done goofed.", "");
    }
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    
    scores[killed.team]--;
    
    announceKill(killer, victim);

    updateScore();
    announceObjective();
  }
  

  
  @Override
  public void updateScore() {
    final StringBuilder sb = new StringBuilder();
    sb.append("scr;");
    for(int i=0;i<scores.length;i++) { sb.append(scores[i]); if(i<scores.length-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getControlled() == null || !controllers.get(i).getControlled().alive()) { sb.append("* "); }
      sb.append(controllers.get(i).getDisplay()); if(i<controllers.size()-1) { sb.append(","); }
    } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getTeam()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getKills()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getDeaths()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getObjectives()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    
    update.add(sb.toString());
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) { /* TeamElimination has no objectives. */ }

  @Override
  public void announceObjective() { /* @TODO last man standing */ }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    getController(player.getSessionId()).announce("tem");
  }
  
  @Override
  public String gametypeName() { return "Team Elimination"; }

  @Override
  public int objectiveBaseId() { return 0; }
}
