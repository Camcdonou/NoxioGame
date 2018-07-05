package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class BombingRun extends TeamRoundGame {
  public static final int ROUND_TIME_LIMIT =120*30, ROUND_END_TIME_LIMIT = 150;
  public static final int ROUND_LIMIT = 3;

  private final Vec2[] spawns;
  private Bomb bomb;
  private BombZone zone;
  
  private boolean side, roundOver;
  private int round, roundTimer, roundEndTimer;
  public BombingRun(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 3, 1, 25), true);
    
    side = false;
    roundTimer = 0; round = 0;
    spawns = new Vec2[2];
    
    List<NoxioMap.Spawn> rs = map.getSpawns("bomb", gametypeId(), 0);
    List<NoxioMap.Spawn> bs = map.getSpawns("bomb", gametypeId(), 1);

    spawns[0] = rs.isEmpty()?new Vec2((map.getBounds()[0]*0.5f)+1f, map.getBounds()[1]*0.5f):rs.get(0).getPos();
    spawns[1] = bs.isEmpty()?new Vec2((map.getBounds()[0]*0.5f)-1f, map.getBounds()[1]*0.5f):bs.get(0).getPos();
    
    switchSides();
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller c = getController(player.getSessionId());
    if(c != null && roundStarted) {
      c.setClientTimer((side?0:1)==c.getTeam()?"Offense":"Defense", roundTimer);
      c.announce("ctf");
    }
  }
  
  @Override
  public void requestTeamChange(final Controller c, final Queue<String> q) {
    super.requestTeamChange(c, q);
    if(roundStarted) {
      c.setClientTimer((side?0:1)==c.getTeam()?"Offense":"Defense", roundTimer);
    }
    else {
      c.setClientTimer("", -1);
    }
  }
  
  @Override
  public void step() {
    if(!isGameOver() && roundStarted && !roundOver && roundTimer-- <= 0) { endRound(); }
    if(!isGameOver() && roundOver && roundEndTimer-- <= 0) { switchSides(); }
    
    super.step();
  }
  
  @Override
  protected void roundStart() {
    super.roundStart();
    setClientTimerTeam(side?0:1, "Offense", roundTimer);
    setClientTimerTeam(side?1:0, "Defense", roundTimer);
  }
  
  private void endRound() {
    roundOver = true;
    
    if((round & 1) == 0) { // Only end game on even round so both teams get equal number of rounds
      if(scores[0] >= scoreToWin || scores[1] >= scoreToWin || round/2 >= ROUND_LIMIT) {
        final Controller top = topPlayer();
        if(scores[0] > scores[1]) {
          gameOver("Red Team wins!", top.getMessageB(), top.getCustomSound());
          for(int i=0;i<controllers.size();i++) {
            if(controllers.get(i).getTeam() == 0) { controllers.get(i).score.win(); }
            else { controllers.get(i).score.lose(); }
          }
        }
        else if(scores[1] > scores[1]) {
          gameOver("Blue Team wins!", top.getMessageB(), top.getCustomSound());
          for(int i=0;i<controllers.size();i++) {
            if(controllers.get(i).getTeam() == 0) { controllers.get(i).score.win(); }
            else { controllers.get(i).score.lose(); }
          }
        }
        else {
          gameOver("Draw!", top.getMessageB(), top.getCustomSound());
          for(int i=0;i<controllers.size();i++) {
            controllers.get(i).score.neutral();
          }
        }
        return;
      }
    }
    
    roundEndTimer = ROUND_END_TIME_LIMIT;
    /* debug */ sendMessage("Round Over");
  }
  
  private void switchSides() {
    side = !side;
    int offense = side?0:1; int defense = side?1:0;
    roundTimer = ROUND_TIME_LIMIT;
    roundOver = false; roundEndTimer = 0;
    round++;
    
    newRound();
    setClientTimer("", -1);
    if(bomb != null && zone != null) { bomb.destroyx(); zone.destroyx(); }
    
    bomb = new Bomb(this, createOid(), spawns[offense], offense);
    zone = new BombZone(this, createOid(), spawns[defense], defense, new Vec2(2f, 2f));
    addObject(bomb);
    addObject(zone);
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    
    announceKill(killer, victim);
    
    updateScore();
  }
  
  private Controller topPlayer() {
    Controller top = controllers.get(0);
    for(int i=1;i<controllers.size();i++) {
      if(top.score.getStats().credits < controllers.get(i).score.getStats().credits) { top = controllers.get(i); }
    }
    return top;
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    if(isGameOver() || roundOver) { return; }
    if(objective.team==0) { scores[0]++; announce("rs"); }
    else { scores[1]++; announce("bs"); }
    if(player != null) { player.score.bomb(); }
    updateScore();
    announceObjective();
    
    endRound();
  }
  
  @Override
  public String gametypeName() { return "Bomb"; }
  
  @Override
  public int objectiveBaseId() { return 1; }
}
