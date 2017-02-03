package org.infpls.noxio.game.module.game.session.ingame;

import com.google.gson.*;
import java.io.IOException;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;


public class InGame extends SessionState {
  
  private final GameLobby lobby;
  
  public InGame(final NoxioSession session, final GameLobby lobby) throws IOException {
    super(session);
    
    this.lobby = lobby;
    
    sendPacket(new PacketS00('g'));
  }
  
  /* Packet Info [ < outgoing | > incoming ]
    > g00 client ready
    < g01 game info
    > g02 close
    > g03 leave game
    < g04 players list
    < g05 game step finish / tells client to parse and render @FIXME still not 100% onboard with this style
    < g06 failed to join game
    > g07 load done
    < g08 left game lobby
    > g09 client game closed (ready to change state)
  
    < g10 create object
    < g11 delete object
    < g12 update object pos/vel
    < g13 shine ability used (visual effect)
    < g14 scoreboard update
    < g15 sys message
    < g16 game over
    < g17 new game
    < g18 game rules (custom gametype settings such as kills to win or number of teams)
  
    > i00 mouse active
    > i01 mouse neutral
    > i02 player request spawn
    < i03 player object control
    > i10 player use action
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
        case "i00" : { lobby.pushPacket(gson.fromJson(data, PacketI00.class).setSrcSid(session.getSessionId())); break; }
        case "i01" : { lobby.pushPacket(gson.fromJson(data, PacketI01.class).setSrcSid(session.getSessionId())); break; }
        case "i02" : { lobby.pushPacket(gson.fromJson(data, PacketI02.class).setSrcSid(session.getSessionId())); break; }
        case "i10" : { lobby.pushPacket(gson.fromJson(data, PacketI10.class).setSrcSid(session.getSessionId())); break; }
        default : { close("Invalid data: " + p.getType()); break; }
      }
    } catch(Exception ex) { /* @FIXME IOException | NullPointerException | JsonParseException */
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
