package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class Rabbit extends SoloGame {

  private final FlagRabbit flag;
  
  public Rabbit(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));
    
    flag = spawnFlag();
  }
  
  private FlagRabbit spawnFlag() {
    List<NoxioMap.Spawn> fs = map.getSpawns("flag", gametypeId());
    final Vec2 fsl;
    fsl = fs.isEmpty()?new Vec2((map.getBounds()[0]*0.5f)+1f, map.getBounds()[1]*0.5f):fs.get(0).getPos();
    final FlagRabbit f;
    f = new FlagRabbit(this, createOid(), fsl, -1);
    addObject(f);
    return f;
  }
  
  @Override
  public void step() {
    super.step();
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    
    if(announceKill(killer, victim)) {
      final GameObject obj = killer.getControlled();
      if(obj != null && obj.is(GameObject.Types.PLAYER)) {
        final Player ply = (Player)obj;
        if(ply.getHolding() == flag) {
          killer.score.killObjective();
          reportObjective(killer, flag);
        }
      }
    }
    
    updateScore();
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    if(isGameOver()) { return; }
    player.score.rabbitControl();
    updateScore();
    announceObjective();
    if(player.score.getObjectives() >= scoreToWin) {
      if(player.score.getDeaths() < 1) { player.announce("pf"); player.score.perfect(); }
      gameOver(player.getDisplay() + " wins!", player.getMessageA(), player.getCustomSound());
      
      final boolean bsort = true;
      final Controller[] cs = controllers.toArray(new Controller[0]);
      for(int i=0;i<cs.length-1;i++) {
        if(cs[i].score.getObjectives()< cs[i+1].score.getObjectives()) {
          final Controller swp = cs[i];
          cs[i] = cs[i+1]; cs[i+1] = swp;
        }
        if(i>=cs.length-1 && !bsort) { i=0; }
      }

      for(int i=0;i<cs.length;i++) {
        switch((int)(4f*(((float)i+1)/cs.length))) {
          case 1 : { cs[i].score.win(); break; }
          case 2 : { cs[i].score.neutral(); break; }
          case 3 : { cs[i].score.neutral(); break; }
          case 4 : { cs[i].score.lose(); break; }
        }
      }
    }
  }
  
  private Controller lead = null;     // Points to player who was in the lead last time we announced objective. If this changes we inform players.
  @Override
  public void announceObjective() {
    if(controllers.size() < 1) { return; } // No players
    Controller newLead = controllers.get(0);
    for(int i=1;i<controllers.size();i++) {
      if(controllers.get(i).score.getObjectives() > newLead.score.getObjectives()) {
        newLead = controllers.get(i);
      }
    }
    if(lead == null || newLead.score.getObjectives() > lead.score.getObjectives() && newLead.score.getObjectives() < scoreToWin) {
      if(lead != null) { lead.announce("ll"); newLead.announce("gl"); }
      if(newLead.score.getObjectives() > 0) { lead = newLead; }
    }
  }  
  @Override
  public void join(final NoxioSession player) throws IOException {
    super.join(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { con.announce("ulf"); }
  }
  
  @Override
  public String gametypeName() { return "Rabbit"; }
  
  @Override
  public int objectiveBaseId() { return 3; }
}
