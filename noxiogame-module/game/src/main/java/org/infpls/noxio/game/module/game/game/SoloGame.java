package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.game.object.*;

public abstract class SoloGame extends NoxioGame {
  
  protected final int scoreToWin;
  
  public SoloGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings, final int stw) throws IOException {
    super(lobby, map, settings);
    
    scoreToWin = stw;
  }
  
  @Override
  public void requestTeamChange(final Controller c, final Queue<String> q) {
    /* No teams, ignore */
  }
  
  @Override
  protected void spawnPlayer(final Controller c, final Queue<String> q) {
    final String charSel = q.remove();
    if(c.getControlled() != null || !c.respawnReady()) { return; } /* Already controlling an object */
    
    final List<NoxioMap.Spawn> spawns = map.getSpawns("player", gametypeName());
    final Vec2 sp = findSafeSpawn(spawns);
    
    int oid = createOid();
    Player player = makePlayerObject(c, charSel, sp);
    addObject(player);
    c.setControl(player);
  }
  
  @Override
  public void updateScore() {
    final StringBuilder sb = new StringBuilder();
    sb.append("scr;");
    sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getUser()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).getTeam()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getKills()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getDeaths()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    for(int i=0;i<controllers.size();i++) { sb.append(controllers.get(i).score.getObjectives()); if(i<controllers.size()-1) { sb.append(","); } } sb.append(";");
    
    update.add(sb.toString());
  }
  
  @Override
  public int isTeamGame() { return 0; }
}
