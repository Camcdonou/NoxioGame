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
    < g05 DEBUG @FIXME
    < g06 failed to join game
  */
  
  @Override
  public void handlePacket(final String data) throws IOException {
    try {
      final Gson gson = new GsonBuilder().create();
      Packet p = gson.fromJson(data, Packet.class);
      if(p.getType() == null) { close("Invalid data: NULL TYPE"); return; } //Switch statements throw NullPointer if this happens.
      switch(p.getType()) {
        case "g00" : { clientReady(gson.fromJson(data, PacketG00.class)); break; }
        case "g02" : { close(); break; }
        case "g03" : { leaveGame(gson.fromJson(data, PacketG03.class)); break; }
        default : { close("Invalid data: " + p.getType()); break; }
      }
    } catch(IOException | NullPointerException | JsonParseException ex) {
      close(ex);
    }
  }
  
  private void clientReady(PacketG00 p) throws IOException {
    GameLobbyInfo info = lobby.getInfo();
    sendPacket(new PacketG01(info.getName(), info.getMaxPlayers()));
    
    if(!lobby.join(this.session)) {
      sendPacket(new PacketG06("Failed to join game."));
      session.leaveGame();
    }
  }
  
  private void leaveGame(PacketG03 p) throws IOException {
    session.leaveGame();
  }
  
  @Override
  public void destroy() throws IOException {
    lobby.leave(this.session);
  }
  
}
