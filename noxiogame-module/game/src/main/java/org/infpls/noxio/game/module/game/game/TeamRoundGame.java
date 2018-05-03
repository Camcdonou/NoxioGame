package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Queue;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public abstract class TeamRoundGame extends TeamGame {
  public static final int MIN_PLAYERS = 4, ROUND_START_TIMER = 300, ROUND_GRACE_PERIOD = 150;
  
  private boolean roundStarted, timerStarted;
  protected boolean graceOver;
  private int roundStartTimer, graceTimer;
  
  public TeamRoundGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings, int stw) throws IOException {
    super(lobby, map, settings, stw);
    
    roundStarted = false; timerStarted = false; graceOver = false;
    roundStartTimer = ROUND_START_TIMER; graceTimer = ROUND_GRACE_PERIOD;
  }
  
  @Override
  public void step() {
    super.step();
    roundCountdown();
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    
    final Controller c = getController(player.getSessionId());
    if(roundStarted && graceOver) { c.setRound("Waiting for end of round..."); }
    else if(roundStarted) { c.clearRound(); }
    else { c.setRound("..."); }
  }
  
  @Override
  protected boolean spawnPlayer(final Controller c, final Queue<String> q) {
    if(super.spawnPlayer(c, q)) { c.setRound("Waiting for end of round..."); return true; }
    else { return false; }
  }
  
  protected void roundCountdown() {
    if(!timerStarted) {
      if(MIN_PLAYERS-controllers.size() > 0) {
        for(int i=0;i<controllers.size();i++) {
          controllers.get(i).setRound("Waiting for players... (" + (MIN_PLAYERS-controllers.size()) + ")");
        }
      }
      else {
        timerStarted = true;
      }
      return;
    }
    
    if(timerStarted && !roundStarted) {
      for(int i=0;i<controllers.size();i++) {
        controllers.get(i).setRound("Round starting in " + new BigDecimal((float)roundStartTimer/30).setScale(2, BigDecimal.ROUND_HALF_UP) + "...");
      }
      if(roundStartTimer-- < 1) {
        for(int i=0;i<controllers.size();i++) {
          final Controller c = controllers.get(i);
          c.clearRound();
        }
        roundStarted = true;
      }
      return;
    }

    if(roundStarted && !graceOver) {
      if(graceTimer-- < 1) {
        for(int i=0;i<controllers.size();i++) {
          final Controller c = controllers.get(i);
          c.setRound("Waiting for end of round...");
        }
        graceOver = true;
      }
    }
  }
}
