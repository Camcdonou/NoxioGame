package org.infpls.noxio.game.module.game.session.login;

import com.google.gson.*;
import java.io.*;
import java.io.IOException;
import java.net.*;
import org.infpls.noxio.game.module.game.dao.server.ServerInfo;

import org.infpls.noxio.game.module.game.dao.server.InfoDao;
import org.infpls.noxio.game.module.game.dao.user.UserDao;
import org.infpls.noxio.game.module.game.session.*;


public class Login extends SessionState {
  
  private final UserDao userDao;
  private final InfoDao infoDao;
  
  public Login(final NoxioSession session, final UserDao userDao, final InfoDao infoDao) throws IOException {
    super(session);
    
    this.userDao = userDao;
    this.infoDao = infoDao;
    
    sendPacket(new PacketS00('l'));
  }
  
  /* Packet Info [ < outgoing | > incoming ]
     > l00 user login
     < l01 server info
     > l02 close
     < l03 validation check [ goes to noxioauth server ]
  */
  
  @Override
  public void handlePacket(final String data) throws IOException {
    try {
      final Gson gson = new GsonBuilder().create();
      Packet p = gson.fromJson(data, Packet.class);
      if(p.getType() == null) { close("Invalid data: NULL TYPE"); return; } //Switch statements throw Null Pointer if this happens.
      switch(p.getType()) {
        case "l00" : { userLogin(gson.fromJson(data, PacketL00.class)); break; }
        case "l02" : { close(); break; }
        default : { close("Invalid data: " + p.getType()); break; }
      }
    } catch(IOException | NullPointerException | JsonParseException ex) {
      close(ex);
    }
  }
  
  /* Validate user/sid then send back result */
  private void userLogin(final PacketL00 p) throws IOException {
    /* @FIXME This is a blocking method that does a HTTP GET */
    /* If you don't fix it in the next 3 commits I'll come to your house and kill you */
    /* I know where you live. */

    /* @FIXME also the app name is hardcoded here, make that a prop later. */
    final String address = "http://" + infoDao.getAuthServerAddress() + "/noxioauth/validate/" + p.getUser() + "/" + p.getSid();
    StringBuilder result = new StringBuilder();
    URL url = new URL(address);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
       result.append(line);
    }
    rd.close();
    
    Gson gson = new GsonBuilder().create();
    Packet pkt = gson.fromJson(result.toString(), Packet.class);
    
    if(pkt.getType().equals("l04")) {
      final PacketL04 r = gson.fromJson(result.toString(), PacketL04.class);
      ServerInfo info = infoDao.getServerInfo();
      sendPacket(new PacketL01(info.getName(), info.getLocation(), info.getDescription()));
      session.login(r.getData(), p.getSid());
    }
    else if(pkt.getType().equals("l03")) {
      final PacketL03 r = gson.fromJson(result.toString(), PacketL03.class);
      session.close("Failed to validate user: " + r.getMessage());
    }
    
    conn.disconnect();
  }

  @Override
  public void destroy() throws IOException {
    
  }
  
}
