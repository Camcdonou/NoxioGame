package org.infpls.noxio.game.module.game.dao.user;

import java.util.Map;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UserUnlocks {
  public static enum Key {
      CHAR_BOX, CHAR_CRATE, CHAR_VOXEL, CHAR_CARGO, CHAR_BLOCK, CHAR_QUAD, CHAR_INFERNO,
      ALT_BOXGOLD, ALT_BOXRED, ALT_CRATEORANGE, ALT_VOXELGREEN, ALT_BLOCKROUND, ALT_QUADFIRE,
      FT_COLOR, FT_SOUND;
  }
  
  public static enum Type {
    CHARACTER, ALTERNATE, FEATURE
  }
  
  private static final List<Unlock> UNLOCKS = Arrays.asList(
    new Unlock(Key.CHAR_BOX, "Box", "So good that he even has a 60/40 matchup against himself.", Type.CHARACTER, 250, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_CRATE, "Crate", "Prefers the air.", Type.CHARACTER, 250, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_VOXEL, "Voxel", "Telefragging the competition.", Type.CHARACTER, 250, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_BLOCK, "Block", "In critical need of a good nights sleep.", Type.CHARACTER, 250, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_CARGO, "Cargo", "Punch.", Type.CHARACTER, 250, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_QUAD, "Quad", "Geometric concepts with swords.", Type.CHARACTER, 250, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_INFERNO, "InfernoPlus", "Unsubscribed.", Type.CHARACTER, 123456789, UserData.Type.ADMIN, false),
    
    new Unlock(Key.ALT_BOXGOLD, "Golden Box", "Solid gold means you are always #1.", Type.ALTERNATE, 100000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BOXRED, "Red Box", "Same old blip, shiney new color.", Type.ALTERNATE, 25000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CRATEORANGE, "Orange Crate", "Same old blip, shiney new color.", Type.ALTERNATE, 25000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_VOXELGREEN, "Green Voxel", "Same old blip, shiney new color.", Type.ALTERNATE, 25000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BLOCKROUND, "Curvy Block", "At least 7 polygons probably.", Type.ALTERNATE, 50000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_QUADFIRE, "Bad Quad", "Exactly the same character but really bad.", Type.ALTERNATE, 50000, UserData.Type.FULL, false),
          
    new Unlock(Key.FT_COLOR, "Custom Colors", "Allows you to change the color of your character. Also allows you to create phasing colors.", Type.FEATURE, 50000, UserData.Type.SPEC, false),
    new Unlock(Key.FT_SOUND, "Custom Sounds", "Allows you to upload and and use a custom sound effect. Plays when you come in 1st place.", Type.FEATURE, 99999999, UserData.Type.SPEC, false)
  );
  
  public static Unlock getUnlock(final Key key) {
    for(int i=0;i<UNLOCKS.size();i++) {
      final Unlock u = UNLOCKS.get(i);
      if(u.key == key) { return u; }
    }
    return null;
  }
  
  public static class Unlock {
    public final Key key;
    public final String name, description;
    public final Type type;
    public final int auto; /* Account level that this is automatically considered 'unlocked' */
    public final int price;
    public final boolean hidden;
    public Unlock(Key key, String name, String description, Type type, int price, UserData.Type userType, boolean hidden) {
      this.key = key;
      this.name = name; this.description = description; this.type = type;
      this.auto = userType.level;
      this.price = price;
      this.hidden = hidden;
    }
  }
  
  public final String uid;
  public final UserData.Type type;
  public final Timestamp updated;
  private final Map<Key, Boolean> unlocks;
  /* Basically just a blank constructor as this class is created from JSON only */
  public UserUnlocks(final String uid) {
    this.uid = uid;
    type = UserData.Type.FREE;
    updated = null;
    unlocks = new HashMap();
  }
  
  public boolean has(Key key) {
    final Unlock u = UserUnlocks.getUnlock(key);
    if(u.auto <= type.level) { return true; }
    final Boolean r = unlocks.get(key);
    return r != null ? r : false;
  }
}
