package org.infpls.noxio.game.module.game.dao.lobby;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.infpls.noxio.game.module.game.dao.server.InfoDao;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.session.PacketH09;

/* This class handles the sending of packets to connected clients */
/* This is done on a seperate thread to prevent blocks in the game loop */
/* If a websocket is blocked for more than 15 seconds on sending a packet it will initiate a force close on it */
public class HttpThread extends Thread {
  private final InfoDao info;            // Hacky, gross, please fix this if you get a chance @TODO:
  
  private List<Packet> out;        // Outgoing packet queue
  private final Gson gson;
    
  private boolean closed;
  public HttpThread(final InfoDao info) {
    this.info = info;
    
    out = new ArrayList();
    gson = new GsonBuilder().create();
    
    closed = false;
  }
  
  @Override
  public void run() {
    while(!closed) {
      final List<Packet> pkts = pop();
      if(pkts != null) {
        for(int i=0;i<pkts.size();i++) {
          sendPost(pkts.get(i));
        }
      }
      else { doWait(); }
    }
  }
  
  /* Blocking */
  private void sendPost(final Packet p) {
    final String content = gson.toJson(p);
    HttpURLConnection connection = null;
    try {
      final URL to = new URL("http://" + info.getAuthServerAddress() + "/noxioauth/report");

      //Create connection
      connection = (HttpURLConnection)to.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", 
           "application/json");
			
      connection.setRequestProperty("Content-Length", "" + 
               Integer.toString(content.getBytes().length));
      connection.setRequestProperty("Content-Language", "en-US");  
			
      connection.setUseCaches (false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      //Send request
      DataOutputStream wr = new DataOutputStream (
                  connection.getOutputStream ());
      wr.writeBytes (content);
      wr.flush ();
      wr.close ();

      //Get Response	
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuffer response = new StringBuffer(); 
      while((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
      final Packet pkt = gson.fromJson(response.toString(), Packet.class);
      /* Return type of h00 is "all okay" so we don't check for it */
      /* Return type of h09 is "error message" so we write that to logs */
      if(p.getType().equals("h09")) {
        final PacketH09 pkth = gson.fromJson(response.toString(), PacketH09.class);
        System.err.println("HttpThread::sendPost - Request returned error: " + pkth.getMessage());
      }
      
    } catch(MalformedURLException ex) {
      System.err.println("HttpThread::new - Invalid URL : " + "http://" + info.getAuthServerAddress() + "/noxioauth/report");
      ex.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if(connection != null) {
        connection.disconnect(); 
      }
    }
  }
  
  private synchronized void doWait() { try { wait(); } catch(InterruptedException ex) { ex.printStackTrace(); } }
  private synchronized void doNotify() { notify(); }
  
  public void push(final Packet p) { if(closed) { return; } syncPacketAccess(false, p); doNotify(); }
  private List<Packet> pop() { return syncPacketAccess(true, null); }
  private synchronized List<Packet> syncPacketAccess(final boolean s, final Packet p) {
    if(s) {
      if(out.size() > 0) { final List<Packet> popped = out; out = new ArrayList(); return popped; }
      else { return null; }
    }
    out.add(p);
    return null;
  }
  
  /* This is the safe close, this is called by NoxioSession during a normal disconnect. */
  public void close() {
    closed = true;
    doNotify();
  }
}
