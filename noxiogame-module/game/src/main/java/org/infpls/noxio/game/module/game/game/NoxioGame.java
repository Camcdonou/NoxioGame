package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.session.PacketH01;
import org.infpls.noxio.game.module.game.util.Oak;

public abstract class NoxioGame {
  
  public final GameLobby lobby;
  
  public final int scoreToWin;
  public final int respawnTime;       // Number of frames that a respawn takes
  public final int penaltyTime;       // Number of extra frames you wait if penalized for team kill or w/e
  
  public boolean disableCustomColor;    // Disables use of custom colors for this gamemode. Used for gamemodes like Ulitmate and Tag.
  
  protected int frame;
  private boolean gameOver;
  private int resetTimer;
   
  public final NoxioMap map;
  
  protected final List<Controller> controllers; // Player controller objects 
  public final List<GameObject> objects;        // Objects populating the game world
    
  private int idGen; /* Used to generate OIDs for objects. */
  public NoxioGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings, int stw) throws IOException {
    this.lobby = lobby;
    
    this.map = map;
    
    controllers = new ArrayList();
    objects = new ArrayList();
    
    created = new ArrayList();
    deleted = new ArrayList();
    update = new ArrayList();
    
    scoreToWin = stw;
    respawnTime = settings.get("respawn_time", 45, 0, 300);
    penaltyTime = settings.get("penalty_time", 90, 0, 300);
    
    disableCustomColor = false;
    
    frame = 0;
    gameOver = false;
    
    createMapObjects();
  }
    
  public void handlePackets(final List<GameLobby.InputData> inputs) {
    for(int i=0;i<inputs.size();i++) {
      final GameLobby.InputData in = inputs.get(i);
      Controller c = getController(in.sid);
      if(c != null) {
        final String[] spl = in.data.split(";");
        final Queue<String> queue = new LinkedList(Arrays.asList(spl));
        for(int j=0;j<queue.size();j++) {
          final String id = queue.remove();
          switch(id) {
            case "02" : { spawnPlayer(c, queue);  break; }
            case "07" : { requestTeamChange(c, queue); break; }
            default : { c.handlePacket(id, queue); break; }
          }
        }
      }
    }
  }
  
  public void step() {
    if(!gameOver) {
      for(int i=0;i<controllers.size();i++) {
        final Controller cont = controllers.get(i);
        cont.step();
      }
    }
    else {
     if(resetTimer > 0) { resetTimer--; }
    }
    for(int i=0;i<objects.size();i++) {
      final GameObject obj = objects.get(i);
      if(obj.destroyed()) {
        deleteObject(obj); i--;
        obj.onDelete();
      }
      else { obj.step(); }
    }
    frame++;
  }
  
  /* Generates the game update packet for all non-localized updates. */
  /* EX: chat messages, object creation, object deletion */
  /* Arrays are commas seperated value lists such as 1,54,6,23,12 or big,fat,booty,blaster
  /* Table of different data structures that are generated --
      OBJ::CREATE   -  crt;<String type>;<int oid>;<vec2 pos>;<int permutation>;<int team>;<int color>;
      OBJ::DELETE   -  del;<int oid>;<vec2 pos>;
      SYS::SCORE    -  scr;<String gametype>;<String description>;<String[] players>;<String[] scores>;<float[] meter>;<float[] r>;<float[] g>;<float[] b>;
      SYS::MESSAGE  -  msg;<String message>;
      SYS::ANNOUNCE -  anc;<String code>;
      SYS::TIMER    -  tim;<String title>;<int frameTime>;     // Frame time is the number of frames till this timer is finished.
      SYS::LOADSND  -  snd;<String customSoundFile>;
      SYS::GAMEOVER -  end;<String head>;<String foot>;<String customSoundFile>;
      DBG::TICK     -  tck;<long tick>;<long sent>;
  */
  private final List<GameObject> created, deleted;   // List of objects create/deleted on this frame. These changes come first in update data.
  protected final List<String> update;               // List of "impulse" updates on this frame. These are things that happen as single events such as chat messages.
  public final void generateUpdatePackets(final long tick) {
    final StringBuilder sba = new StringBuilder(); // Append to start
    final StringBuilder sbb = new StringBuilder(); // Append to end
    
    sba.append("tck;"); sba.append(tick); sba.append(";");
    for(int i=0;i<created.size();i++) {
      final GameObject obj = created.get(i);
      sba.append("crt"); sba.append(";");
      sba.append(obj.getOid()); sba.append(";");
      sba.append(obj.type()); sba.append(";");
      obj.getPosition().toString(sba); sba.append(";");
      sba.append(obj.permutation); sba.append(";");
      sba.append(obj.team); sba.append(";");
      sba.append(obj.color()); sba.append(";");
    }
    for(int i=0;i<deleted.size();i++) {
      final GameObject obj = deleted.get(i);
      sba.append("del"); sba.append(";");
      sba.append(obj.getOid()); sba.append(";");
      obj.getPosition().toString(sba); sba.append(";");
    }
    for(int i=0;i<update.size();i++) {
      sbb.append(update.get(i));
    }
    
    created.clear(); deleted.clear(); update.clear();
    
    final String globalPreData = sba.toString();
    final String globalPostData = sbb.toString();
    for(int i=0;i<controllers.size();i++) {
      final StringBuilder sbc = new StringBuilder();
      sbc.append(globalPreData);
      controllers.get(i).generateUpdateData(sbc);
      sbc.append(globalPostData);
      Packet p = new PacketG10(sbc.toString());
      lobby.sendPacket(p, controllers.get(i).getSid());
    }
  }
  
  /* Called after sending data to clients. Used for final cleanup of each tick. */
  public void post() {
    for(int i=0;i<objects.size();i++) {
      objects.get(i).post();
    }
  }
  
  protected final void generateJoinPacket(NoxioSession player) {
    final StringBuilder sb = new StringBuilder();
    for(int i=0;i<objects.size();i++) {
      final GameObject obj = objects.get(i);
      sb.append("crt"); sb.append(";");
      sb.append(obj.getOid()); sb.append(";");
      sb.append(obj.type()); sb.append(";");
      obj.getPosition().toString(sb); sb.append(";");
      sb.append(obj.permutation); sb.append(";");
      sb.append(obj.team); sb.append(";");
      sb.append(obj.color()); sb.append(";");
    }
    for(int i=0;i<controllers.size();i++) {
      final String csm = controllers.get(i).getCustomSound();
      if(!csm.equals("")) { sb.append("snd;"); sb.append(csm); sb.append(";"); }
    }
    lobby.sendPacket(new PacketG10(sb.toString()), player);
  }
  
  public final void addObject(final GameObject obj) {
    objects.add(obj);
    created.add(obj);
  }
  
  public final void deleteObject(final GameObject obj) {
    objects.remove(obj);
    deleted.add(obj);
  }
  
  /* Returns controller for given SID */
  public Controller getController(final String sid) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getSid().equals(sid)) {
        return controllers.get(i);
      }
    }
    return null;
  }
  
  /* Returns controller for given Object */
  public Controller getControllerByObject(final GameObject obj) {
    final GameObject o = obj instanceof PolyBit ? ((PolyBit)obj).owner : obj;
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getControlled() == o) {
        return controllers.get(i);
      }
    }
    return null;
  }
  
    
  /* Spawns special objects placed in the map like teleporters or bumpers */
  public final void createMapObjects() {
    /* Teleporters */
    final List<NoxioMap.Spawn> entrances = map.getSpawns("telenter", gametypeId());
    for(int i=0;i<entrances.size();i++) {
      final NoxioMap.Spawn ent = entrances.get(i);
      final List<NoxioMap.Spawn> exits = map.getSpawns("telexit", gametypeId(), ent.getTeam());
      
      if(exits.size() > 0) {
        final Telexit exit = new Telexit(this, createOid(), exits.get(0).getPos());
        final Telenter entrance = new Telenter(this, createOid(), ent.getPos(), exit);
        addObject(exit);
        addObject(entrance);
      }
    }
    
    /* Voids */
    final List<NoxioMap.Spawn> voids = map.getSpawns("void", gametypeId());
    for(int i=0;i<voids.size();i++) {
      final NoxioMap.Spawn v = voids.get(i);
      
      final VoidZone vz = new VoidZone(this, createOid(), v.getPos(), v.getTeam());
      addObject(vz);
    }
    
    /* Bumper */
    final List<NoxioMap.Spawn> bumpers = map.getSpawns("bumper", gametypeId());
    for(int i=0;i<bumpers.size();i++) {
      final NoxioMap.Spawn bump = bumpers.get(i);
      
      final Bumper bumper = new Bumper(this, createOid(), bump.getPos());
      addObject(bumper);
    }
    
    /* Jumper */
    final List<NoxioMap.Spawn> jumpers = map.getSpawns("jumper", gametypeId());
    for(int i=0;i<jumpers.size();i++) {
      final NoxioMap.Spawn jump = jumpers.get(i);
      
      final Jumper jumper = new Jumper(this, createOid(), jump.getPos());
      addObject(jumper);
    }
    
  }
  
  protected abstract boolean spawnPlayer(final Controller c, final Queue<String> q); // Returns true is player is spawned
  protected final Player makePlayerObject(final Controller c, final String id, final Vec2 pos) { return makePlayerObject(c, id, pos, -1); }
  protected final Player makePlayerObject(final Controller c, final String id, final Vec2 pos, final int team) {
    /* @TODO: use reflection to meme this in a more efficent way? very low priority */
    final int color = c.user.unlocks.has(UserUnlocks.Key.FT_COLOR)&&!disableCustomColor?c.user.settings.game.getColor(team):0;
    
    /* BOX_x :: Box.java */
    for(Box.Permutation perm : Box.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Box po = new Box(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
      
    /* CRT_x :: Crate.java */
    for(Crate.Permutation perm : Crate.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Crate po = new Crate(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
      
    /* QUA_x :: Quad.java */
    for(Quad.Permutation perm : Quad.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Quad po = new Quad(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
      
    /* VOX_x :: Voxel.java */
    for(Voxel.Permutation perm : Voxel.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Voxel po = new Voxel(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
      
    /* BLK_x :: Block.java */
    for(Block.Permutation perm : Block.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Block po = new Block(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
      
    /* CRG_x :: Cargo.java */
    for(Cargo.Permutation perm : Cargo.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Cargo po = new Cargo(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
    
    /* CUB_x :: Cube.java */
    for(Cube.Permutation perm : Cube.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Cube po = new Cube(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
    
    /* PLY_x :: Poly.java */
    for(Poly.Permutation perm : Poly.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Poly po = new Poly(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
    
    /* XOB_x :: Xob.java */
    for(Xob.Permutation perm : Xob.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Xob po = new Xob(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
    
    /* INF_x :: Inferno.java */
    for(Inferno.Permutation perm : Inferno.Permutation.values()) {
      if(perm.name().equalsIgnoreCase(id) && c.user.unlocks.has(perm.unlock)) {
        final Inferno po = new Inferno(this, createOid(), pos, perm, team);
        po.setColor(color);
        return po;
      }
    }
    
    return new Box(this, createOid(), pos, Box.Permutation.BOX_N, team); /* Default */
  }
  
  private final static float SPAWN_SAFE = 5.0f, SPAWN_MIN_SAFE = 3.f;
  protected final Vec2 findSafeSpawn(final List<NoxioMap.Spawn> spawns) {
    if(spawns.isEmpty()) { return new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f); } // Fallback
    
    final List<NoxioMap.Spawn> safe = new ArrayList(), minSafe = new ArrayList();
    
    /* Strategy #1 - Max safe distance + random choice */
    for(int i=0;i<spawns.size();i++) {
      final NoxioMap.Spawn sp = spawns.get(i);
      boolean isSafe = true;
      boolean isMinSafe = true;
      for(int j=0;j<controllers.size();j++) {
        final GameObject obj = controllers.get(j).getControlled();
        if(obj != null) {
          float dist = obj.getPosition().distance(sp.getPos());
          if(dist < SPAWN_SAFE) { isSafe = false; }
          if(dist < SPAWN_MIN_SAFE) { isMinSafe = false; }
        }
      }
      if(isSafe) { safe.add(sp); }
      if(isMinSafe) { minSafe.add(sp); }
    }
    
    /* Strategy #2 - Add min safe distance spawns */
    if(safe.size() < 3) {
      for(int i=0;i<minSafe.size();i++) {
        safe.add(minSafe.get(i));
      }
    }
    
    /* Strategy #3 - Find spawn point with the most clearance */
    if(safe.isEmpty()) {
      NoxioMap.Spawn safest = spawns.get(0);
      float clearance = 0.f;
      for(int i=0;i<spawns.size();i++) {
        final NoxioMap.Spawn sp = spawns.get(i);
        float nearest = SPAWN_SAFE;
        
        for(int j=0;j<controllers.size();j++) {
          final GameObject obj = controllers.get(j).getControlled();
          if(obj != null) {
            float dist = obj.getPosition().distance(sp.getPos());
            if(dist < nearest) { nearest = dist; }
          }
        }
        if(nearest > clearance) { safest = sp; clearance = nearest; }
      }
      safe.add(safest);
    }
    
    return safe.get((int)(Math.random()*safe.size())).getPos();
  }
  
  public void join(final NoxioSession player) throws IOException {
    final Controller c = new Controller(this, player);
    controllers.add(c);
    generateJoinPacket(player);
    updateScore();
    final String csm = c.getCustomSound();
    if(!csm.equals("")) { update.add("snd;" + csm + ";"); }
  }
  
  public void leave(final NoxioSession player) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getSid().equals(player.getSessionId())) {
        final String usr = controllers.get(i).getUser();
        sendStats(usr, controllers.get(i).destroy());
        controllers.remove(i);
        break;
      }
    }
    updateScore();
  }
  
  public abstract void requestTeamChange(final Controller controller, final Queue<String> q);
  
  public abstract void reportKill(final Controller killer, final GameObject killed);
  public abstract void reportObjective(final Controller player, final GameObject objective);
  public void reportTouch(Mobile a, Mobile b) { } /* Only used by the Tag gametype so generally an empty function. */
  
  /* Called after each score change, announces gametype specific events */ // @TODO: not called anywhere in super class, currently has subclass implement call. weird. mabye fix this?
  public abstract void announceObjective();
  
  /* Called after <killer> has scored a kill on <killed>, if there was a multi or a spree it is announced */
  /* Returns true if the kill was (1. a player killing another player, 2. the players were on opposing teams or no team). Otherwise false. */
  /* Also records any stats to the the Score class */
  private boolean firstBlood = false; // Flag to check if first blood has been awarded or not!
  public final boolean announceKill(final Controller killer, final Controller killed) {
    if(killed == null) { return false; }
    int kjc = killed.score.death();
    
    if(killer == null || killer == killed) { return false; }
    
    if(killer.getTeam() != -1 && killer.getTeam() == killed.getTeam()) {
      killer.penalize();
      killer.announce("btl"); killer.score.betrayl();
      killed.announce("btd"); killed.score.betrayed();
      return false;
    }
    killer.score.kill(frame);
    if(!firstBlood) { announce("fb," + killer.getDisplay()); killer.score.firstBlood(); firstBlood = true; }
    if(kjc >= 5 && kjc < 15) { killer.announce("kj"); killer.score.killJoy(); }
    else if(kjc >= 15) { announce("er," + killer.getDisplay() + "," + killed.getDisplay()); sendMessage("Killjoy (" + killer.getDisplay() + ")"); killer.score.endedReign(); }
    final int m = killer.score.getMulti();
    if(m > 1 && m <= 18) {
      killer.announce("mk," + m);
      if(m > 4) { sendMessage("Multikill X" + m + " (" + killer.getDisplay() + ")"); }
    }
    final int s = killer.score.getSpree();
    switch(s) {
      case 5  : { killer.announce("sp,5"); break; }
      case 10 : { killer.announce("sp,10"); break; }
      case 15 : { killer.announce("sp,15"); announceExcluding(killer, "oc,"+killer.getDisplay()); sendMessage(killer.getDisplay() + " is unkillable!"); break; }
      case 20 : { killer.announce("sp,20"); announceExcluding(killer, "oc,"+killer.getDisplay()); sendMessage(killer.getDisplay() + " is invincible!"); break; }
      case 25 : { killer.announce("sp,25"); announceExcluding(killer, "oc,"+killer.getDisplay()); sendMessage(killer.getDisplay() + " is inconceivable!"); break; }
      case 30 : { killer.announce("sp,30"); announceExcluding(killer, "oc,"+killer.getDisplay()); sendMessage(killer.getDisplay() + " is godlike!"); break; }
      default : { break; }
    }
    return true;
  }
  
  /* Annouce Codes;
    mk,# :: Multikill <# 2 to 18>
    sp,# :: Spree     <# 5, 10, 15, 20, 25, 30>
    oc,@ :: Out of control <@ name of killer> 
    kj   :: Killjoy
    er,@,&  :: Ended Reign  <@ name of player> <& name of killed>
    fb,@ :: First Blood <@ name of player>
    gl   :: Gained the lead
    ll   :: Lost the lead
    btd  :: Betrayed   
    btl  :: Betrayl
    dm   :: Deathmatch
    em   :: Elimination
    kh   :: King
    rab  :: Rabbit
    tag  :: Tag
    tkh  :: Team King
    ult  :: Ultimate Lifeform
    tdm  :: Team Deathmatch
    tem  :: Team Elimination
    ctf  :: Capture the Flag
    fsf  :: Freestyle Flag
    ass  :: Assault
    cst  :: Custom Game
    khm  :: Hill Moved
    nu   :: New Ultimate Lifeform
    pow  :: Your Power Is Maximum
    it   :: You Are It
    fc   :: Flag Captured
    fl   :: Flag Lost
    ff   :: Flag Reset
    fr   :: Flag Return
    fs   :: Flag Stolen
    ft   :: Flag Taken
    off  :: Offense
    def  :: Defense
    rnd  :: Round Over
    t60  :: 1 Minute Remaining
    t30  :: 30 Seconds Reamining
    t10  :: 10 Seconds Remaining
    pf   :: Perfect
    hu   :: Humiliation
    go   :: Game Over
  */
  public final void announce(final String code) {
    update.add("anc;"+code+";");
  }
  
  /* Sends announce code to all players excluding <player> */
  public final void announceExcluding(final Controller player, final String code) {
    for(int i=0;i<controllers.size();i++) {
      final Controller c = controllers.get(i);
      if(c == player) { continue; }
      c.announce(code);
    }
  }
  
  public abstract void updateScore();
  
  /* Send a message that will appear in all players chatlog */
  public void sendMessage(final String msg) {
    update.add("msg;"+msg+";");
  }
    
  public void gameOver(String head, String foot, String soundFile) {
    announce("go");
    update.add("end;" + head + ";" + foot + ";" + soundFile + ";");
    gameOver = true; resetTimer = 210;
  }
  
  /* Sends stats recorded in this match to the auth server */
  public void sendStats(final String user, final Score.Stats stats) {
    lobby.httpToAuth.push(new PacketH01(user, stats));
  }
  
  public void destroy() {
    while(controllers.size() > 0) {
      final String usr = controllers.get(0).getUser();
      sendStats(usr, controllers.get(0).destroy());
      controllers.remove(0);
    }
  }
  
  public void close() {
    destroy();
  }
  
  public abstract int isTeamGame();                                             // Returns number of teams in ths game (generally 0 or 2 unless i add support for more)
  public int getFrame() { return frame; }
  public boolean isGameOver() { return gameOver; }                              // Game over, resetTimer counting down to new game
  public boolean isResetReady() { return resetTimer < 1 && gameOver; }          // Ready to start a new game
  public final int createOid() { return idGen++; }
  public final void setClientTimer(String title, int time) { update.add("tim;" + title + ";" + time + ";"); }
  public abstract String gametypeName();                                        // What is displayed to users EX: "Capture The Flag"
  public final String gametypeId() {                                            // What is used to identify the gametype EX: "capturetheflag"
    return gametypeName().toLowerCase().replaceAll(" ", "");
  }
  public abstract int objectiveBaseId();                                        // ID for gametype objective, 0 = none, 1 = ctf flags, etc...
}
