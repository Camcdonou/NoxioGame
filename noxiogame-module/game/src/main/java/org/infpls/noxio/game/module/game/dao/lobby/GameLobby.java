package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Oak;
import org.infpls.noxio.game.module.game.util.Salt;

/* OLD TODO :: innacurate and out of date
   ! - INPUT IS SO FUCKING WEIRD LOL to many references to handle to many things in weird ways. pls fix.
   # - RequestAnimFrame + Delta Time Interpolation (remember camera & particles & lights needs it)
   & - Gametype/map dependency needs to be worked out. Also MapDao or whatever
   4 - Map files need static mesh support and game object spawning and gametype info added to their spec
   6 - UI Scaling
   8 - HDR
   14 - Extend sound stuf https://www.html5rocks.com/en/tutorials/webaudio/games/ && https://www.html5rocks.com/en/tutorials/webaudio/intro/ ?????
   9 - Object creation permutations
   11 - shadow map size needs to be added as a uniform to allow proper sampling (ACTUALLY JUST REFACTOR AND RENAME AND UPDATE THAT )
   12 - maybe adjust center point based on camera angle. (maaaaaaaaaaaaaaybe)
   14 - SKY
*/

/* Release Cleanup
  - Change passwords (mail, etc)
  - Audio Balance
  - File Compression
  - Announcer VO
  - Firefox bug (framerate/refresh rate??/double connection issue?)
  - Double connection issue
  - Hung SQL connector issue
  - Iphone issue? (safari is AudioContext webkit thing. Iphone uknown so far )
  - Controls lock out when phone is slept for a while. (debug but... my guess is websocket is hung and I just need to look at the time between game steps on a frame draw and if its been to long, termiante)
  - Override back button
  - Mobile app container (?)
  - Backspace in chat causes browser back sometimes
*/

/* Finalizing list ::
  ==== Core Functions ====
  / MERGE ALL CHANGES TO HTTPS, SETUP PAYPAL PROPERTIES
  / CONFIRM SECURITY OF SQL?
  + HTTPS + WSS Support
  + Database setup
  + Patch hash salting + using bcrypt for password storage
  + Storage of usersettings and userdata
  + Create filestore for user uploaded content
  + Setup secure paypal payment, credit/debit payment, and patreon support page
  + Email authentication
  + Password change
  + Setup server adress whitelist and info dao to store things props (done, confirm its safe, maybe add soms ssh keys or w/e)
  + Warn users if HW accel is off (Also look into report of bad performance ? (could be improved but it does exist and work)
  + Guest Mode
  + 20xx logging/Oak to a seperate file
  - Look into registering next frame draw at start of draw instead of end. (unlikely to have any effect)
  - look into frame skipping because of 33 / 30
  + Prod server bug, auto reconnect & file store oddity (seems fixed)
  * Syncrhonize access to sessions (appears to be done but should be tested)
  - Sound rework as needed
  / Email to SSL to fix non-deliver
  + Map data to HTTPS
  + Reverb
  ==== Creation of UserData ====
  + Implement statistics and credits
  + Implement unlocks (free unlocks & payed user unlocks)
  + Implement global ranking meter (lifetime credits)
  ==== Expansion of UserSettings ====
  + Volume slider expansion ... ( Master, Music, Announcer, Voice, SFX )
  + Custom colors, win sounds
  + Togglescape 2007 ( disable custom colors/sounds/skins/ui/etc )
  ==== Required Features. Will add ====
  ~ Create custom game menus (horrible implementation, rework)
  + Gametype expansion ( Assault, VIP, Bomber, Murder Mystery, Juggernaut, Rabbit, Elimination, Death Race, Tag, Mobile CTF, Payloadish, tug of war? )
  * Cache fixes
  * Map and Tileset Expansion ... ( 1 more tile set, 6~ new maps ) ( more maps, tile sets could use some improvements )
  / Replace illegal textures
  + Improve existing sky
  / Create a handful of alt skins for unlocks
  + Optimize decals, very very bad performance with them (mostly done)
  - Mobile Controls
  ==== Extra Features. Unlikely to add before release ====
  - Name display bugs and settings
  - Color Blind Mode
  - Display name
  - Improve chat log, it's very bad performance wise and lacks features
  - Move all javascript constants into the actual function instead of the constructor. Small performance change.
  - Spectator mode
  - More Skyboxes
  - Low graphics mode    ( Diffuse render only, very low end computer support ) (SAFEMODE)
  - Support for mobile controls
  - Support for controller
  - Twitch integration (very unlikely)
  - Sprays
  - Patterns
  - Delete fallback mode as we abandonded that system ages ago. (and ban emily)
  - Forgot username email
  - Ambidex touch control
  - Controller support on PC an phone
  ==== Commission Work ====
  - Voice work for ... Announcer, Fox, Shiek, Puff
  - Sound effect work for ... ( All SFX from melee need to be replaced )
  - Game Music ... (This is not likely to happen but possibly chiptune/synthwave music ? )
*/

/* SQL list additions -
  = Patreon Supporter Flag
  - More Gametype stats
  + UI toggles
  + New Volume sliders
  + Win message and unlock (validate input, check unlock)
  + Lag comp mode 0 1 2 3 4
  - safemode
+ unlock lobby creation
  -------------------------- box-6 crate-5 voxel-4 cargo-5 block-4 quad-4
  - Box Voice
  - Box Red
  - Box Rainbow Shine
  - Box Solid Gold +++++++++++ ADD ALL
  - NEBULA ++++++++++++ ADD ALL
  - Box Hitmarker
  - Box 4 Year Old
  - Box Blood Shine
  - Crate Voice 
  - Crate Orange
  - Crate Rainbow Shine
  - Crate Fireshine
  - Crate Blackflame
  - Voxel Voice (Edgey Meme)
  - Voxel Green
  - Voxel Rainbow Shine
  - Voxel Darkshine
  - Quad Voice
  - Quad Rainbow   +++++++++++++++++++++++++++++++
  - Quad Fire
  - Quad Legend
  - Quad Runescape
  - Block Voice
  - Block Rainbow   +++++++++++++++++++++++++++++++
  - Block Round
  - Block Windows 98
  - Block Volatile (explodey)
  - Block Roblock
  - Cargo Voice
  - Cargo Voice (InfernoPlus)
  - Cargo Rainbow   +++++++++++++++++++++++++++++++
  - Cargo Blackflame
  - Cargo Minecraft
*/

public abstract class GameLobby {
  protected final String lid; //Lobby ID
  
  protected final String name;
  
  public final int maxPlayers;
  protected final List<NoxioSession> players, loading;
  
  private final GameLoop loop; /* Seperate timer thread to trigger game steps */
  public final HttpThread httpToAuth; /* Seperate thread for sending post game stats over http */
  protected NoxioGame game; /* The actual game object */
  
  private final InputSync inputs; /* Packets that the game must handle are stored until a gamestep happens. This is for synchronization. */
  private final EventSync events; /* Second verse same as the first. */
  
  private final LobbySettings settings;
  
  private int gameCount;    // Number of times the game has ended and restarted
  protected boolean closed; // Clean this shit up!
  public GameLobby(final HttpThread http, final LobbySettings settings) throws IOException {
    lid = Salt.generate();
    this.settings = settings;
    
    name = settings.get("game_name", "Default Name");
    maxPlayers = settings.get("max_players", 6, 2, 64);
    
    players = new ArrayList();
    loading = new ArrayList();
    
    inputs = new InputSync();
    events = new EventSync();
    
    gameCount = 0;
    closed = false;
    
    newGame();

    loop = new GameLoop(this);
    httpToAuth = http;
  }
  
  /* It's apparently dangerous to start the thread in the constructor because ********REASONS********* so here we are! */
  public void start() { loop.start(); }
  
  private void newGame() throws IOException {
    if(game != null) { game.destroy(); }
    final GameSettings gs = settings.getRotation(gameCount);
    final String gametype = gs.get("gametype", "deathmatch");
    final NoxioMap map = new NoxioMap(gs.get("map_name", "final"));
    switch(gametype.toLowerCase()) {
      case "deathmatch" : { game = new Deathmatch(this, map, gs); break; }
      case "elimination" : { game = new Elimination(this, map, gs); break; }
      case "king" : { game = new King(this, map, gs); break; }
      case "ultimate" : { game = new Ultimate(this, map, gs); break; }
      case "rabbit" : { game = new Rabbit(this, map, gs); break; }
      case "tag" : { game = new Tag(this, map, gs); break; }
      case "teamdeathmatch" : { game = new TeamDeathmatch(this, map, gs); break; }
      case "teamelimination" : { game = new TeamElimination(this, map, gs); break; }
      case "capturetheflag" : { game = new CaptureTheFlag(this, map, gs); break; }
      case "freestyleflag" : { game = new FreestyleFlag(this, map, gs); break; }
      case "assault" : { game = new Assault(this, map, gs); break; }
      case "bomb" : { game = new BombingRun(this, map, gs); break; }
      case "teamking" : { game = new TeamKing(this, map, gs); break; }
      default : { game = new Deathmatch(this, map, gs); break; }
    }
    gameCount++;
  }

  public void step(final long tick) {
    try {
      handleEvents();
      if(game.isResetReady()) {
        newGame();
        inputs.pop();
        for(int i=0;i<players.size();i++) {
          final NoxioSession player = players.get(i);
          player.sendPacket(new PacketG17(name, game.gametypeName(), maxPlayers, game.scoreToWin, game.isTeamGame(), game.objectiveBaseId(), game.map)); /* This is one of the few packets we dont send in a blob because it has an odd state. */
          loading.add(player); /* We don't check for duplicates because if the situation arises where a player loading and a new game triggers we need them to return load finished twice. */
          /* @FIXME while the above comment describes what should happen this might need testing and maybe we need to ID our loads to make sure that the right load is done before allowing the player to join the game */
        }
        return; /* Screw you guys I'm going home. */
      }
      
      game.handlePackets(inputs.pop());  // Player Input
      game.step();                       // Game tick
      game.generateUpdatePackets(tick);  // Send updates to players
      game.post();                       // Clean up
      
    }
    catch(Exception ex) {
      Oak.log(Oak.Level.CRIT, "Game step exception ??? lobbyName=" + name + " ... gameOver=" + game.isGameOver(), ex);
      Oak.log(Oak.Level.ERR,"Attempting to close lobby nicely!");
      try { close("The Game Lobby encoutered an error and had to close. Sorry!"); Oak.log(Oak.Level.INFO, "Lobby closed correctly."); }
      catch(IOException ioex) {
        Oak.log(Oak.Level.ERR, "Failed to close lobby correctly! Ejecting players manually!", ioex);
        closed = true;
        for(int i=0;i<players.size();i++) {
          try { players.get(i).close(ex); }
          catch(Exception pioex) { Oak.log(Oak.Level.CRIT, "Very bad! Better start praying!", pioex); }
        }
      }
    }
  }
  
  /* Handles user connection/join/leave events */
  private void handleEvents() throws IOException { 
      final List<SessionEvent> evts = events.pop();
      for(int i=0;i<evts.size();i++) {
        SessionEvent evt = evts.get(i);
        switch(evt.getType()) {
          case "e00" : {
            if(!evt.getSession().isOpen()) { break; }                                     /* Check to make sure connection is still active. */
            if(!connect(evt.getSession())) { evt.getSession().sendPacket(new PacketG06("Connection failed.")); evt.getSession().leaveGame(); }
            else { GameLobbyInfo info = getInfo(); evt.getSession().sendPacket(
              new PacketG01(name, game.gametypeName(), maxPlayers, game.scoreToWin, game.isTeamGame(), game.objectiveBaseId(), game.map));
            }
            break;
          }
          case "e01" : {
            if(!evt.getSession().isOpen()) { break; } /* Check to make sure connection is still active. */
            if(!join(evt.getSession())) {
              remove(evt.getSession());
              evt.getSession().sendPacket(new PacketG06("Failed to join game."));
              evt.getSession().leaveGame();
            }
            else { sendPacket(new PacketG11(), evt.getSession()); }
            break;
          }
          case "e02" : {
            if(!evt.getSession().isOpen()) { break; } /* Check to make sure connection is still active. */
            leave(evt.getSession()); evt.getSession().sendPacket(new PacketG08()); /* Sent immiedately as client is ready to change state. */
            break;
          }
          case "e03" : {
            remove(evt.getSession());
            break;
          }
          default : { throw new IOException("Invalid SessionEvent type: " + evt.getType() + ". This really should never happen."); }
        }
      }
  }
   
  protected boolean connect(NoxioSession player) throws IOException {
    if(closed) { return false; }
    if(players.size() >= maxPlayers) { return false; }
    if(players.contains(player)) { player.close("Lobby Doppleganger Error."); return false; }
    players.add(player);
    loading.add(player);
    game.sendMessage(player.getDisplay() + " connected.");
    return true;
  }
  
  private boolean join(NoxioSession player) throws IOException {
    if(closed) { return false; }
    if(players.size() > maxPlayers) { return false; }
    if(!players.contains(player) || !loading.contains(player)) { remove(player); player.close("Lobby Ghost Error."); return false; } /* A thing that can happen and cause null pointers. Only possible if load times are long on clients and they leave lobby and come back AFAIK */
    loading.remove(player);
    game.join(player);
    game.sendMessage(player.getDisplay() + " joined the game.");
    return true;
  }
  
  protected void leave(NoxioSession player) throws IOException {
    if(!players.remove(player)) { return; } /* If the player attempting to leave is not in the game then don't bother with the rest of this. */
    while(loading.remove(player));
    game.leave(player);
    if(players.size() >= 1) { game.sendMessage(player.getDisplay() + " left the game."); }
  }
  
  public void remove(NoxioSession player) throws IOException {
    if(!players.remove(player)) { return; } /* If the player attempting to leave is not in the game then don't bother with the rest of this. */
    while(loading.remove(player));
    game.leave(player);
    if(players.size() >= 1) { game.sendMessage(player.getDisplay() + " disconnected."); }
  }
  
  public void remove(NoxioSession player, final String message) throws IOException { player.sendPacket(new PacketG06(message)); remove(player); }
  
  protected void close(final String message) throws IOException {
    sendPacket(new PacketG06(message));
    close();
  }
  
  protected void close() throws IOException {
    closed = true;
    for(int i=0;i<players.size();i++) {
      players.get(i).leaveGame();
    }
    game.close();
  }
  
  /* Send a packet to everyone in the lobby */
  public void sendPacket(final Packet p) {
    for(int i=0;i<players.size();i++) {
      players.get(i).sendPacket(p);
    }
  }
  
  /* Send a packet to a specific player with the given SID */
  public void sendPacket(final Packet p, final String sid) {
    for(int i=0;i<players.size();i++) {
      final NoxioSession player = players.get(i);
      if(player.getSessionId().equals(sid)) {
        player.sendPacket(p);
        return;
      }
    }
    Oak.log(Oak.Level.WARN, "Invalid User SID: '" + sid + "'");
  }
  
  /* Send a packet to a specific player */
  public void sendPacket(final Packet p, final NoxioSession player) {
    player.sendPacket(p);
  }
  
  public void pushInput(final String sid, final String data) { inputs.push(new InputData(sid, data)); }
  public void pushEvent(final SessionEvent evt) { events.push(evt); }
  
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
    
    private long lastStepTime;
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
          Oak.log(Oak.Level.CRIT, "Game loop thread interupted by exception!", ex);
          /* DO something about this... Not sure if this is a real problem or not, might report it in debug. */
        }
      }
    }
  }
  
  private class InputSync {
    private List<InputData> inputs;
    public InputSync() { inputs = new ArrayList(); }

    public void push(final InputData in) { syncInputAccess(false, in); }
    public List<InputData> pop() { return syncInputAccess(true, null); }

    /* Game Packet handling methods 
       - syncAccess.s == true / pop
       - syncAccess.s == false / push
    */
    private synchronized List<InputData> syncInputAccess(final boolean s, final InputData in) {
      if(s) {
        List<InputData> inps = inputs;
        inputs = new ArrayList();
        return inps;
      }
      else {
        if(in == null) { return null; }
        inputs.add(in);
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
  
  public class InputData {
    public final String sid, data;
    public InputData(final String sid, final String data) {
      this.sid = sid;
      this.data = data;
    }
  }
}
