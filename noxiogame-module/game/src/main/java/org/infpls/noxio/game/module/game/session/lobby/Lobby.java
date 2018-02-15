package org.infpls.noxio.game.module.game.session.lobby;

import com.google.gson.*;
import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.util.*;


public class Lobby extends SessionState {
  
  private final LobbyDao lobbyDao;
  
  public Lobby(final NoxioSession session, final LobbyDao lobbyDao) throws IOException {
    super(session);
    
    this.lobbyDao = lobbyDao;
    
    sendPacket(new PacketS00('b'));
  }
  
  /* Packet Info [ < outgoing | > incoming ]
     > b00 request game lobby list
     < b01 game lobby list
     > b02 close
     > b03 create lobby
     > b04 join lobby
     < b05 failed to join/craete lobby
  */
  
  @Override
  public void handlePacket(final String data) throws IOException {
    try {
      final Gson gson = new GsonBuilder().create();
      Packet p = gson.fromJson(data, Packet.class);
      if(p.getType() == null) { close("Invalid data: NULL TYPE"); return; } //Switch statements throw NullPointer if this happens.
      switch(p.getType()) {
        case "b00" : { getGameLobbyList(gson.fromJson(data, PacketB00.class)); break; }
        case "b02" : { close(); break; }
        case "b03" : { createLobby(gson.fromJson(data, PacketB03.class)); break; }
        case "b04" : { joinLobby(gson.fromJson(data, PacketB04.class)); break; }
        default : { /*close("Invalid data: " + p.getType()); */ break; }   /* @TODO: commented out to prevent game kick -> i00 packet error */
      }
    } catch(IOException | NullPointerException | JsonParseException ex) {
      close(ex);
    }
  }
  
  private void getGameLobbyList(PacketB00 p) throws IOException {
    List<GameLobbyInfo> info = lobbyDao.getLobbyInfo();
    sendPacket(new PacketB01(info));
  }
  
  private void createLobby(PacketB03 p) throws IOException {
    /* Make sure data is valid */
    if(!Validation.isAlphaNumericWithSpaces(p.getName())) {
      sendPacket(new PacketB05("Lobby name must be alpha-numeric characters only."));
      return;
    }
    if(p.getName().length() < 4) {
      sendPacket(new PacketB05("Lobby name must be at least 4 characters."));
      return;
    }
    if(p.getName().length() > 32) {
      sendPacket(new PacketB05("Lobby name cannot be longer than 32 characters."));
      return;
    }
    
    GameLobby gl = lobbyDao.createLobby(p.getName());
    if(gl != null) {
      session.joinGame(gl);
    }
    else {
      sendPacket(new PacketB05("Failed to create lobby."));
    }
  }
  
  private void joinLobby(PacketB04 p) throws IOException {
    GameLobby gl = lobbyDao.getLobby(p.getLid());
    if(gl != null) {
      session.joinGame(gl);
    }
    else {
      sendPacket(new PacketB05("Failed to join lobby."));
    }
  }
  
  @Override
  public void destroy() throws IOException {
    
  }
  
}
