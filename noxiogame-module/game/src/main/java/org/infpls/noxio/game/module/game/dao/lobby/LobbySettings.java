package org.infpls.noxio.game.module.game.dao.lobby;

import java.util.*;
import org.infpls.noxio.game.module.game.util.Oak;

public class LobbySettings extends GameSettings {
  private final List<GameSettings> rotation;
  public LobbySettings() {
    super();
    rotation = new ArrayList();
  }
  
  public void addRotation(final GameSettings gs) { rotation.add(gs); }
  public GameSettings getRotation(int i) { return rotation.size()>0?rotation.get(i%rotation.size()):new GameSettings(); }
  
  /* ================== Static Methods ================== */
  
  /* I can't remember why I wrote my own JSON parser here. I think I had a good reason? */
  /* AFAIK this class works perfectly so no need to change it. */
  public static List<LobbySettings> parseMultipleSettings(final String raw) {
    try {
      final List<LobbySettings> lss = new ArrayList();
      final Map<String,Object> map = parseBlock(raw, 0);
      final List<Object> ls = (List)(map.get("lobbies"));
      for(int i=0;i<ls.size();i++) {
        lss.add(convertFromMap((Map)(ls.get(i))));
      }
      return lss;
    }
    catch(IndexOutOfBoundsException | NullPointerException ex) {
      Oak.log(Oak.Level.ERR, "Error parsing game settings data.");
      Oak.log(Oak.Level.INFO, "Raw settings data: " + raw, ex);
      return null;
    }
  }
  
  public static LobbySettings parseSettings(final String raw) {
    try {
      final Map<String,Object> map = parseBlock(raw, 0);
      return convertFromMap((Map)map.get("lobby"));
    }
    catch(IndexOutOfBoundsException | NullPointerException ex) {
      Oak.log(Oak.Level.ERR, "Error parsing game settings data.");
      Oak.log(Oak.Level.INFO, "Raw settings data: " + raw, ex);
      return null;
    }
  }
  
  private static LobbySettings convertFromMap(final Map<String,Object> map) {
    final LobbySettings ls = new LobbySettings();
    final String[] keys = map.keySet().toArray(new String[0]);
    for(int i=0;i<keys.length;i++) {
      if(!keys[i].equals("rotation")) { ls.set(keys[i], (String)(map.get(keys[i]))); }
      else {
        final List<Object> rs = (List)(map.get("rotation"));
        for(int j=0;j<rs.size();j++) {
          final GameSettings gs = new GameSettings();
          final Map<String, Object> r = (Map)(rs.get(j));
          final String[] rkeys = r.keySet().toArray(new String[0]);
          for(int k=0;k<rkeys.length;k++) {
            gs.set(rkeys[k], (String)(r.get(rkeys[k])));
          }
          ls.addRotation(gs);
        }
      }
    }
    return ls;
  }
  
  /* This is scary. It does work though. */
  private static Map<String,Object> parseBlock(final String raw, final int startAt) {
    final Map<String,Object> block = new HashMap();

    Object subBlock = null;
    boolean comment = false, string = false;
    boolean vvFlag = false;
    int varAt = -1, varLen = 0, valAt = -1, valLen = 0;
    for(int i=startAt,k=0;i<raw.length();i++) {
      final char c = raw.charAt(i);
      if(c == '#' && !string) { comment = !comment; continue; }
      if(comment)  { continue; }
      if(c == '"') { string = !string; continue; }
      if(!string) {
        if(Character.isWhitespace(c)) { continue; }
        if(c == '{') { k++; } else if(c == '}') { k--; }
        if(k < 0) { if(vvFlag) { block.put(raw.substring(varAt, varAt+varLen), subBlock!=null?subBlock:raw.substring(valAt, valAt+valLen)); } break; }
        if(c == ':') { vvFlag = true; continue; }
        if(c == ',' || c == '}') { if(vvFlag) { block.put(raw.substring(varAt, varAt+varLen), subBlock!=null?subBlock:raw.substring(valAt, valAt+valLen)); } vvFlag = false; varAt = -1; valAt = -1; subBlock = null; continue; }
        if(c == '{') {
          int blockAt = ++i;
          for(int j=1;i<raw.length()&&j!=0;i++) {
            final char cx = raw.charAt(i);
            if(cx == '{') { j++; }
            else if(cx == '}') { j--; }
          }
          i-=2;
          subBlock = parseBlock(raw, blockAt);
          continue;
        }
        if(c == '[') {
          int blockAt = ++i, blockLen = 0;
          for(int j=1;i<raw.length()&&j!=0;i++) {
            final char cx = raw.charAt(i);
            if(cx == '[') { j++; }
            else if(cx == ']') { j--; }
            blockLen++;
          }
          i-=2;
          subBlock = parseArray(raw, blockAt);
          continue;
        }
      }
      if(!vvFlag) { 
        if(varAt == -1) { varAt = i; varLen = 0; }
        varLen++;
      }
      else {
        if(valAt == -1) { valAt = i; valLen = 0; }
        valLen++;
      }
    }
    if(vvFlag) { block.put(raw.substring(varAt, varAt+varLen), subBlock!=null?subBlock:raw.substring(valAt, valAt+valLen)); }

    return block;
  }
  
  /* Does not support anything except arrays of objects. Support can be added but it would be a good bit of work */
  private static List<Object> parseArray(final String raw, final int startAt) {
    final List<Object> array = new ArrayList();
    for(int i=startAt;i<raw.length();i++) { 
      final char c = raw.charAt(i);
        if(c == '{') {
          int blockAt = ++i;
          for(int j=1;i<raw.length()&&j!=0;i++) {
            final char cx = raw.charAt(i);
            if(cx == '{') { j++; }
            else if(cx == '}') { j--; }
          }
          i-=2;
          array.add(parseBlock(raw, blockAt));
          continue;
        }
        if(c == ']') { break; }
    }
    return array;
  }
}