package org.infpls.noxio.game.module.game.dao.user;

public class UserData {
  public static enum Type {
    FREE(0, ""), SPEC(1, "γ"), FULL(2, "β"), MOD(3, "α"), ADMIN(4, "α");
    
    public final int level;
    public final String symbol;
    Type(int lvl, String sym) { level = lvl; symbol = sym; }
  }
  
  public final String uid;                 // Unique ID for user
  public final String name, display;       // User is always lower case
  public final Type type;                  // User type (see table in noxioauth User.java)
  public final boolean supporter;          // Outside supporter flag

  public final UserSettings settings;
  public final UserUnlocks unlocks;

  public UserData(String uid, String name, String display, Type type, boolean supporter, UserSettings settings, UserUnlocks unlocks) {
    this.uid = uid;
    this.name = name; this.display = display;
    this.type = type;
    this.supporter = supporter;
    this.settings = settings; this.unlocks = unlocks;
  }
}
