package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Salt;

/* On next work day.
   ! - INPUT IS SO FUCKING WEIRD LOL to many references to handle to many things in weird ways. pls fix.
   # - RequestAnimFrame + Delta Time Interpolation (remember camera & particles & lights needs it)
   ? - Fill out the Gametype sub types so they do something.
   & - Gametype/map dependency needs to be worked out. Also MapDao or whatever
   1 - Clean up PacketG10 AND INPUT PACKETS some it's a bit of a mess ##ONGOING
   2 - Clean up stuff in general maybe.... (client...) ##ONGOING
   4 - Map files need static mesh support and game object spawning and gametype info added to their spec
   6 - UI Scaling so text remains readable?
   8 - HDR
   14 - Extend sound stuf https://www.html5rocks.com/en/tutorials/webaudio/games/ && https://www.html5rocks.com/en/tutorials/webaudio/intro/
   9 - Object creation permutations
   10 - (UPDATE: ITS COLLISION RELATED? happened again when i had 2 player objects run into each other) saw a rare bug where a lobby borked and appeared to have its game loop stopped but no exceptions, no idea what happned. showed 2 players even after logging out.
   11 - shadow map size needs to be added as a uniform to allow proper sampling (ACTUALLY JUST REFACTOR AND RENAME AND UPDATE THAT LOL )
   12 - maybe adjust center point based on camera angle. (maaaaaaaaaaaaaaybe)
   13 - make a nice actual map for benchmarking
   14 - SKYBOXES OR SOMETHING...
*/

public abstract class GameLobby {
  protected final String lid; //Lobby ID
  
  protected final String name;
  
  protected final int maxPlayers;
  protected final List<NoxioSession> players, loading; //@FIXME Access to this should be synchronized or it might RIP
  
  private final GameLoop loop; /* Seperate timer thread to trigger game steps */
  protected NoxioGame game; /* The actual game object */
  
  private final PacketSync packets; /* Packets that the game must handle are stored until a gamestep happens. This is for synchronization. */
  private final EventSync events; /* Second verse same as the first. */
  
  private final GameSettings settings;
  private final NoxioMap map;
  
  protected boolean closed; //Clean this shit up!
  public GameLobby(final GameSettings settings) throws IOException {
    lid = Salt.generate();
    this.settings = settings;
    
    name = settings.get("game_name", "Default Name");
    map = new NoxioMap(settings.get("map_name", "final"));
    maxPlayers = settings.get("max_players", 6);
    
    players = new ArrayList();
    loading = new ArrayList();
    closed = false;
    
    packets = new PacketSync();
    events = new EventSync();
    
    outAll = new ArrayList();
    outDirect = new HashMap();
    
    newGame();
    loop = new GameLoop(this); loop.start(); //@FIXME apparently a no no??
  }
  
  private void newGame() throws IOException {
    final String gametype = settings.get("gametype", "Deathmatch");
    switch(gametype) {
      case "Deathmatch" : { game = new Deathmatch(this, map, settings); break; }
      case "TeamDeathmatch" : { game = new TeamDeathmatch(this, map, settings); break; }
      case "CaptureTheFlag" : { game = new CaptureTheFlag(this, map, settings); break; }
      default : { game = new Deathmatch(this, map, settings); break; }
    }
  }
  
  /* @FIXME this method is getting pretty THICC. Maybe put it on a diet or something... */
  public void step(final long tick) {
    try {
      final List<SessionEvent> evts = events.pop();
      for(int i=0;i<evts.size();i++) {
        SessionEvent evt = evts.get(i);
        switch(evt.getType()) {
          case "e00" : {
            if(!evt.getSession().isOpen()) { break; } /* Check to make sure connection is still active. */
            if(!connect(evt.getSession())) {
              sendPacket(new PacketG06("Failed to connect to game."), evt.getSession());
              evt.getSession().leaveGame();
            }
            else { 
              GameLobbyInfo info = getInfo();
              evt.getSession().sendPacket(new PacketG01(info.getName(), info.getMaxPlayers(), game.map));
            }
            break;
          }
          case "e01" : {
            if(!evt.getSession().isOpen()) { break; } /* Check to make sure connection is still active. */
            if(!join(evt.getSession())) {
              sendPacket(new PacketG06("Failed to join game."), evt.getSession());
              evt.getSession().leaveGame();
            }
            break;
          }
          case "e02" : {
            if(!evt.getSession().isOpen()) { break; } /* Check to make sure connection is still active. */
            leave(evt.getSession());
            evt.getSession().sendPacket(new PacketG08()); /* This is one of the few packets we dont send in a blob because it has an odd state. */
            break;
          }
          case "e03" : {
            remove(evt.getSession());
            break;
          }
          default : { throw new IOException("Invalid SessionEvent type: " + evt.getType() + ". This really should never happen."); }
        }
      }
      if(game.isGameOver()) {
        newGame();
        packets.pop();
        GameLobbyInfo info = getInfo();
        for(int i=0;i<players.size();i++) {
          players.get(i).sendPacket(new PacketG17(info.getName(), info.getMaxPlayers(), game.map)); /* This is one of the few packets we dont send in a blob because it has an odd state. */
          loading.add(players.get(i)); /* We don't check for duplicates because if the situation arises where a player loading and a new game triggers we need them to return load finished twice. */
          /* @FIXME while the above comment describes what should happen this might need testing and maybe we need to ID our loads to make sure that the right load is done before allowing the player to join the game */
        }
        return; /* Screw you guys I'm going home. */
      }
      
      game.handlePackets(packets.pop()); // Player Input
      game.step();                       // Game tick
      game.generateUpdatePackets(tick);  // Send updates to players
      game.post();                       // Clean up
      
      for(int i=0;i<players.size();i++) {
        final NoxioSession player = players.get(i);
        if(!loading.contains(player) && !outAll.isEmpty()) {
          player.sendPacket(new PacketS01(outAll));
        }
      }
      outAll.clear();
      for(int i=0;i<players.size();i++) {
        final NoxioSession player = players.get(i);
        if(!loading.contains(player) && !outDirect.get(player).isEmpty()) {
          player.sendPacket(new PacketS01(outDirect.get(player)));
        }
        outDirect.get(player).clear();
      }
    }
    catch(Exception ex) {
      for(int i=0;i<players.size();i++) {
        try { players.get(i).close(ex); }
        catch(IOException ioex) { System.err.println("Bad stuff be happening here!"); ioex.printStackTrace(); }
      }
      System.err.println("## CRITICAL ## Game step exception!");
      System.err.println("## STATE    ## lobbyName=" + name + "gameOver=" + game.isGameOver());
      ex.printStackTrace();
      /* @FIXME do something? Probably handle server side GAME errors by closing the lobby and kicking players to menu. */
    }
  }
   
  protected boolean connect(NoxioSession player) throws IOException {
    if(closed) { return false; }
    if(players.size() >= maxPlayers) { return false; }
    if(players.contains(player)) { player.close("Lobby Doppleganger Error."); return false; }
    players.add(player);
    loading.add(player);
    outDirect.put(player, new ArrayList());
    game.sendMessage(player.getUser() + " connected.");
    return true;
  }
  
  private boolean join(NoxioSession player) throws IOException {
    if(closed) { return false; }
    if(players.size() >= maxPlayers) { return false; }
    if(!players.contains(player) || !loading.contains(player)) { remove(player); player.close("Lobby Ghost Error."); return false; } /* A thing that can happen and cause null pointers. Only possible if load times are long on clients are long and they leave lobby and come back AFAIK */
    loading.remove(player);
    game.join(player);
    game.sendMessage(player.getUser() + " joined the game.");
    //@TODO: REMOVE THIS TESTING STUFF
    game.getController(player.getSessionId()).whisper("Controls:");
    game.getController(player.getSessionId()).whisper("R-Mouse = Move, ESC = Menu");
    game.getController(player.getSessionId()).whisper("F = Shine, Shift = Dash, Space = Jump");
    return true;
  }
  
  protected abstract void leave(NoxioSession player) throws IOException;
  public abstract void remove(NoxioSession player) throws IOException;
  
  protected void close() throws IOException {
    closed = true;
    for(int i=0;i<players.size();i++) {
      players.get(i).leaveGame(); /* @FIXME maybe give leaveGame a message parameter to tell people why they were removed if there is a reason */
    }
    game.close();
  }
  
  protected List<Packet> outAll;
  protected Map<NoxioSession, List<Packet>> outDirect;
  /* Send a packet to everyone in the lobby */
  public void sendPacket(final Packet p) { /* @FIXME catching exceptions here in these 3 methods is bad nono */
    outAll.add(p);
  }
  
  /* Send a packet to a specific player with the given SID */
  public void sendPacket(final Packet p, final String sid) {
    for(int i=0;i<players.size();i++) {
      final NoxioSession player = players.get(i);
      if(!loading.contains(player) && player.getSessionId().equals(sid)) {
        outDirect.get(player).add(p);
        return;
      }
    }
  }
  
  /* Send a packet to a specific player */
  public void sendPacket(final Packet p, final NoxioSession player) {
    outDirect.get(player).add(p);
  }
  
  public void pushPacket(final Packet p) { packets.push(p); }
  public void pushEvent(final SessionEvent evt) { events.push(evt); }
  
  //@FIXME Not a great way to do this. Need to seperate Offical Server into it's own type. Seperate from a user server. Also the host being the person in slot 0 is kinda dumb maybe.
  public abstract NoxioSession getHost();
  public String getLid() { return lid; }
  public abstract GameLobbyInfo getInfo();
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
  
  private class PacketSync {
    private List<Packet> packets;
    public PacketSync() { packets = new ArrayList(); }

    public void push(final Packet p) { syncPacketAccess(false, p); }
    public List<Packet> pop() { return syncPacketAccess(true, null); }

    /* Game Packet handling methods 
       - syncAccess.s == true / pop
       - syncAccess.s == false / push
    */
    private synchronized List<Packet> syncPacketAccess(final boolean s, final Packet p) {
      if(s) {
        List<Packet> pkts = packets;
        packets = new ArrayList();
        return pkts;
      }
      else {
        if(p == null) { return null; }
        packets.add(p);
        return null;
      }
    }
  }
  
  private class EventSync {
    private List<SessionEvent> sessionEvents;
    public EventSync() { sessionEvents = new ArrayList(); }

    public void push(final SessionEvent evt) { syncEventAccess(false, evt); }
    public List<SessionEvent> pop() { return syncEventAccess(true, null); }

    /* SessionEvent handling methods 
     - syncAccess.s == true / pop
     - syncAccess.s == false / push
    */
    private synchronized List<SessionEvent> syncEventAccess(final boolean s, final SessionEvent evt) {
      if(s) {
        List<SessionEvent> evts = sessionEvents;
        sessionEvents = new ArrayList();
        return evts;
      }
      else {
        if(evt == null) { return null; }
        sessionEvents.add(evt);
        return null;
      }
    }
  }
}
