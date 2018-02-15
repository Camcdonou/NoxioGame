package org.infpls.noxio.game.module.game.session;

import java.io.IOException;
import java.util.*;

/* This class handles the sending of packets to connected clients */
/* This is done on a seperate thread to prevent blocks in the game loop */
/* If a websocket is blocked for more than 15 seconds on sending a packet it will initiate a force close on it */
public class SessionThread extends Thread {
  private final NoxioSession session;
  private List<Packet> out;              // Outgoing packet queue
  
  private static final int SEND_TIMEOUT = 450, CLOSE_WAIT_TIMEOUT = 150;
  
  private long sendTime;                 // Time of last send start
  private boolean sending;               // Currently in the process of sending data to a client
  private boolean forceClose, safeClose, closed;
  public SessionThread(final NoxioSession ns) {
    session = ns;
    out = new ArrayList();
    
    sendTime = System.currentTimeMillis();
    sending = false;
    forceClose = false;
    safeClose  = false;
    closed = false;
  }
  
  @Override
  public void run() {
    while(session.isOpen() && !forceClose && !safeClose) {
      final List<Packet> paks = pop();
      try {
        if(paks != null) {
          sendTime = System.currentTimeMillis();
          sending = true;
          session.sendImmiediate(new PacketS01(paks));
          sending = false;
        }
        else { doWait(); }
      }
      catch(IOException | IllegalStateException ex) {
        System.err.println("SessionThread::run() Exception during SessionThread send for user: " + session.getUser());
        System.err.println("SessionThread::run() Force closing connection...");
        ex.printStackTrace();
        forceClose();
      }
    }
    if(forceClose) {
      System.err.println("SessionThread::run() Unsafe SessionThread close for user: " + session.getUser());
      System.err.println("SessionThread::run() Force closing connection...");
      if(session.isOpen()) {
        try  { session.close("Connection force closed by server."); }
        catch(IOException ex) { 
          System.err.println("SessionThread::run() IOException during SessionThread forceClose for user: " + session.getUser());
          ex.printStackTrace();
        }
      }
    }
    closed = true;
  }
  
  private synchronized void doWait() { try { wait(); } catch(InterruptedException ex) { ex.printStackTrace(); } }
  private synchronized void doNotify() { notify(); }
  
  public void checkTimeout() {
    final long now = System.currentTimeMillis();
    if(sending && ((now - sendTime) > SEND_TIMEOUT)) {
      System.err.println("SessionThread::checkTimeout() Send Timeout exceeded for user: " + session.getUser() + " :: lastSend=" + sendTime + " now=" + now + " diff=" + (now-sendTime));
      forceClose();
    }
  }
  
  public void push(final Packet p) { syncPacketAccess(false, p); checkTimeout(); doNotify(); }
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
    safeClose = true;
    doNotify();
  }
  
  /* Returns as soon as thread is completely finished sending data OR if CLOSE_WAIT_TIMEOUT milliseconds pass */
  /* Returns true if properly closed, returns false if times out */
  public boolean blockingWaitForClose() throws IOException {
    doNotify();
    final long start = System.currentTimeMillis();
    long now = start;
    while(!closed && ((now - start) < CLOSE_WAIT_TIMEOUT)) { now = System.currentTimeMillis(); }
    if(!closed) { System.err.println("SessionThread::blockingWaitForClose() TIMED OUT WAITING FOR CLOSE! user: " + session.getUser()); }
    return closed;
  }
  
  public void forceClose() {
    System.err.println("SessionThread::forceClose() call for user: " + session.getUser());
    forceClose = true;
    doNotify();
  }
}
