package org.infpls.noxio.game.module.game.session.login;

import com.google.gson.*;
import java.io.*;
import java.io.IOException;
import java.net.*;

import org.infpls.noxio.game.module.game.session.*;
import org.infpls.noxio.game.module.game.util.Settable;


public class Login extends SessionState {
  
  public Login(final NoxioSession session) throws IOException {
    super(session);
    
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
    /* @TODO: this is blocking but it's probably okay. research and decide. */
    /* @TODO: move this type of get to a static class? */
    /* @FIXME also the app name is hardcoded here, make that a prop later. */

    // Use HTTPS for port 443, HTTP for other ports
    final int portInt = Settable.getAuthPort();
    final String port = String.valueOf(portInt);
    final String protocol = "443".equals(port) ? "https://" : "http://";
    final String portSuffix = ("443".equals(port) || "80".equals(port)) ? "" : ":" + port;
    final String address = protocol + Settable.getAuthDomain() + portSuffix + "/nxc/validate/" + p.getUser() + "/" + p.getSid();
    StringBuilder result = new StringBuilder();
    URL url = new URL(address);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    // Set Host header for internal Docker requests
    conn.setRequestProperty("Host", Settable.getAuthDomain() + portSuffix);
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
      sendPacket(new PacketL01(Settable.getServerInfo()));
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
