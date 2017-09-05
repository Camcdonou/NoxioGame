package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;
import org.infpls.noxio.game.module.game.dao.lobby.GameSettings;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.session.ingame.*;

public class CaptureTheFlag extends NoxioGame {
  
  private int[] scores;
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
  protected void spawnPlayer(PacketI02 p) {
    Controller c = getController(p.getSrcSid());
    if(c.getControlled() != null) { return; } /* Already controlling an object */
    
    Vec2 sp;
    List<NoxioMap.Spawn> spawns = map.getSpawns("player");
    for(int i=0;i<spawns.size();i++) {
      if(spawns.get(i).getTeam() != c.getTeam()) { spawns.remove(i--); }
    }
    if(spawns.size() < 1) { sp = new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f); } // Fallback
    else {
      sp = spawns.get(0).getPos(); float d = averagePlayerDistance(sp);
      for(int i=1;i<spawns.size();i++) {
        float dn = averagePlayerDistance(spawns.get(i).getPos());
        if(dn > d) { sp = spawns.get(i).getPos(); }
      }
    }
    
    long oid = createOid();
    Player player = new Player(this, oid, sp, c.getTeam());
    addObject(player);
    c.setControl(player);
  }
    
  /* Used to find the spawn point that is the "safest" to spawn at. */
  private float averagePlayerDistance(final Vec2 P) {
    float d = 0f; int j = 0;
    for(int i=0;i<controllers.size();i++) {
      GameObject obj = controllers.get(i).getControlled();
      if(obj != null) {
        d += obj.getPosition().distance(P);
        j++;
      }
    }
    return d/j;
  }
  
  @Override
  public void requestTeamChange(final Controller controller) {
    if(autoBalanceTeams) {
      int a=0, b=0;
      for(int i=0;i<controllers.size();i++) {
        if(controllers.get(i).getTeam()==0) { a++; }
        else { b++; }
      }
      if(a<b && controller.getTeam()==1) { controller.setTeam(0); }
      else if(b<a && controller.getTeam()==0) { controller.setTeam(1); }
    }
    else {
      controller.setTeam(controller.getTeam()==0?1:0);
    }
  }

  @Override
  public void reportKill(final Controller killer, final GameObject killed) {
    final Controller victim = getControllerByObject(killed);
    if(killer != null && victim != null) {
      killer.getScore().kill();
      victim.getScore().death();
      sendMessage(killer.getUser() + " killed " + victim.getUser() + ".");
      updateScore();
    }
  }
  
  @Override
  public void reportObjective(final Controller player, final GameObject objective) {
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
    
//    for(int i = 0;i<controllers.size();i++) {
//      final Controller c = controllers.get(i);
//      final Score s = controllers.get(i).getScore();
//      scs.add(new ScoreBoard(c.getUser(), s.getObjectives() + "/" + s.getKills() + "/" + s.getDeaths(), 0.0f, new Color3()));
//    }
    lobby.sendPacket(new PacketG14("Capture The Flag", "First team to " + scoreToWin + " captures wins!", scs));
  }
  
  @Override
  public void join(final NoxioSession player) throws IOException {
    int t = 0;
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getTeam()==0) { t++; }
      else { t--; }
    }
    controllers.add(new Controller(this, player.getUser(), player.getSessionId(), t<0?0:1));
    for(int i=0;i<objects.size();i++) {
      GameObject obj = objects.get(i);
      generateJoinPacket(player);
    }
    updateScore();
  }
  
  @Override
  public String gametypeName() { return "Capture The Flag"; }
}
