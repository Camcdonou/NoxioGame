package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Queue;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.GameObject;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public abstract class TeamRoundGame extends TeamGame {
  public static final int ROUND_GRACE_PERIOD = 150;
  
  private final int minPlayers, roundStartTime;
  
  private boolean timerStarted;
  protected boolean roundStarted, graceOver;
  private int roundStartTimer, graceTimer;
  private final boolean allowRespawn; // Allow respawn during the round or only at the start of each round.
  
  public TeamRoundGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings, int stw, boolean ar) throws IOException {
    super(lobby, map, settings, stw);
    
    minPlayers = settings.get("min_players", 4, 2, lobby.maxPlayers);
    roundStartTime = settings.get("round_start_time", 300, 90, 900);
    
    allowRespawn = ar;
    newRound();
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
    if(roundStarted && graceOver) {
      if(allowRespawn) { c.clearRound(); }
      else { c.setRound("Waiting for end of round..."); }
    }
    else if(roundStarted) { c.clearRound(); }
    else { c.setRound("..."); }
  }
  
  @Override
  protected boolean spawnPlayer(final Controller c, final Queue<String> q) {
    if(super.spawnPlayer(c, q)) {
      if(allowRespawn) { c.clearRound(); }
      else { c.setRound("Waiting for end of round..."); }
      return true;
    }
    else { return false; }
  }
  
  protected final void newRound() {
    roundStarted = false; timerStarted = false; graceOver = false;
    roundStartTimer = roundStartTime; graceTimer = ROUND_GRACE_PERIOD;
    for(int i=0;i<objects.size();i++) {
      final GameObject obj = objects.get(i);
      if(obj.is(GameObject.Types.PLAYER)) {
        obj.destroyx();
      }
    }
  }
  
  protected void roundCountdown() {
    if(!timerStarted) {
      if(minPlayers-controllers.size() > 0) {
        for(int i=0;i<controllers.size();i++) {
          controllers.get(i).setRound("Waiting for players... (" + (minPlayers-controllers.size()) + ")");
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
        roundStart();
      }
      return;
    }

    if(roundStarted && !graceOver) {
      if(graceTimer-- < 1) {
        for(int i=0;i<controllers.size();i++) {
          final Controller c = controllers.get(i);
          if(allowRespawn) { c.clearRound(); }
          else { c.setRound("Waiting for end of round..."); }
        }
        graceOver = true;
      }
    }
  }
  
  protected void roundStart() {
    for(int i=0;i<controllers.size();i++) {
      final Controller c = controllers.get(i);
      c.clearRound();
    }
    roundStarted = true;
  }
}
