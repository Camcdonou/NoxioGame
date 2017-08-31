package org.infpls.noxio.game.module.game.dao.lobby;

import java.util.*;

public class GameSettings {
  private final Map<String, String> settings;
  public GameSettings() { 
    settings = new HashMap();
  }
  
  public void set(final String key, final String val) { settings.put(key, val); }
  public int get(final String key, final int def) { final String val = settings.get(key); return val!=null?Integer.parseInt(val):def; }
  public float get(final String key, final float def) { final String val = settings.get(key); return val!=null?Float.parseFloat(val):def; }
  public String get(final String key, final String def) { final String val = settings.get(key); return val!=null?val:def; }
}
