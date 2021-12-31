package org.infpls.noxio.game.module.game.session.lobby;

import com.google.gson.*;
import java.io.IOException;
import java.util.*;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
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
     > b11 request quickmatch
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
        case "b06" : { joinLobbyByName(gson.fromJson(data, PacketB06.class)); break; }
        case "b11" : { joinLobbyAuto(gson.fromJson(data, PacketB11.class)); break; }
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
    /* Make sure user has permission to make a lobby */
    if(!session.getUserData().unlocks.has(UserUnlocks.Key.FT_LOBBY)) {
      sendPacket(new PacketB05("You do not have the ability to create custom lobbies.")); return;
    }
    /* Make sure data is valid */
    final LobbySettings ls = LobbySettings.parseSettings(p.getSettings());
    if(ls == null) {
      sendPacket(new PacketB05("Syntax error in settings data. Failed to parse.")); return;
    }
    
    final String gameName = ls.get("game_name", session.getUser() + "s Lobby");
    if(!Validation.isAlphaNumericWithSpaces(gameName)) {
      sendPacket(new PacketB05("Lobby name must be alpha-numeric characters only.")); return;
    }
    
    if(gameName.trim().length() < 3 || gameName.length() > 24) {
      sendPacket(new PacketB05("Lobby name must be between 3 and 24 characters.")); return;
    }
    
    if(!Validation.isAlphaNumericWithSpaces(ls.getRotation(0).get("map_name", "final"))) {
      sendPacket(new PacketB05("Stop trying to break things you jerk.")); return;
    }

    GameLobby gl = lobbyDao.createLobby(ls);
    if(gl != null) {
      session.joinGame(gl);
    }
    else {
      sendPacket(new PacketB05("Error during lobby creation."));
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
  
    private void joinLobbyByName(PacketB06 p) throws IOException {
    if(p.getLobbyName() == null) { sendPacket(new PacketB05("Failed to join lobby.")); }
    
    GameLobby gl = lobbyDao.getLobbyByName(p.getLobbyName());
    if(gl != null) {
      session.joinGame(gl);
    }
    else {
      sendPacket(new PacketB05("Failed to join lobby."));
    }
  }
  
  private void joinLobbyAuto(PacketB11 p) throws IOException {
    GameLobby gl = lobbyDao.getLobbyAuto();
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
