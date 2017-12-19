package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class CaptureTheFlag extends TeamGame {

  public CaptureTheFlag(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 3));
    
    spawnFlags();
  }
  
  private void spawnFlags() {
    List<NoxioMap.Spawn> rs = map.getSpawns("flag", gametypeName(), 0);
    List<NoxioMap.Spawn> bs = map.getSpawns("flag", gametypeName(), 1);
    final Vec2 rsl, bsl;
    rsl = rs.isEmpty()?new Vec2((map.getBounds()[0]*0.5f)+1f, map.getBounds()[1]*0.5f):rs.get(0).getPos();
    bsl = bs.isEmpty()?new Vec2((map.getBounds()[0]*0.5f)-1f, map.getBounds()[1]*0.5f):bs.get(0).getPos();
    final Flag rf, bf;
    rf = new Flag(this, createOid(), rsl, 0);
    bf = new Flag(this, createOid(), bsl, 1);
    addObject(rf);
    addObject(bf);
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
    if(player.getTeam()==0) { scores[0]++; announce("rs"); }
    else { scores[1]++; announce("bs"); }
    player.getScore().objective();
    updateScore();
    announceObjective();
    int winr;
    if( scores[0] >= scoreToWin) { gameOver("Red Team wins!"); winr = 0; }
    else if( scores[1] >= scoreToWin) { gameOver("Blue Team wins!"); winr = 1; }
    else { return; }
    if(scores[winr==0?1:0] == 0) {
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam() == winr) { controllers.get(i).announce("pf"); }
      }
    }
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { con.announce("ctf"); }
  }
  
  @Override
  public String gametypeName() { return "Capture The Flag"; }
  
  @Override
  public int objectiveBaseId() { return 1; }
}
