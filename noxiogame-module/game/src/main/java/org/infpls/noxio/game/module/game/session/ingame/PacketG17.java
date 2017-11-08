package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.game.NoxioMap;

public class PacketG17 extends Packet {
  private final String name, gametype;
  private final int maxPlayers, teams, objective, scoreToWin;
  private final NoxioMap map;
  public PacketG17(final String name, final String gametype, final int maxPlayers, final int scoreToWin, final int teams, final int objective, final NoxioMap map) {
    super("g17");
    this.name = name;
    
    this.maxPlayers = maxPlayers;
    this.scoreToWin = scoreToWin;
    this.gametype = gametype;
    this.teams = teams;
    this.objective = objective;
    
    this.map = map;
  }
  
  public String getName() { return name; } 
  public int getMaxPlayers() { return maxPlayers; }
  public int getTeams() { return teams; }
  public int getScoreToWin() { return scoreToWin; }
  public int getObjective() { return objective; }
  public String getGametype() { return gametype; }
  public NoxioMap getMap() { return map; }
}