package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.dao.lobby.GameSettings;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class CaptureTheFlag extends NoxioGame {
  
  private final int[] scores;
  private final int scoreToWin;
  private final boolean autoBalanceTeams;
  
  public CaptureTheFlag(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    super(lobby, map, settings);
    
    scores = new int[]{0,0};
    scoreToWin = settings.get("score_to_win", 3);
    autoBalanceTeams = settings.get("auto_balance_teams", 1)==1;
    spawnFlags();
  }
  
  private void spawnFlags() {
    List<NoxioMap.Spawn> rs = map.getSpawns("flag", 0);
    List<NoxioMap.Spawn> bs = map.getSpawns("flag", 1);
    final Flag rf, bf;
    rf = new Flag(this, createOid(), rs.get(0).getPos(), 0);
    bf = new Flag(this, createOid(), bs.get(0).getPos(), 1);
    addObject(rf);
    addObject(bf);
  }
  
  @Override
  protected void spawnPlayer(final Controller c, final Queue<String> q) {
    final String charSel = q.remove();
    if(c.getControlled() != null || !c.respawnReady()) { return; } /* Already controlling an object */
    
    Vec2 sp;
    final List<NoxioMap.Spawn> spawns = map.getSpawns("player", c.getTeam());
    if(spawns.isEmpty()) { sp = new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f); } // Fallback
    else {
      final List<NoxioMap.Spawn> crop = new ArrayList();
      for(int i=0;i<spawns.size();i++) {
        if(nearestPlayerDistance(spawns.get(i).getPos()) >= SPAWN_SAFE_RADIUS) {
          crop.add(spawns.get(i));
        }
      }
      if(crop.isEmpty()) {
        sp = spawns.get((int)(Math.random()*spawns.size())).getPos();
      }
      else {
        sp = crop.get((int)(Math.random()*crop.size())).getPos();
      }
    }
    
    int oid = createOid();
    Player player = makePlayerObject(charSel, sp, c.getTeam());
    addObject(player);
    c.setControl(player);
  }
    
  /* Used to find the spawn point that is the "safest" to spawn at. */
  private float nearestPlayerDistance(final Vec2 P) {
    float d = 0f;
    for(int i=0;i<controllers.size();i++) {
      GameObject obj = controllers.get(i).getControlled();
      if(obj != null) {
        float k = obj.getPosition().distance(P);
        if(k < d) { d = k; }
      }
    }
    return d;
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
  
  private int lead = 0;
  @Override
  public void announceObjective() {
    if(controllers.size() < 1) { return; } // No players
    int newLead;
    if(scores[0] > scores[1]) { newLead = 0; }
    else { newLead = 1; }
    
    if(scores[newLead] > scores[lead] && scores[newLead] < scoreToWin) {
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam() == newLead) { controllers.get(i).announce("gl"); }
        else { controllers.get(i).announce("ll"); }
      }
      lead = newLead;
    }
  }
  
  @Override
  public void updateScore() {
    final StringBuilder sb = new StringBuilder();
    sb.append("scr;");
    for(int i=0;i<scores.length;i++) { sb.append(scores[i]); if(i<scores.length-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getUser()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getTeam()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getScore().getKills()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getScore().getDeaths()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getScore().getObjectives()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    
    update.add(sb.toString());
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    int t = 0;
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getTeam()==0) { t++; }
      else { t--; }
    }
    final Controller controller = new Controller(this, player.getUser(), player.getSessionId(), t<0?0:1);
    controllers.add(controller);
    generateJoinPacket(player);
    updateScore();
    controller.announce("ctf");
  }
  
  @Override
  public String gametypeName() { return "Capture The Flag"; }
  
  @Override
  public int objectiveBaseId() { return 1; }
}
