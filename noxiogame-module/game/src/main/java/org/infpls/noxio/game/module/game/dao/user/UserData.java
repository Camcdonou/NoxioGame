package org.infpls.noxio.game.module.game.dao.user;

public class UserData {
  public final String uid;                   // Unique ID for user
  public final String name, display;         // User is always lower case
  public final boolean premium;              // Payed user

  public final UserSettings settings;
  public final UserUnlocks unlocks;

  public UserData(final String uid, final String name, final String display, final boolean premium, final UserSettings settings, final UserUnlocks unlocks) {
    this.uid = uid;
    this.name = name; this.display = display;
    this.premium = premium;
    this.settings = settings; this.unlocks = unlocks;
  }
}
