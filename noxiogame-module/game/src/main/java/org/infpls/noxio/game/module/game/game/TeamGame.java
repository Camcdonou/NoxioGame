package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.*;

public abstract class TeamGame extends NoxioGame {
  
  protected final int[] scores;
  protected final int scoreToWin;
  protected final boolean autoBalanceTeams;
  
  public TeamGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings, final int stw) throws IOException {
    super(lobby, map, settings);
    
    scores = new int[]{0,0};
    scoreToWin = stw;
    autoBalanceTeams = settings.get("auto_balance_teams", 1, 0, 1)==1;
  }
  
  @Override
  protected void spawnPlayer(final Controller c, final Queue<String> q) {
    final String charSel = q.remove();
    if(c.getControlled() != null || !c.respawnReady()) { return; } /* Already controlling an object */

    final List<NoxioMap.Spawn> spawns = map.getSpawns("player", gametypeName(), c.getTeam());
    final Vec2 sp = findSafeSpawn(spawns);
    
    int oid = createOid();
    Player player = makePlayerObject(charSel, sp, c.getTeam());
    addObject(player);
    c.setControl(player);
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
  }
  
  @Override
  public int isTeamGame() { return 2; }
}
