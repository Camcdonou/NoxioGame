package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class Tag extends SoloGame {
  public static final int TAG_COOLDOWN_TIME = 30;

  private GameObject it;
  private final int scoreTimeAdjust;
  private int tagCooldown;
  private final Map<Controller, Integer> scoreTimers;
  
  public Tag(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings, settings.get("score_to_win", 25, 1, 99));
    
    scoreTimeAdjust = settings.get("score_time_adjust", 120, 30, 300);
    
    scoreTimers = new HashMap();
    tagCooldown = 0;
    it = null;
  }
  
  /* Whoever happens to be alive */
  private void makeIt() {
    for(int i=0;i<controllers.size();i++) {
      final GameObject obj = controllers.get(i).getControlled();
      if(obj != null && !obj.isDead()) {
        makeIt(controllers.get(i), obj);
        return;
      }
    }
  }
  
  /* Specific player */
  private void makeIt(final Controller player, final GameObject obj) {
    if(controllers.size() < 2) { return; } /* Don't make an Ultimate Lifeform until there is more than 1 player in the game. */
    player.announce("hc");
    it = obj;
  }
  
  @Override
  public void step() {
    super.step();
    if(controllers.size() < 2) { return; }
    
    if(it != null && !it.isDead()) {
      if(it.is(GameObject.Types.PLAYER)) {
        ((Player)it).objective();
      }
    }
    else { makeIt(); }
    
    for(int i=0;i<controllers.size();i++) {
      final Controller c = controllers.get(i);
      int sc = scoreTimers.get(c);
      if(c.getControlled() == null || c.getControlled().isDead() || c.getControlled() == it) {
        sc = Math.max(0, sc-1);
      }
      else { sc++; }
      if(sc > scoreTimeAdjust) {
        sc = 0;
        reportObjective(c, c.getControlled());
      }
      scoreTimers.put(c, sc);
    }
    
    if(tagCooldown > 0) { tagCooldown--; }
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    if(isGameOver()) { return; }                              // Prevents post game deaths causing a double victory
    final Controller victim = getControllerByObject(killed);
    
    if(announceKill(killer, victim)) {
      final GameObject obj = killer.getControlled();
      if(killed == it) {
        if(obj != null && !obj.isDead()) { makeIt(killer, obj); }
      }
    }
    
    updateScore();
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    if(isGameOver()) { return; }
    player.score.tagControl();
    updateScore();
    announceObjective();
    if(player.score.getObjectives() >= scoreToWin) {
      gameOver(player.getDisplay() + " wins!", "[CUSTOM WIN MESSAGE]", player.getCustomSound());
      
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
  
  @Override
  public void reportTouch(Mobile a, Mobile b) {
    if(tagCooldown > 0) { return; }
    
    final Controller ca = getControllerByObject(a);
    final Controller cb = getControllerByObject(b);
    if(ca == null || cb == null) { return; }
    
    if(a == it) {
      makeIt(cb, b);
      b.stun(TAG_COOLDOWN_TIME);
      tagCooldown = TAG_COOLDOWN_TIME;
      if(a.is(GameObject.Types.PLAYER)) { ((Player)a).dejective(); }
    }
    else if(b == it) {
      makeIt(ca, a);
      a.stun(TAG_COOLDOWN_TIME);
      tagCooldown = TAG_COOLDOWN_TIME;
      if(b.is(GameObject.Types.PLAYER)) { ((Player)b).dejective(); }
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
    if(con != null) {
      con.announce("ulf");
      scoreTimers.put(con, 0);
    }
  }
  
  @Override
  public void leave(final NoxioSession player) {
    super.leave(player);
    final Controller con = getController(player.getSessionId());
    if(con != null) { scoreTimers.remove(con); }
  }
  
  @Override
  public String gametypeName() { return "Tag"; }
  
  @Override
  public int objectiveBaseId() { return 3; }
}
