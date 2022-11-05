package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class SportsBall extends TeamGame {

  public SportsBall(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 3, 1, 25));
    
    createObjectives();
  }
  
  public final void createObjectives() {
    List<NoxioMap.Spawn> ballSp = map.getSpawns("ball", gametypeId(), 0);
    List<NoxioMap.Spawn> redSp = map.getSpawns("goal", gametypeId(), 0);
    List<NoxioMap.Spawn> blueSp = map.getSpawns("goal", gametypeId(), 1);
    final Vec2 center = new Vec2((map.getBounds()[0]*0.5f)+1f, map.getBounds()[1]*0.5f);
    final Vec2 ballP = ballSp.isEmpty()?center:ballSp.get(0).getPos();
    final Vec2 redP = redSp.isEmpty()?center.add(new Vec2(5,0)):redSp.get(0).getPos();
    final Vec2 blueP = blueSp.isEmpty()?center.add(new Vec2(-5,0)):blueSp.get(0).getPos();
    
    final Ball ball = new Ball(this, createOid(), ballP, 0);
    final GoalZone red = new GoalZone(this, createOid(), redP, 0, new Vec2(3,3));
    final GoalZone blue = new GoalZone(this, createOid(), blueP, 1, new Vec2(3,3));
    
    addObject(ball);
    addObject(red);
    addObject(blue);
  }
  
  @Override
  public void step() {
    super.step();
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
    if(isGameOver()) { return; }
    
    scores[player.getTeam()]++;
    announceTeam(player.getTeam(), "fc");
    announceTeam(player.getTeam()==0?1:0, "fl");

    player.score.flagCapture();
    updateScore();
    announceObjective();
    
    int winr;
    if(scores[0] >= scoreToWin) {
      final Controller top = topPlayer();
      gameOver("Red Team wins!", top.getMessageB(), top.getCustomSound());
      winr = 0;
    }
    else if(scores[1] >= scoreToWin) {
      final Controller top = topPlayer();
      gameOver("Blue Team wins!", top.getMessageB(), top.getCustomSound());
      winr = 1;
    }
    else { return; }
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { con.announce("ctf"); }
  }
  
  @Override
  public String gametypeName() { return "Sports Ball"; }
  
  @Override
  public int objectiveBaseId() { return 1; }
}
