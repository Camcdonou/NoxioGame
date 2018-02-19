package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.List;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class TeamKing extends TeamGame {

  private final Hill hill;
  private final int scoreToMove;
  private final boolean staticHill;
  private int hillSpawnRotation, moveTimer;
  public TeamKing(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));

    staticHill = settings.get("static_hill", 1, 0, 1)==1;
    scoreToMove = settings.get("score_to_move", 10, 1, 99);
    
    moveTimer = 0;
    hillSpawnRotation = 0;
    
    hill = createHill();
  }
  
  private Hill createHill() {
    List<NoxioMap.Spawn> hillSpawns = map.getSpawns("hill", gametypeName());
    final Vec2 hs;
    final float size;
    if(hillSpawns.isEmpty()) {
      hs = new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f);
      size = 2f;
    }
    else {
      int doot = hillSpawnRotation++%hillSpawns.size();
      hs = hillSpawns.get(doot).getPos();
      size = hillSpawns.get(doot).getTeam();
    }
    
    final Hill h = new Hill(this, createOid(), hs, new Vec2(size));
    addObject(h);
    return h;
  }
  
  private void moveHill() {
    moveTimer = 0;
    if(staticHill) { return; }
    List<NoxioMap.Spawn> hillSpawns = map.getSpawns("hill", gametypeName());
    final Vec2 hs;
    final float size;
    if(hillSpawns.isEmpty()) {
      hs = new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f);
      size = 2f;
    }
    else {
      int doot = hillSpawnRotation++%hillSpawns.size();
      hs = hillSpawns.get(doot).getPos();
      size = hillSpawns.get(doot).getTeam();
    }
    
    hill.moveTo(hs, new Vec2(size));
    announce("khm");
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    if(killer != null && victim != null && killer != victim) {
      announceKill(killer, victim);
    }
    else if(victim != null) { victim.getScore().death(); }
    updateScore();
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    if(isGameOver()) { return; }                              // Prevents post game scores causing a double victory @TODO: doesnt work, look at actual value instead.
    if(player.getTeam()==0) { scores[0]++; }
    else { scores[1]++; }
    player.getScore().objective();
    updateScore();
    announceObjective();
    if(moveTimer++ > scoreToMove) { moveHill(); }
    int winr;
    if( scores[0] >= scoreToWin) { gameOver("Red Team wins!"); winr = 0; }
    else if( scores[1] >= scoreToWin) { gameOver("Blue Team wins!"); winr = 1; }
    else { return; }
    if(scores[winr==0?1:0] == 0) {
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam() == winr) { controllers.get(i).announce("pf"); }
        else { controllers.get(i).announce("hu"); }
      }
    }
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { con.announce("tkh"); }
  }
  
  @Override
  public String gametypeName() { return "Team King"; }
  
  @Override
  public int objectiveBaseId() { return 2; }
  
  @Override
  public int getScoreToWin() { return scoreToWin; }
}
