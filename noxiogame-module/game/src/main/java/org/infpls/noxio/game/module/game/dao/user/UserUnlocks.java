package org.infpls.noxio.game.module.game.dao.user;

import java.util.Map;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.HashMap;

public class UserUnlocks {
  public static enum Key {
      CHAR_BOX, CHAR_CRATE, CHAR_VOXEL, CHAR_CARGO, CHAR_BLOCK, CHAR_QUAD, CHAR_INFERNO,
      ALT_BOXGOLD, ALT_BOXRED, ALT_CRATEORANGE, ALT_VOXELGREEN, ALT_BLOCKROUND, ALT_QUADFIRE,
      FT_COLOR, FT_SOUND;
  }
  
  public final String uid;
  public final Timestamp updated;
  private final Map<Key, Boolean> unlocks;
  public UserUnlocks(final Map<String, Object> data) {
    unlocks = new HashMap();
    uid = (String)data.remove("uid");
    updated = (Timestamp)data.remove("updated");
   
    /* Uses reflection to map SQL databse names to enums that identify the unlocks */
    try {
      final String[] ks = data.keySet().toArray(new String[0]);
      for(int i=0;i<ks.length;i++) {
        final Field en = Key.class.getField(ks[i]);
        unlocks.put((Key)en.get(null), (boolean)data.get(ks[i]));
      }
    }
    catch(NoSuchFieldException | IllegalAccessException ex) {
      System.err.println("UserUnlocks::new - Error parsing unlock data from database.");
      ex.printStackTrace();
    }
  }
  
  public boolean has(Key key) {
    final Boolean r = unlocks.get(key);
    return r != null ? r : false;
  }
}
