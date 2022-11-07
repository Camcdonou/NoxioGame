package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.*;

public abstract class TeamGame extends NoxioGame {
  
  protected final int[] scores;
  protected final boolean autoBalanceTeams;
  
  public TeamGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings, final int stw) throws IOException {
    super(lobby, map, settings, stw);
    
    scores = new int[]{0,0};
    autoBalanceTeams = settings.get("auto_balance_teams", 1, 0, 1)==1;
  }
  
  private final static float SPAWN_SAFE = 5.0f, SPAWN_MIN_SAFE = 3.f, SPAWN_MIN_TEAM_SAFE = .75f;
  protected final Vec2 findSafeTeamSpawn(Controller controller, List<NoxioMap.Spawn> spawns) {
    if(spawns.isEmpty()) { return new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f); } // Fallback
    
    final List<NoxioMap.Spawn> safe = new ArrayList(), minSafe = new ArrayList();
    
    /* Strategy #1 - Max safe distance + random choice */
    for(int i=0;i<spawns.size();i++) {
      final NoxioMap.Spawn sp = spawns.get(i);
      boolean isSafe = true;
      boolean isMinSafe = true;
      for(int j=0;j<controllers.size();j++) {
        final Controller con = controllers.get(j);
        final GameObject obj = con.getControlled();
        if(obj != null && con.getTeam() != controller.getTeam()) {
          float k = obj.getPosition().distance(sp.getPos());
          if(k < SPAWN_SAFE) { isSafe = false; }
          if(k < SPAWN_MIN_SAFE) { isMinSafe = false; }
        }
        else if(obj != null && con.getTeam() == controller.getTeam()) {
          float k = obj.getPosition().distance(sp.getPos());
          if(k < SPAWN_MIN_TEAM_SAFE) { isSafe = false; isMinSafe = false; break; }
        }
      }
      if(isSafe) { safe.add(sp); }
      if(isMinSafe) { minSafe.add(sp); }
    }
    
    /* Strategy #2 - Add min safe distance spawns */
    if(safe.size() < 3) {
      for(int i=0;i<minSafe.size();i++) {
        safe.add(minSafe.get(i));
      }
    }
    
    /* Strategy #3 - Find spawn point with the most clearance */
    if(safe.size() < 1) {
      NoxioMap.Spawn safest = spawns.get(0);
      float clearance = 0.f;
      for(int i=0;i<spawns.size();i++) {
        final NoxioMap.Spawn sp = spawns.get(i);
        float ck = SPAWN_SAFE;
        
        for(int j=0;j<controllers.size();j++) {
          final GameObject obj = controllers.get(j).getControlled();
          if(obj != null) {
            float k = obj.getPosition().distance(sp.getPos());
            if(k < ck) { ck = k; }
          }
        }
        if(ck > clearance) { safest = sp; clearance = ck; }
      }
      safe.add(safest);
    }
    
    if(safe.isEmpty()) { return spawns.get((int)(Math.random()*spawns.size())).getPos(); }
    else               { return safe.get((int)(Math.random()*safe.size())).getPos(); }
  }
  
  @Override
  protected boolean spawnPlayer(final Controller c, final Queue<String> q) {
    final String charSel = q.remove();
    if(c.getControlled() != null || !c.respawnReady()) { return false; } /* Already controlling an object */

    final List<NoxioMap.Spawn> spawns = map.getSpawns("player", gametypeId(), c.getTeam());
    final Vec2 sp = findSafeTeamSpawn(c, spawns);
    
    Player player = makePlayerObject(c, charSel, sp, c.getTeam());
    addObject(player);
    c.setControl(player);
    return true;
  }
  
  @Override
  public void requestTeamChange(final Controller c, final Queue<String> q) {
    if(autoBalanceTeams) {
      int a=0, b=0;
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam()==0) { a++; }
        else { b++; }
      }
      if(a<b && c.getTeam()==1) { c.setTeam(0); }
      else if(b<a && c.getTeam()==0) { c.setTeam(1); }
      else { c.whisper("Teams are unbalanced, can't switch."); }
    }
    else {
      c.setTeam(c.getTeam()==0?1:0);
    }
  }
  
  private boolean firsties = true;
  private int lead = 0;
  @Override
  public void announceObjective() {
    if(controllers.size() < 1) { return; } // No players
    int newLead;
    if(scores[0] > scores[1]) { newLead = 0; }
    else { newLead = 1; }
    
    if(scores[newLead] > scores[lead] && scores[newLead] < scoreToWin) {
      if(!firsties) {
        for(int i=0;i<controllers.size();i++) {
          if(controllers.get(i).getTeam() == newLead) { controllers.get(i).announce("gl"); }
          else { controllers.get(i).announce("ll"); }
        }
      }
      firsties = false;
      lead = newLead;
    }
  }
  
  @Override
  public void updateScore() {
    final StringBuilder sb = new StringBuilder();
    sb.append("scr;");
    for(int i=0;i<scores.length;i++) { sb.append(scores[i]); if(i<scores.length-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getDisplay()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getTeam()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getKills()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getDeaths()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getObjectives()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    
    update.add(sb.toString());
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    int t = 0;
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getTeam()==0) { t++; }
      else { t--; }
    }
    final Controller controller = new Controller(this, player, t<0?0:1);
    controllers.add(controller);
    generateJoinPacket(player);
    updateScore();
    controller.whisper("Map: " + map.getName());
    final String csm = controller.getCustomSound();
    if(csm != null) { update.add("snd;" + csm + ";"); }
  }
  
  /* Sends announce code to all players on specified team */
  public final void announceTeam(final int team, final String code) {
    for(int i=0;i<controllers.size();i++) {
      final Controller c = controllers.get(i);
      if(c.getTeam() != team) { continue; }
      c.announce(code);
    }
  }
  
  public final void setClientTimerTeam(int team, String title, int time) {
    for(int i=0;i<controllers.size();i++) {
      final Controller c = controllers.get(i);
      if(c.getTeam() != team) { continue; }
      c.setClientTimer(title, time);
    }
  }
  
  @Override
  public int isTeamGame() { return 2; }
}
