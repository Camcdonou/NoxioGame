package org.infpls.noxio.game.module.game.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Oak {
  
  private static final String FILE = "NXG-";
  private static final String EXTEN = ".html";
  private static final String CSS = "<style>.l1 { color: #FFFFFF; background-color: #000000; font-family: \"Calibri\", monospace; }.l2 { color: #FFFF00; background-color: #000000; font-family: \"Calibri\", monospace; }.l3 { color: #FF0000; background-color: #000000; font-family: \"Calibri\", monospace; }.l4 { color: #000000; background-color: #FF0000; font-family: \"Calibri\", monospace; }.ex { background-color: #888888; color: #000000; font-family: \"Calibri\", monospace; }</style>\n";
  private static final String FORMAT = "HH:mm yy-MM-dd";
  private static final SimpleDateFormat DATE = new SimpleDateFormat(FORMAT);
  
  public static enum Type {
    GAME,       // Game & Lobby Logging
    NETWORK,    // Websocket write/read level logging
    SESSION,    // User websocket session logging
    HTTPS,      // Rest services and HTTPS logging
    SYSTEM      // System & File IO logging
  }
  
  public static enum Level {
    INFO("l1"), WARN("l2"), ERR("l3"), CRIT("l4");
    
    private final String css;
    Level(String css) {
      this.css = css;
    }
    public String html(String src, String message) {
      return "<div class='" + css +"'><small>" + DATE.format(new Date()) + "</small> - <b>" + src + "</b> :: " + message + "</div>";
    }
  }

  private static final List<Log> LOGS = new ArrayList();
  public static void open() {
    if(LOGS.size()>0) { return; }
    final String folder = Settable.getFilePath();
    final Type fs[] = Type.values();
    
    for(int i=0;i<fs.length;i++) {
      final String file = FILE + fs[i].name() + EXTEN;
      final Log l = new Log(fs[i], folder, file);
      LOGS.add(l);
      l.open();
    }
  }
 
  public static Log get(Type t) {
    for(int i=0;i<LOGS.size();i++) {
      final Log l = LOGS.get(i);
      if(l.type == t) { return l; }
    }
    return null;
  }
  
  public static void close() {
    for(int i=0;i<LOGS.size();i++) {
      LOGS.get(i).close();
      LOGS.remove(i--);
    }
  }
  
  public static void log(Type type, Level level, String msg) {
    log(type, level, msg, (Exception)null);
  }
  
  public static void log(Type type, Level level, String src, String msg) {
    log(type, level, src, msg, (Exception)null);
  }
  
  public static void log(Type type, Level level, String msg, Exception ex) {
    String src = "<Unknown>";
    try {
      final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
      final String[] spl = ste[ste.length > 2 ? 3 : 0].getClassName().split("\\.");
      src = spl[spl.length > 0 ? spl.length-1 : 0] + "." + ste[ste.length > 2 ? 3 : 0].getMethodName();
    }
    catch(Exception e) { }
    log(type, level, src, msg, ex);
  }
  
  public static void log(Type type, Level level, String src, String msg, Exception ex) {
    get(type).write(level, src, msg, ex);
  }
  
  public static class Log {
    public final Type type;
    private final String folder; // Path
    private final String file;  // Path
    
    private boolean ready;
    
    private FileWriter FW;
    private BufferedWriter BW;
    private PrintWriter OUT;
    public Log(Type type, String folder, String file) {
      this.type = type; this.folder = folder; this.file = folder + "/" + file;
      ready = false;
    }
    
    public void open() {
      try {
        boolean nu = false;
        final File fsDir = new File(folder);
        if(fsDir.exists() && !fsDir.isDirectory()) { System.err.println("Oak.open :: Filestore path is not a valid directory!"); }
        if(fsDir.exists() && !fsDir.canWrite()) { System.err.println("Oak.open :: Can't write to filestore path!"); }
        if(!fsDir.exists()) { if(!fsDir.mkdirs()) { System.err.println("Oak.open :: Failed to create filestore directory!"); } }
        final File fsLog = new File(file);
        if(fsLog.exists() && fsLog.isDirectory()) { System.err.println("Oak.open :: Log file is a directory??"); }
        if(!fsLog.exists()) { if(!fsLog.createNewFile()) { System.err.println("Oak.open :: Failed to create log file!"); } else { nu = true; } }

        FW = new FileWriter(file, true);
        BW = new BufferedWriter(FW);
        OUT = new PrintWriter(BW);
        
        if(nu) { OUT.println(CSS); }
      }
      catch (IOException ex) {
        System.err.println("Oak.open :: Unable to open Log file: " + file);
        ex.printStackTrace();
        return;
      }
      ready = true;
      Oak.log(type, Oak.Level.INFO, "Logging started");
    }
    
    public boolean ready() {
      return OUT != null && ready;
    }
    
    private void write(Level level, String src, String msg, Exception ex) {
      if(!ready()) { return; }

      OUT.println(level.html(src, msg));
      if(ex != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        final String trace = "<div class='ex'><small>" + sw.toString().replaceAll("\\r?\\n", "</br>") + "</small></div>";
        OUT.println(trace);
      }

      OUT.flush();
    }
    
    public void close() {
      if(!ready()) { return; }
      try {
        OUT.close();
        BW.close();
        FW.close();
        ready = false;
      }
      catch (IOException ex) {
        System.err.println("Oak.close :: Unable to close Log file: " + file);
        ex.printStackTrace();
        return;
      }
      Oak.log(type, Oak.Level.INFO, "Logging stopped");
    }
  }
}
