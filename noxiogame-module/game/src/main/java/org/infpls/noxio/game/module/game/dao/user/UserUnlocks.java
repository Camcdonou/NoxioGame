package org.infpls.noxio.game.module.game.dao.user;

import java.util.Map;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UserUnlocks {
  public static enum Key {
      CHAR_BOX, CHAR_CRATE, CHAR_VOXEL, CHAR_CARGO, CHAR_BLOCK, CHAR_QUAD, CHAR_INFERNO,
      ALT_BOXVO, ALT_BOXRED, ALT_BOXRAINBOW, ALT_BOXGOLD, ALT_BOXDELTA, ALT_BOXHIT, ALT_BOXFOUR, ALT_BOXBLOOD, ALT_BOXLOOT,
      ALT_CRATEVO, ALT_CRATEORANGE, ALT_CRATERAINBOW, ALT_CRATEGOLD, ALT_CRATEDELTA, ALT_CRATEFIRE, ALT_CRATEBLACK, ALT_CRATELOOT,
      ALT_VOXVO, ALT_VOXGREEN, ALT_VOXRAINBOW, ALT_VOXGOLD, ALT_VOXDELTA, ALT_VOXBLACK, ALT_VOXLOOT,
      ALT_QUADVO, ALT_QUADRAINBOW, ALT_QUADGOLD, ALT_QUADDELTA, ALT_QUADFIRE, ALT_QUADLEGEND, ALT_QUADRUNE,
      ALT_BLOCKVO, ALT_BLOCKRAINBOW, ALT_BLOCKGOLD, ALT_BLOCKDELTA, ALT_BLOCKROUND, ALT_BLOCKWIN, ALT_BLOCKFIRE, ALT_BLOCKRO, ALT_BLOCKLOOT,
      ALT_CARGOVO, ALT_CARGOPLUS, ALT_CARGORAINBOW, ALT_CARGOGOLD, ALT_CARGODELTA, ALT_CARGOBLACK, ALT_CARGOMINE, ALT_CARGORETRO,
      FT_LOBBY, FT_COLOR, FT_SOUND, FT_MESSAGE;
  }
  
  public static enum Type {
    CHARACTER, ALTERNATE, FEATURE
  }
  
  private static final List<Unlock> UNLOCKS = Arrays.asList(
    new Unlock(Key.CHAR_BOX, "Box", "So good that it even has a 60/40 matchup against itself.", Type.CHARACTER, 500, UserData.Type.FREE, false),
    new Unlock(Key.CHAR_CRATE, "Crate", "Prefers the air.", Type.CHARACTER, 500, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_VOXEL, "Voxel", "Telefragging the competition.", Type.CHARACTER, 500, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_BLOCK, "Block", "Time for a nap.", Type.CHARACTER, 500, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_CARGO, "Cargo", "Punch.", Type.CHARACTER, 500, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_QUAD, "Quad", "Geometric concepts with swords.", Type.CHARACTER, 500, UserData.Type.SPEC, false),
    new Unlock(Key.CHAR_INFERNO, "Plus", "Unsubscribed.", Type.CHARACTER, 123456789, UserData.Type.ADMIN, false),
    
    new Unlock(Key.ALT_BOXVO, "Talking Box", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_BOXRED, "Red Box", "Box with red effects.", Type.ALTERNATE, 5000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BOXRAINBOW, "Rainbow Box", "Box with rainbow effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BOXGOLD, "Solid Gold Box", "Box with a shiny gold finish and purple effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BOXDELTA, "Delta Box", "Box with a shiny finish and blue effects.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BOXHIT, "Hitbox", "You can't escape the hitmarkers.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BOXFOUR, "Four Box", "Yay! Hats!", Type.ALTERNATE, 5000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BOXBLOOD, "Blood Box", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_BOXLOOT, "Loot Box", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    
    new Unlock(Key.ALT_CRATEVO, "Talking Crate", "Now it can talk.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CRATEORANGE, "Orange Crate", "Crate with orange effects.", Type.ALTERNATE, 5000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CRATERAINBOW, "Rainbow Crate", "Crate with rainbow effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CRATEGOLD, "Solid Gold Crate", "Crate with a shiny gold finish and purple effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CRATEDELTA, "Delta Crate", "Crate with a shiny finish and blue effects.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CRATEFIRE, "Spicy Crate", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_CRATEBLACK, "Blackflame Crate", "Crate with edgy effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CRATELOOT, "Loot Crate", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    
    new Unlock(Key.ALT_VOXVO, "Talking Voxel", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_VOXGREEN, "Green Voxel", "Voxel with green effects.", Type.ALTERNATE, 5000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_VOXRAINBOW, "Rainbow Voxel", "Voxel with rainbow effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_VOXGOLD, "Solid Gold Voxel", "Voxel with a shiny gold finish and purple effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_VOXDELTA, "Delta Voxel", "Voxel with a shiny finish and blue effects.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_VOXBLACK, "Dark Voxel", "Voxel with edgy effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_VOXLOOT, "Loot Voxel", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    
    new Unlock(Key.ALT_QUADVO, "Talking Quad", "Now it can talk.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_QUADRAINBOW, "Rainbow Quad", "Quad with rainbow effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_QUADGOLD, "Solid Gold Quad", "Quad with a shiny gold finish and purple effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_QUADDELTA, "Delta Quad", "Delta with a shiny finish and blue effects.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_QUADFIRE, "Spicy Quad", "Quad with fire effects.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_QUADLEGEND, "Undying Quad", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_QUADRUNE, "Rune Quad", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    
    new Unlock(Key.ALT_BLOCKVO, "Talking Block", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_BLOCKRAINBOW, "Rainbow Block", "Block with rainbow effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BLOCKGOLD, "Solid Gold Block", "Block with a shiny gold finish.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BLOCKDELTA, "Delta Block", "Delta with a shiny finish.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BLOCKROUND, "Round Block", "An abomination.", Type.ALTERNATE, 5000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BLOCKWIN, "Block 95", "Retro.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_BLOCKFIRE, "Spicy Block", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_BLOCKRO, "Roblock", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_BLOCKLOOT, "Loot Block", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    
    new Unlock(Key.ALT_CARGOVO, "Talking Cargo", "Now it can talk.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CARGOPLUS, "Bad Cargo", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_CARGORAINBOW, "Rainbow Cargo", "Cargo with rainbow effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CARGOGOLD, "Solid Gold Cargo", "Cargo with a shiny gold finish and purple effects.", Type.ALTERNATE, 10000, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CARGODELTA, "Delta Cargo", "Delta with a shiny finish and blue effects.", Type.ALTERNATE, 7500, UserData.Type.FULL, false),
    new Unlock(Key.ALT_CARGOBLACK, "Blackflame Cargo", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_CARGOMINE, "Minecargo", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
    new Unlock(Key.ALT_CARGORETRO, "Retro Cargo", "Descriptions describe things.", Type.ALTERNATE, 999999, UserData.Type.FULL, true),
        
    new Unlock(Key.FT_LOBBY, "Custom Lobbies", "Allows you to host custom games with your own rule sets.", Type.FEATURE, 25000, UserData.Type.SPEC, false),
    new Unlock(Key.FT_COLOR, "Custom Colors", "Allows you to change the color of your character and create phasing colors.", Type.FEATURE, 25000, UserData.Type.SPEC, false),
    new Unlock(Key.FT_MESSAGE, "Custom Message", "Allows you to set a win message that is displayed if you come in 1st place.", Type.FEATURE, 25000, UserData.Type.SPEC, false),
    new Unlock(Key.FT_SOUND, "Custom Sounds", "Allows you to upload and and use a custom sound effect. Plays when you come in 1st place.", Type.FEATURE, 123456789, UserData.Type.SPEC, false)
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
