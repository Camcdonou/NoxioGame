package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.ingame.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.util.Oak;

public abstract class NoxioGame {
  
  public final GameLobby lobby;
  
  public final int respawnTime;       // Number of frames that a respawn takes
  public final int penaltyTime;       // Number of extra frames you wait if penalized for team kill or w/e
  
  protected int frame;
  private boolean gameOver;
  private int resetTimer;
   
  public final NoxioMap map;
  
  protected final List<Controller> controllers; // Player controller objects 
  public final List<GameObject> objects;        // Objects populating the game world
    
  private int idGen; /* Used to generate OIDs for objects. */
  public NoxioGame(final GameLobby lobby, final NoxioMap map, final GameSettings settings) throws IOException {
    this.lobby = lobby;
    
    this.map = map;
    
    controllers = new ArrayList();
    objects = new ArrayList();
    
    created = new ArrayList();
    deleted = new ArrayList();
    update = new ArrayList();
    
    respawnTime = settings.get("respawn_time", 30);
    penaltyTime = settings.get("penalty_time", 90);
    
    frame = 0;
    gameOver = false;
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
      else {
        Oak.log("Invalid User Input :: " + (c!=null?c.getUser():"<NULL_CONTROLLER>") + "@NoxioGame.handlePackets", 1);
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
      if(obj.isDead()) {
        deleteObject(obj); i--;
        obj.destroy();
      }
      else { obj.step(); }
    }
    frame++;
  }
  
  /* Generates the game update packet for all non-localized updates. */
  /* EX: chat messages, object creation, object deletion */
  /* Arrays are commas seperated value lists such as 1,54,6,23,12 or big,fat,booty,blaster
  /* Table of different data structures that are generated --
      OBJ::CREATE   -  crt;<int oid>;<string type>;<vec2 pos>;<vec2 vel>;
      OBJ::DELETE   -  del;<int oid>;<vec2 pos>;
      SYS::SCORE    -  scr;<String gametype>;<String description>;<String[] players>;<String[] scores>;<float[] meter>;<float[] r>;<float[] g>;<float[] b>;
      SYS::MESSAGE  -  msg;<String message>;
      SYS::ANNOUNCE -  anc;<string code>;
      SYS::GAMEOVER -  end;<String winner>;
      DBG::TICK     -  tck;<long tick>;<long sent>;
  */
  private final List<GameObject> created, deleted;  // List of objects create/deleted on this frame. These changes come first in update data.
  protected final List<String> update;                // List of "impulse" updates on this frame. These are things that happen as single events such as chat messages.
  public void generateUpdatePackets(final long tick) {
    final StringBuilder sba = new StringBuilder(); // Append to start
    final StringBuilder sbb = new StringBuilder(); // Append to end
    
    sba.append("tck;"); sba.append(tick); sba.append(";");
    for(int i=0;i<created.size();i++) {
      final GameObject obj = created.get(i);
      sba.append("crt"); sba.append(";");
      sba.append(obj.getOid()); sba.append(";");
      sba.append(obj.type()); sba.append(";");
      obj.getPosition().toString(sba); sba.append(";");
      obj.getVelocity().toString(sba); sba.append(";");
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
  
  protected void generateJoinPacket(NoxioSession player) {
    final StringBuilder sb = new StringBuilder();
    for(int i=0;i<objects.size();i++) {
      final GameObject obj = objects.get(i);
      sb.append("crt"); sb.append(";");
      sb.append(obj.getOid()); sb.append(";");
      sb.append(obj.type()); sb.append(";");
      obj.getPosition().toString(sb); sb.append(";");
      obj.getVelocity().toString(sb); sb.append(";");
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
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getControlled() == obj) {
        return controllers.get(i);
      }
    }
    return null;
  }
  
  protected abstract void spawnPlayer(final Controller c, final Queue<String> q);
  protected final Player makePlayerObject(final String id, final Vec2 pos) { return makePlayerObject(id, pos, -1); }
  protected final Player makePlayerObject(final String id, final Vec2 pos, final int team) {
    switch(id) {
      case "inf" : { return new Inferno(this, createOid(), pos, team); }
      case "fox" : { return new Fox(this, createOid(), pos, team); }
      case "flc" : { return new Falco(this, createOid(), pos, team); }
      case "mar" : { return new Marth(this, createOid(), pos, team); }
      case "shk" : { return new Shiek(this, createOid(), pos, team); }
      case "puf" : { return new Puff(this, createOid(), pos, team); }
      case "cap" : { return new Captain(this, createOid(), pos, team); }
      default : { return new Fox(this, createOid(), pos, team); }
    }
  }
  
  private final static float SAFE_SPAWN_RADIUS = 2.5f;
  protected final Vec2 findSafeSpawn(final List<NoxioMap.Spawn> spawns) {
    if(spawns.isEmpty()) { return new Vec2(map.getBounds()[0]*0.5f, map.getBounds()[1]*0.5f); } // Fallback
    
    final List<NoxioMap.Spawn> sss = new ArrayList();
    
    for(int i=0;i<spawns.size();i++) {
      final NoxioMap.Spawn sp = spawns.get(i);
      boolean safe = true;
      for(int j=0;j<controllers.size();j++) {
        final GameObject obj = controllers.get(j).getControlled();
        if(obj != null) {
          float k = obj.getPosition().distance(sp.getPos());
          if(k < SAFE_SPAWN_RADIUS) { safe = false; break; }
        }
      }
      if(safe) { sss.add(sp); }
    }
    
    if(sss.isEmpty()) { return spawns.get((int)(Math.random()*spawns.size())).getPos(); }
    else              { return sss.get((int)(Math.random()*sss.size())).getPos(); }
  }
  
  public void join(final NoxioSession player) throws IOException {
    controllers.add(new Controller(this, player.getUser(), player.getSessionId()));
    generateJoinPacket(player);
    updateScore();
  }
  
  public void leave(final NoxioSession player) {
    for(int i=0;i<controllers.size();i++) {
      if(controllers.get(i).getSid().equals(player.getSessionId())) {
        controllers.get(i).destroy();
        controllers.remove(i);
        return;
      }
    }
    updateScore();
  }
  
  public abstract void requestTeamChange(final Controller controller, final Queue<String> q);
  
  public abstract void reportKill(final Controller killer, final GameObject killed);
  public abstract void reportObjective(final Controller player, final GameObject objective);
  
  /* Called after each score change, announces gametype specific events */ // @TODO: not called anywhere in super class, currently has subclass implement call. weird. mabye fix this?
  public abstract void announceObjective();
  /* Called after <killer> has scored a kill on <killed>, if there was a multi or a spree it is announced */
  public final void announceKill(final Controller killer, final Controller killed) {
    if(killer.getTeam() != -1 && killer.getTeam() == killed.getTeam()) {
      killer.penalize();
      killed.getScore().death();
      killer.announce("btl"); killed.announce("btd");
      return;
    }
    killer.getScore().kill(frame);
    int kjc = killed.getScore().death();
    if(kjc >= 5 && kjc < 10) { killer.announce("kj"); }
    else if(kjc >= 10) { announce("er," + killer.getUser() + "," + killed.getUser()); sendMessage("Killjoy (" + killer.getUser() + ")"); }
    final int m = killer.getScore().getMulti();
    if(m > 1) {
      switch(m) {
        case 2  : { killer.announce("mk,2"); break; }
        case 3  : { killer.announce("mk,3"); break; }
        case 4  : { killer.announce("mk,4"); sendMessage("Multikill X4 (" + killer.getUser() + ")"); break; }
        case 5  : { killer.announce("mk,5"); sendMessage("Multikill X5 (" + killer.getUser() + ")"); break; }
        case 6  : { killer.announce("mk,6"); sendMessage("Multikill X6 (" + killer.getUser() + ")"); break; }
        case 7  : { killer.announce("mk,7"); sendMessage("Multikill X7 (" + killer.getUser() + ")"); break; }
        case 8  : { killer.announce("mk,8"); sendMessage("Multikill X8 (" + killer.getUser() + ")"); break; }
        default : { killer.announce("mk,9"); sendMessage("Multikill X" + m + " (" + killer.getUser() + ")"); break; }
      }
    }
    final int s = killer.getScore().getSpree();
    switch(s) {
      case 5  : { killer.announce("sp,5"); break; }
      case 10 : { killer.announce("sp,10"); announce("oc,"+killer.getUser()); sendMessage("Killing Spree X10 (" + killer.getUser() + ")"); break; }
      case 15 : { killer.announce("sp,15"); announce("oc,"+killer.getUser()); sendMessage("Killing Spree X15 (" + killer.getUser() + ")"); break; }
      case 20 : { killer.announce("sp,20"); announce("oc,"+killer.getUser()); sendMessage("Killing Spree X20 (" + killer.getUser() + ")"); break; }
      case 25 : { killer.announce("sp,25"); announce("oc,"+killer.getUser()); sendMessage("Killing Spree X25 (" + killer.getUser() + ")"); break; }
      default : { break; }
    }
  }
  
  /* Annouce Codes;
    mk,# :: Multikill <# 0 to 9>
    sp,# :: Spree     <# 5, 10, 15, 20, 25>
    oc,@ :: Out of control <@ name of killer> 
    kj   :: Killjoy
    er,@,&  :: Ended Reign  <@ name of player> <& name of killed>
    fb,@ :: First Blood <@ name of player>
    gl   :: Gained the lead
    ll   :: Lost the lead
    btd  :: Betrayed   
    btl  :: Betrayl
    dm   :: Deathmatch
    kh   :: King
    tkh  :: Team King
    ulf  :: Ultimate Lifeform
    tdm  :: Team Deathmatch
    ctf  :: Capture the Flag
    khm  :: Hill Moved
    hc   :: New Ultimate Lifeform
    bs   :: Blue Team Score
    rs   :: Red Team Score
    bft  :: Blue Team Flag Taken
    bfr  :: Blue Team Flag Return
    rft  :: Red Team Flag Taken
    rfr  :: Red Team Flag Return
    1m   :: 1 Minute Remaining
    pf   :: Perfect
    hu   :: Humiliation
    go   :: Game Over
  */
  public final void announce(final String code) {
    update.add("anc;"+code+";");
  }
  
  public abstract void updateScore();
  
  /* Send a message that will appear in all players chatlog */
  public void sendMessage(final String msg) {
    update.add("msg;"+msg+";");
  }
  
  public void gameOver(String msg) {
    announce("go");
    update.add("end;"+msg+";");
    gameOver = true; resetTimer = 210;
  }
  
  public void close() {
    /* Do things! */
  }
  
  public int getFrame() { return frame; }
  public boolean isGameOver() { return gameOver; }                              // Game over, resetTimer counting down to new game
  public boolean isResetReady() { return resetTimer < 1 && gameOver; }          // Ready to start a new game
  public final int createOid() { return idGen++; }
  public abstract String gametypeName();
  public abstract int objectiveBaseId();                                        // ID for gametype objective, 0 = none, 1 = ctf flags, etc...
}
