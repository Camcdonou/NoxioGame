package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class Assault extends TeamGame {

  public Assault(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 3, 1, 25));
    
    spawnFlags();
  }
  
  private void spawnFlags() {
    List<NoxioMap.Spawn> rs = map.getSpawns("flag", gametypeId(), 0);
    List<NoxioMap.Spawn> bs = map.getSpawns("flag", gametypeId(), 1);
    final Vec2 rsl, bsl;
    rsl = rs.isEmpty()?new Vec2((map.getBounds()[0]*0.5f)+1f, map.getBounds()[1]*0.5f):rs.get(0).getPos();
    bsl = bs.isEmpty()?new Vec2((map.getBounds()[0]*0.5f)-1f, map.getBounds()[1]*0.5f):bs.get(0).getPos();
    final FlagAssault rf, bf;
    rf = new FlagAssault(this, createOid(), rsl, 0);
    bf = new FlagAssault(this, createOid(), bsl, 1);
    addObject(rf);
    addObject(bf);
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
    if(player.getTeam()==0) { scores[0]++; announce("rs"); }
    else { scores[1]++; announce("bs"); }
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
    if(scores[winr==0?1:0] == 0) {
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam() == winr) { controllers.get(i).announce("pf"); controllers.get(i).score.perfect(); }
      }
    }
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getTeam() == winr) { controllers.get(i).score.win(); }
      else { controllers.get(i).score.lose(); }
    }
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { con.announce("ctf"); }
  }
  
  @Override
  public String gametypeName() { return "Assault"; }
  
  @Override
  public int objectiveBaseId() { return 1; }
}
