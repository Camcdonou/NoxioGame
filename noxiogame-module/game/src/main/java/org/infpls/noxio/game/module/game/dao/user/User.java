package org.infpls.noxio.game.module.game.dao.user;

public class User {
  private final String user; //case insensitive - forces lower case
  private final String hash; //pwd hash
  
  public User(final String user, final String hash) {
    this.user = user.toLowerCase();
    this.hash = hash;
  }
  
  public String getUser() { return user; }
  public String getHash() { return hash; }
  
}
