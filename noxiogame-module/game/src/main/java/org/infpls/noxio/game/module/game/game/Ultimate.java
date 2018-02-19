package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.Queue;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class Ultimate extends SoloGame {

  private GameObject ultimate;
  private final int scoreTimeAdjust;
  private int scoreTimer;
  
  public Ultimate(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));
    
    scoreTimeAdjust = settings.get("score_time_adjust", 120, 30, 300);
    
    scoreTimer = 0;
    ultimate = null;
  }
  
  /* Whoever happens to be alive */
  private void makeUltimate() {
    for(int i=0;i<controllers.size();i++) {
      final GameObject obj = controllers.get(i).getControlled();
      if(obj != null && !obj.isDead()) {
        makeUltimate(controllers.get(i), obj);
        return;
      }
    }
  }
  
  /* Specific player */
  private void makeUltimate(final Controller player, final GameObject obj) {
    if(controllers.size() < 2) { return; } /* Don't make an Ultimate Lifeform until there is more than 1 player in the game. */
    player.announce("hc");
    scoreTimer = 0;
    ultimate = obj;
  }
  
  @Override
  public void step() {
    super.step();
    
    if(ultimate != null && !ultimate.isDead()) {
      if(scoreTimer++ > scoreTimeAdjust) {
        scoreTimer = 0;
        final Controller controller = getControllerByObject(ultimate);
        if(controller != null) { reportObjective(controller, ultimate); }
      }
      if(ultimate.is(GameObject.Types.PLAYER)) {
        ((Player)ultimate).ultimate();
      }
    }
  }
  
  @Override
  protected void spawnPlayer(final Controller c, final Queue<String> q) {
    super.spawnPlayer(c, q);
    if((ultimate == null || ultimate.isDead()) && c.getControlled() != null) {
      makeUltimate(c, c.getControlled());
    }
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    if(killer != null && victim != null && killer != victim) {
      announceKill(killer, victim);
        final GameObject obj = killer.getControlled();
      if(killed == ultimate) {
        if(obj != null && !obj.isDead()) { makeUltimate(killer, obj); reportObjective(killer, obj); }
        else { makeUltimate(); }
      }
      else if(obj == ultimate) {
        if(obj != null && !obj.isDead()) { reportObjective(killer, obj); }
      }
    }
    else if(victim != null) {
      victim.getScore().death();
      if(killed == ultimate) { makeUltimate(); }
    }
    updateScore();
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    if(isGameOver()) { return; }                              // Prevents post game scores causing a double victory @TODO: doesnt work, look at actual value instead.
    player.getScore().objective();
    updateScore();
    announceObjective();
    if(player.getScore().getObjectives() >= scoreToWin) {
      gameOver(player.getUser() + " wins!");
      if(player.getScore().getDeaths() < 1) { player.announce("pf"); }
    }
  }
  
  private Controller lead = null;     // Points to player who was in the lead last time we announced objective. If this changes we inform players.
  @Override
  public void announceObjective() {
    if(controllers.size() < 1) { return; } // No players
    Controller newLead = controllers.get(0);
    for(int i=1;i<controllers.size();i++) {
      if(controllers.get(i).getScore().getObjectives() > newLead.getScore().getObjectives()) {
        newLead = controllers.get(i);
      }
    }
    if(lead == null || newLead.getScore().getObjectives() > lead.getScore().getObjectives() && newLead.getScore().getObjectives() < scoreToWin) {
      if(lead != null) { lead.announce("ll"); newLead.announce("gl"); }
      if(newLead.getScore().getObjectives() > 0) { lead = newLead; }
    }
  }  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { con.announce("ulf"); }
  }
  
  @Override
  public String gametypeName() { return "Ultimate Lifeform"; }
  
  @Override
  public int objectiveBaseId() { return 3; }
  
  @Override
  public int getScoreToWin() { return scoreToWin; }
}
