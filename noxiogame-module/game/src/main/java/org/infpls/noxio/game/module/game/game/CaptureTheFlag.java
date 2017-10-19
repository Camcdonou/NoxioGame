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
    Player player = new Player(this, oid, sp, c.getTeam());
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
    if(killer != null && victim != null) {
      if(killer.getTeam() != victim.getTeam()) {
        killer.getScore().kill();
        victim.getScore().death();
        sendMessage(killer.getUser() + " killed " + victim.getUser() + ".");
      }
      else {
        victim.getScore().death();
        killer.penalize();
        sendMessage(killer.getUser() + " betrayed " + victim.getUser() + ".");
      }
      updateScore();
    }
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
    if(isGameOver()) { return; }                              // Prevents post game scores causing a double victory
    sendMessage(player.getUser() + " scored!");
    if(player.getTeam()==0) { scores[0]++; }
    else { scores[1]++; }
    player.getScore().objective();
    updateScore();
    if( scores[0] >= scoreToWin) { gameOver("Red Team wins!"); }
    else if( scores[1] >= scoreToWin) { gameOver("Blue Team wins!"); }
  }
  
  @Override
  public void updateScore() {
    final List<ScoreBoard> scs = new ArrayList();
    
    scs.add(new ScoreBoard("Red Team", scores[0] + "", (float)scores[0]/scoreToWin, new Color3(1.0f, 0.5f, 0.5f)));
    scs.add(new ScoreBoard("Blue Team", scores[1] + "", (float)scores[1]/scoreToWin, new Color3(0.5f, 0.5f, 1.0f)));
    
    final StringBuilder sb = new StringBuilder();
    sb.append("scr;Capture The Flag;First team to "); sb.append(scoreToWin); sb.append(" wins!;");
    for(int i=0;i<scs.size();i++) { sb.append(scs.get(i).name); if(i<scs.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<scs.size();i++) { sb.append(scs.get(i).score); if(i<scs.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<scs.size();i++) { sb.append(scs.get(i).meter); if(i<scs.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<scs.size();i++) { sb.append(scs.get(i).color.r); if(i<scs.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<scs.size();i++) { sb.append(scs.get(i).color.g); if(i<scs.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<scs.size();i++) { sb.append(scs.get(i).color.b); if(i<scs.size()-1) { sb.append(","); } } sb.append(";");
    
    update.add(sb.toString());
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    int t = 0;
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getTeam()==0) { t++; }
      else { t--; }
    }
    controllers.add(new Controller(this, player.getUser(), player.getSessionId(), t<0?0:1));
    generateJoinPacket(player);
    updateScore();
  }
  
  @Override
  public String gametypeName() { return "Capture The Flag"; }
}
