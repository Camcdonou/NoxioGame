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

  */
  
  @Override
  public void handlePacket(final String data) throws IOException {
    try {
      final Gson gson = new GsonBuilder().create();
      Packet p = gson.fromJson(data, Packet.class);
      if(p.getType() == null) { close("Invalid data: NULL TYPE"); return; } //Switch statements throw NullPointer if this happens.
      switch(p.getType()) {
        case "g02" : { close(); break; }
        default : { close("Invalid data: " + p.getType()); break; }
      }
    } catch(IOException | NullPointerException | JsonParseException ex) {
      close(ex);
    }
  }
  
  @Override
  public void destroy() throws IOException {
    
  }
  
}
