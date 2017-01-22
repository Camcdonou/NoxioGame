package org.infpls.noxio.game.module.game.dao.lobby;

public class GameLobbyInfo {
  private final String lid; //Lobby ID
  
  private final String name, gametype, host;
  private final int players, maxPlayers;
  public GameLobbyInfo(final String lid, final String name, final String gametype, final String host, final int players, final int maxPlayers) {
    this.lid = lid;
    this.name = name; this.gametype = gametype; this.host = host; 
    this.players = players;
    this.maxPlayers = maxPlayers;
  }
  
  public String getName() { return name; }
  public String getGametype() { return gametype; }
  public String getHost() { return host; }
  public int getPlayers() { return players; }
  public int getMaxPlayers() { return maxPlayers; }
}
