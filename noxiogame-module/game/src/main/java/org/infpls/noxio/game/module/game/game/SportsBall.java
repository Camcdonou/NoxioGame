package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class SportsBall extends TeamGame {

  public SportsBall(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 5, 1, 25));
    
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
    
    List<NoxioMap.Spawn> fieldSpA = map.getSpawns("field", gametypeId(), 0);
    List<NoxioMap.Spawn> fieldSpB = map.getSpawns("field", gametypeId(), 1);
    final Vec2 fieldPA = fieldSpA.isEmpty()?new Vec2(0,0):fieldSpA.get(0).getPos();
    final Vec2 fieldPB = fieldSpB.isEmpty()?new Vec2(256,256):fieldSpB.get(0).getPos();
    
    final Vec2[] v = new Vec2[] {
      fieldPA,
      new Vec2(fieldPA.x, fieldPB.y),
      fieldPB,
      new Vec2(fieldPB.x, fieldPA.y)
    };
    final Polygon field = new Polygon(v);
    
    final Ball ball = new Ball(this, createOid(), ballP, -1, field);
    final GoalZone red = new GoalZone(this, createOid(), redP, 0, new Vec2(1.5f,2f));
    final GoalZone blue = new GoalZone(this, createOid(), blueP, 1, new Vec2(1.5f,2f));
    
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
    
    int team = objective.team==1?0:1;
    scores[team]++;
    announce(team==1?"bts":"rts");

    if(player != null) {
      player.score.ballScore();
      if(player.getTeam() == team) { sendMessage(player.getDisplay() + " scored a goal!"); }
      else { sendMessage(player.getDisplay() + " is very bad at sports!"); }
    }
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
    if(con != null) { con.announce("spb"); }
  }
  
  @Override
  public String gametypeName() { return "Sports Ball"; }
  
  @Override
  public int objectiveBaseId() { return 1; }
}
