package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.dao.lobby.GameSettings;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class Elimination extends SoloRoundGame {
  
  private final List<GameObject> alive; // Players still alive
  private final List<String> dead;      // Uses SID of player, ordered from first death to last death;
  public Elimination(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));
    
    alive = new ArrayList(); dead = new ArrayList();
  }
  
  @Override
  public void step() {
    super.step();
    gameState();
  }
  
  @Override
  protected boolean spawnPlayer(final Controller c, final Queue<String> q) {
    if(super.spawnPlayer(c, q)) { alive.add(c.getControlled()); updateScore(); return true; }
    else { return false; }
  }
  
  /* Checks to see if the game is over: 'when the round is started, spawning is over, and only 1 or 0 players remain */
  private void gameState() {
    if(isGameOver()) { return; }
    if(alive.size() == 1 && (graceOver || dead.size() >= controllers.size()-1)) {
      final Controller winner = getControllerByObject(alive.get(0));
      if(winner.score.getKills() >= dead.size()) { winner.announce("pf"); winner.score.perfect(); }
      gameOver(winner.getDisplay() + " wins!", winner.getMessageA(), winner.getCustomSound());
      final List<Controller> ordered = new ArrayList();
      ordered.add(winner);
      for(int i=dead.size()-1;i>=0;i--) {
        final Controller c = getController(dead.get(i));
        if(c != null) { ordered.add(c); }
      }
      final Controller[] cs = ordered.toArray(new Controller[0]);

      for(int i=0;i<cs.length;i++) {
        switch((int)(4f*(((float)i+1)/cs.length))) {
          case 1 : { cs[i].score.win(); break; }
          case 2 : { cs[i].score.neutral(); break; }
          case 3 : { cs[i].score.neutral(); break; }
          case 4 : { cs[i].score.lose(); break; }
        }
      }
    }
    if(alive.size() < 1 && graceOver) {
      gameOver("Draw!", "You done goofed.", "");
    }
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    
    alive.remove(killed);
    dead.add(victim.getSid());
    
    announceKill(killer, victim);

    updateScore();
    announceObjective();
  }
  
  @Override
  public void updateScore() {
    final StringBuilder sb = new StringBuilder();
    sb.append("scr;");
    sb.append(";");
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
  public void reportObjective(final Controller player, final GameObject objective) { /* Elimination has no objectives. */ }

  @Override
  public void announceObjective() { /* Nothing to announce in Elimination */ }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    getController(player.getSessionId()).announce("em");
  }
  
  @Override
  public String gametypeName() { return "Elimination"; }

  @Override
  public int objectiveBaseId() { return 0; }
}
