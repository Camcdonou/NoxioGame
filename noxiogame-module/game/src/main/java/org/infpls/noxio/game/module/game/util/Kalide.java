package org.infpls.noxio.game.module.game.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* @TODO: something that should be added to a NoxioShared package */

/* Pre defined color list, used for player colors */
/* Colors are identified by their index in the colors array. */
/* This class needs to directly match the list on the game server and game client */
public class Kalide {
  /* Max size of this array is 255 */
  private static final List<Color3f> COLORS = Arrays.asList(
    new Color3f(127, 127, 127), /* Considered a 'no color' slot. Defaults to grey or is unused. */
    new Color3f(127, 127, 127),
    new Color3f(255, 0, 0),
    new Color3f(255, 127, 0),
    new Color3f(255, 255, 0),
    new Color3f(127, 255, 0),
    new Color3f(0, 255, 0),
    new Color3f(0, 255, 127),
    new Color3f(0, 255, 255),
    new Color3f(0, 127, 255),
    new Color3f(127, 0, 255),
    new Color3f(127, 0, 255),
    new Color3f(255, 0, 255),
    new Color3f(255, 0, 127)
  );
  
  /* Max size of this array is 255 */
  private static final List<Color3f> REDS = Arrays.asList(
    new Color3f(192, 62, 62), /* Considered a 'no color' slot. Uses default red color or is unused. */
    new Color3f(255, 0, 0),
    new Color3f(255, 76, 76),
    new Color3f(223, 31, 31),
    new Color3f(193, 66, 66)
  );
  
  /* Max size of this array is 255 */
  private static final List<Color3f> BLUES = Arrays.asList(
    new Color3f(62, 62, 192), /* Considered a 'no color' slot. Uses default blue color or is unused. */
    new Color3f(0, 0, 255),
    new Color3f(76, 76, 255),
    new Color3f(31, 31, 223),
    new Color3f(66, 66, 193)
  );
  
  /* ID and Team, team=0 returns red team colors, team=1 returns blue team colors, all else returns regular colors */
  public static Color3f getColor(int id, int team) {
    switch(team) {
      case 0 : { return getRed(id); }
      case 1 : { return getBlue(id); }
      default : { return getColor(id); }
    }
  }
  
  /* ID and Team, team=0 returns red team colors, team=1 returns blue team colors, all else returns regular colors */
  public static List<Color3f> getColors(int id, int team) {
    switch(team) {
      case 0 : { return getReds(id); }
      case 1 : { return getBlues(id); }
      default : { return getColors(id); }
    }
  }
  
  /* Single id single color */
  public static Color3f getColor(int id) { return kalide(id, COLORS); }
  
  /* Retrieves up to 4 color ids (0-255) from a single 32bit integer, used for animated colors */
  public static List<Color3f> getColors(int id) { return kalides(id, COLORS); }
  
  /* Single id single red team color */
  public static Color3f getRed(int id) { return kalide(id, REDS); }
  
  /* Retrieves up to 4 color ids (0-255) from a single 32bit integer, used for animated red team colors */
  public static List<Color3f> getReds(int id) { return kalides(id, REDS); }
  
  /* Single id single blue team color */
  public static Color3f getBlue(int id) { return kalide(id, BLUES); }
  
  /* Retrieves up to 4 color ids (0-255) from a single 32bit integer, used for animated blue team colors */
  public static List<Color3f> getBlues(int id) { return kalides(id, BLUES); }
  
  /* Does the magic */
  private static Color3f kalide(int id, List<Color3f> list) {
    if(id >= 0 && id < list.size()) { return list.get(id); }
    else { return list.get(0); }
  }
  
  /* Does the magic up to 4 times */
  private static List<Color3f> kalides(int id, List<Color3f> list) {
    int ex[] = new int[] {
      id & 0xFF, (id >> 8) & 0xFF, (id >> 16) & 0xFF, (id >> 24) & 0xFF
    };
    final List<Color3f> colors = new ArrayList();
    for(int i=0;i<ex.length;i++) {
      if(ex[i] != 0 && ex[i] >= 0 && ex[i] < list.size()) { colors.add(colors.get(ex[i])); }
    }
    if(colors.size() < 1) { colors.add(colors.get(0)); }
    return colors;
  }
  
  /* Compresses up to 4 color ids (0-255) into a single 32bit integer */
  public static int compressColors(int... c) {
    int comp = 0;
    if(c.length > 0) { comp = comp | (c[0] & 0xFF); }
    if(c.length > 1) { comp = comp | ((c[1] << 8) & 0xFF00); }
    if(c.length > 2) { comp = comp | ((c[2] << 16) & 0xFF0000); }
    if(c.length > 3) { comp = comp | ((c[3] << 24) & 0xFF000000); }
    return comp;
  }
  
  /* Converts compressed color id back to (up to) 4 color ids. **does not truncate blanks */
  public static int[] decompressColors(int id) {
    return new int[] { id & 0xFF, (id >> 8) & 0xFF, (id >> 16) & 0xFF, (id >> 24) & 0xFF };
  }
  
  public static class Color3f {
    public final float r, g, b;
    public Color3f(float r, float g, float b) {
      this.r = r; this.g = g; this.b = b;
    }
    public Color3f(int r, int g, int b) {
      this.r = ((float)r)/255f;
      this.g = ((float)g)/255f;
      this.b = ((float)b)/255f;
    }
  }
}
