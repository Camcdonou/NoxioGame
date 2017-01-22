package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.util.Salt;

public class GameLobby {
  private final String lid; //Lobby ID
  
  private final String name;
  private final boolean autoClose; //Automatically close this lobby if it's empty.
  
  private final int maxPlayers;
  private final List<NoxioSession> players; //@FIXME Access to this must be synchronized or it might RIP
  
  private final GameLoop loop;
  
  private boolean closed; //Clean this shit up!
  public GameLobby(final String name, final boolean autoClose) {
    lid = Salt.generate();
    this.name = name;
    this.autoClose = autoClose;
    maxPlayers = 16; //Lol
    players = new ArrayList();
    loop = new GameLoop(this); loop.start(); //@FIXME apparently a no no??
    closed = false;
  }
  
  public void step() {
    try {
      for(int i=0;i<players.size();i++) {
        players.get(i).sendPacket(new PacketG05(0));
      }
    }
    catch(IOException e) {
      /* @FIXME do something? Probably handle server side GAME errors by closing the lobby and kicking players to menu. */
    }
  }
  
  public boolean join(NoxioSession player) throws IOException {
    if(closed) { return false; }
    if(players.size() >= maxPlayers) { return false; }
    if(players.contains(player)) { return false; } // @FIXME If this actually happens then something has gone HORRENDOUSLY wrong. Maybe even throw an exception.
    players.add(player);
    updatePlayerList();
    return true;
  }
  
  public void leave(NoxioSession player) throws IOException {
    players.remove(player);
    if(players.size() < 1 && autoClose) { close(); }
    if(players.size() >= 1) { updatePlayerList(); }
  }
  
  private void close() throws IOException {
    closed = true;
    for(int i=0;i<players.size();i++) {
      players.get(i).leaveGame(); /* @FIXME maybe give leaveGame a message parameter to tell people why they were removed if there is a reason */
    }
  }
  
  private void updatePlayerList() throws IOException {
    List<String> playerList = new ArrayList();
    for(int i=0;i<players.size();i++) {
      playerList.add(players.get(i).getUser());
    }
    for(int i=0;i<players.size();i++) {
      players.get(i).sendPacket(new PacketG04(playerList));
    }
  }
  
  //@FIXME Not a great way to do this. Need to seperate Offical Server into it's own type. Seperate from a user server. Also the host being the person in slot 0 is kinda dumb maybe.
  public String getHost() { return autoClose ? (players.size() > 0 ? players.get(0).getUser() : "N/A") : "Official Server"; }
  public String getLid() { return lid; }
  public GameLobbyInfo getInfo() { return new GameLobbyInfo(lid, name, "STUB", getHost(), players.size(), maxPlayers); }
  public boolean isClosed() { return closed; }
  
  /* @FIXME This might be the worst way to do this in the universe. It might be fine. No way to know really. 
     Just make sure it's safe by using sychronized methods where you can and making sure you don't subscribe
     players to the game loop before their clients are ready to handle the stream of data.
  */
  private class GameLoop extends Thread {
    private static final int TICK_RATE = 100;
    private final GameLobby lobby;
    public GameLoop(final GameLobby lobby) {
      super();
      this.lobby = lobby;
    }
    
    @Override
    public void run() {
      long last = System.currentTimeMillis();
      while(!lobby.closed) {
        long now = System.currentTimeMillis();
        if(last + GameLoop.TICK_RATE <= now) {
          last = now;
          lobby.step();
        }
        try {
          long t = (last + GameLoop.TICK_RATE) - now;
          sleep(t > GameLoop.TICK_RATE ? GameLoop.TICK_RATE : (t < 1 ? 1 : t));
        }
        catch(InterruptedException ex) {
          /* DO something about this... Not sure if this is a real problem or not, might report it in debug. */
        }
      }
    }
  }
}
