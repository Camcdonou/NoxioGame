package org.infpls.noxio.game.module.game.dao.user;

public class UserData {
  public static enum Type {
    FREE(0), SPEC(1), FULL(2), MOD(3), ADMIN(4);
    
    public final int level;
    Type(int lvl) { level = lvl; } 
  }
  
  public final String uid;                   // Unique ID for user
  public final String name, display;         // User is always lower case
  public final Type type;                  // Payed/unpayed user (see table in noxioauth User.java)

  public final UserSettings settings;
  public final UserUnlocks unlocks;

  public UserData(final String uid, final String name, final String display, final Type type, final UserSettings settings, final UserUnlocks unlocks) {
    this.uid = uid;
    this.name = name; this.display = display;
    this.type = type;
    this.settings = settings; this.unlocks = unlocks;
  }
}
