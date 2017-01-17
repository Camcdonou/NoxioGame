package org.infpls.noxio.game.module.game.session.lobby;

import com.google.gson.*;
import java.io.IOException;

import org.infpls.noxio.game.module.game.session.*;


public class Lobby extends SessionState {
  
  public Lobby(final NoxioSession session) throws IOException {
    super(session);
    
    sendPacket(new PacketS00('b'));
  }
  
  /* Packet Info [ < outgoing | > incoming ]
     > b00 ready
     > b02 close
  */
  
  @Override
  public void handlePacket(final String data) throws IOException {
    try {
      final Gson gson = new GsonBuilder().create();
      Packet p = gson.fromJson(data, Packet.class);
      if(p.getType() == null) { close("Invalid data: NULL TYPE"); return; } //Switch statements throw Null Pointer if this happens.
      switch(p.getType()) {
        case "b00" : { readyState(gson.fromJson(data, PacketB00.class)); break; }
        case "b02" : { close(); break; }
        default : { close("Invalid data: " + p.getType()); break; }
      }
    } catch(IOException | NullPointerException | JsonParseException ex) {
      close(ex);
    }
  }
  
  private void readyState(PacketB00 p) {
    /* doot */
  }
  
  @Override
  public void destroy() throws IOException {
    
  }
  
}
