package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Salt;

public class GameLobby {
  private final String lid; //Lobby ID
  
  private final String name;
  private final boolean autoClose; //Automatically close this lobby if it's empty.
  
  private final int maxPlayers;
  private final List<NoxioSession> players; //@FIXME Access to this must be synchronized or it might RIP
  
  private final GameLoop loop; /* Seperate timer thread to trigger game steps */
  private final NoxioGame game; /* The actual game object */
  private List<Packet> gamePackets; /* Packets that the game must handle are stored until a gamestep happens. This is for synchronization. */
  
  private boolean closed; //Clean this shit up!
  public GameLobby(final String name, final boolean autoClose) {
    lid = Salt.generate();
    this.name = name;
    this.autoClose = autoClose;
    maxPlayers = 16; //Lol
    players = new ArrayList();
    closed = false;
    
    gamePackets = new ArrayList();
    game = new NoxioGame();
    loop = new GameLoop(this); loop.start(); //@FIXME apparently a no no??
  }
  
  public void step(final long tick) {
    try {
      final List<Packet> preStep = game.handlePackets(popPackets());
      for(int i=0;i<players.size();i++) {
        for(int j=0;j<preStep.size();j++) {
          if(preStep.get(j).matchSrcSid(players.get(i).getSessionId())) {
            players.get(i).sendPacket(preStep.get(j));
          }
        }
      }
      final List<Packet> updates = game.step();
      for(int i=0;i<players.size();i++) {
        for(int j=0;j<updates.size();j++) {
          if(updates.get(j).matchSrcSid(players.get(i).getSessionId())) {
            players.get(i).sendPacket(updates.get(j));
          }
        }
        players.get(i).sendPacket(new PacketG05(tick, System.currentTimeMillis()));
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
    game.join(player);
    players.add(player);
    updatePlayerList(player.getUser() + " joined the game.");
    return true;
  }
  
  public void leave(NoxioSession player) throws IOException {
    players.remove(player);
    game.leave(player);
    if(players.size() < 1 && autoClose) { close(); }
    if(players.size() >= 1) { updatePlayerList(player.getUser() + " left the game."); }
  }
  
  private void close() throws IOException {
    closed = true;
    for(int i=0;i<players.size();i++) {
      players.get(i).leaveGame(); /* @FIXME maybe give leaveGame a message parameter to tell people why they were removed if there is a reason */
    }
    game.close();
  }
  
  private void updatePlayerList(final String message) throws IOException {
    List<String> playerList = new ArrayList();
    for(int i=0;i<players.size();i++) {
      playerList.add(players.get(i).getUser());
    }
    for(int i=0;i<players.size();i++) {
      players.get(i).sendPacket(new PacketG04(playerList));
      players.get(i).sendPacket(new PacketG15(message));
    }
  }
  
  /* Game Packet handling methods 
     @FIXME this is a bit jank
     - syncAccess.s == true / pop
     - syncAccess.s == false / push
  */
  public void pushPacket(final Packet p) {
    syncAccess(false, p);
  }
  
  public List<Packet> popPackets() {
    return syncAccess(true, null);
  }
  
  private synchronized List<Packet> syncAccess(final boolean s, final Packet p) {
    if(s) {
      List<Packet> packets = gamePackets;
      gamePackets = new ArrayList();
      return packets;
    }
    else {
      if(p == null) { /* @FIXME THROW EXCEPTION */ }
      gamePackets.add(p);
      return null;
    }
  }
  
  //@FIXME Not a great way to do this. Need to seperate Offical Server into it's own type. Seperate from a user server. Also the host being the person in slot 0 is kinda dumb maybe.
  public String getHostName() { return autoClose ? (players.size() > 0 ? players.get(0).getUser() : "N/A") : "Official Server"; }
  public String getLid() { return lid; }
  public GameLobbyInfo getInfo() { return new GameLobbyInfo(lid, name, "STUB", getHostName(), players.size(), maxPlayers); }
  public boolean isClosed() { return closed; }
  
  /* @FIXME This might be the worst way to do this in the universe. It might be fine. No way to know really. 
     Just make sure it's safe by using sychronized methods where you can and making sure you don't subscribe
     players to the game loop before their clients are ready to handle the stream of data.
  */
  private class GameLoop extends Thread {
    private static final int TICK_RATE = 33;
    private final GameLobby lobby;
    
    private long lastStepTime; /* @FIXME DEBUG */
    public GameLoop(final GameLobby lobby) {
      super();
      this.lobby = lobby;
      lastStepTime = 0;
    }
    
    @Override
    public void run() {
      long last = System.currentTimeMillis();
      while(!lobby.closed) {
        long now = System.currentTimeMillis();
        if(last + GameLoop.TICK_RATE <= now) {
          last = now;
          lobby.step(lastStepTime);
          lastStepTime = System.currentTimeMillis() - now;
        }
        try {
          long t = (last + GameLoop.TICK_RATE) - System.currentTimeMillis(); //Cannot use 'now' again because time may have passed during lobby.step();
          sleep(t > GameLoop.TICK_RATE ? GameLoop.TICK_RATE : (t < 1 ? 1 : t));
        }
        catch(InterruptedException ex) {
          System.err.println("## CRITICAL ## Game loop thread interupted by exception!");
          ex.printStackTrace();
          /* DO something about this... Not sure if this is a real problem or not, might report it in debug. */
        }
      }
    }
  }
}
