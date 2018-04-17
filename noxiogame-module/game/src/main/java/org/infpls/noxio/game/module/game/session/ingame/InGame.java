package org.infpls.noxio.game.module.game.session.ingame;

import com.google.gson.*;
import java.io.IOException;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.util.Oak;


public class InGame extends SessionState {
  
  private final GameLobby lobby;
  
  public InGame(final NoxioSession session, final GameLobby lobby) throws IOException {
    super(session);
    
    this.lobby = lobby;
    
    sendPacket(new PacketS00('g'));
  }
  
  /* Packet Info [ < outgoing | > incoming ]
    > g00 client ready
    < g01 game info && map file
    > g02 close
    > g03 leave game
    < g06 transversal error/info (generic message)
    > g07 load done
    < g08 left game lobby
    > g09 client game closed (ready to change state)
    < g11 join success (tells client they can start gameplay. this prevents early input packets causing a kick)
  
    < g10 the game update packet - see NoxioGame & Controller class for details
  
    < g17 new game                  
    
    > i00 input data blob
    -> 01 mouse neutral
    -> 02 player request spawn
    -> 04 mouse move
    -> 05 use action
    -> 06 request new game
    -> 07 request team change
    -> 08 chat message
  */
  
  @Override
  public void handlePacket(final String data) throws IOException {
    try {
      final Gson gson = new GsonBuilder().create();
      Packet p = gson.fromJson(data, Packet.class);
      if(p.getType() == null) { close("Invalid data: NULL TYPE"); return; } //Switch statements throw NullPointer if this happens.
      switch(p.getType()) {
        /* Session Type Packets g0x */
        case "g00" : { clientReady(gson.fromJson(data, PacketG00.class)); break; }
        case "g02" : { close(); break; }
        case "g03" : { leaveGame(gson.fromJson(data, PacketG03.class)); break; }
        case "g07" : { loadDone(gson.fromJson(data, PacketG07.class)); break; }
        case "g09" : { session.leaveGame(); break; }
        
        /* Ingame Type Packets gxx */
        
        /* Input Type Packets ixx */
        case "i00" : { lobby.pushInput(session.getSessionId(), gson.fromJson(data, PacketI00.class).getData()); break; }
        default : { close("Invalid data: " + p.getType()); break; }
      }
    } catch(Exception ex) { /* IOException | NullPointerException | JsonParseException */
      Oak.log(Oak.Level.WARN, "User: '" + session.getUser() + "' threw Unknown Exception", ex);
      close(ex);
    }
  }
  
  /* SessionEvent Info
    - e00 client ready
    - e01 load done
    - e02 leave game
    - e03 remove from game
  */
  
  private void clientReady(PacketG00 p) throws IOException { lobby.pushEvent(new SessionE00(session)); }
  private void loadDone(PacketG07 p) throws IOException { lobby.pushEvent(new SessionE01(session)); }
  private void leaveGame(PacketG03 p) throws IOException { lobby.pushEvent(new SessionE02(session)); }
  
  @Override
  public void destroy() throws IOException { lobby.pushEvent(new SessionE03(session)); }
}
