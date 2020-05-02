package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.List;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class King extends SoloGame {

  private final Hill hill;
  private final int scoreToMove;
  private final boolean staticHill;
  private int hillSpawnRotation, moveTimer;
  public King(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));

    staticHill = settings.get("static_hill", 0, 0, 1)==0;
    scoreToMove = settings.get("score_to_move", 7, 1, 99);
    
    moveTimer = 0;
    hillSpawnRotation = 0;
    
    hill = createHill();
  }
  
  private Hill createHill() {
    List<NoxioMap.Spawn> hillSpawns = map.getSpawns("hill", gametypeId());
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
    List<NoxioMap.Spawn> hillSpawns = map.getSpawns("hill", gametypeId());
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
    if(killer == null) { return; }
    final Controller victim = getControllerByObject(killed);
    
    final GameObject obj = killer.getControlled();
    if(obj != null && hill.isInside(obj)) { killer.score.killObjective(); }
    
    announceKill(killer, victim);

    updateScore();
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    if(isGameOver()) { return; }                              // Prevents post game scores causing a double victory @TODO: doesnt work, look at actual value instead.
    player.score.hillControl();
    updateScore();
    announceObjective();
    if(moveTimer++ > scoreToMove) { moveHill(); }
    if(player.score.getObjectives() >= scoreToWin) {
      if(player.score.getDeaths() < 1 && controllers.size() > 2) { player.announce("pf"); player.score.perfect(); }
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
    if(con != null) { con.announce("kh"); }
  }
  
  @Override
  public String gametypeName() { return "King"; }
  
  @Override
  public int objectiveBaseId() { return 2; }
}
