package org.infpls.noxio.game.module.game.util;

import org.infpls.noxio.game.module.game.game.object.*;

/* Parses strings to various data types such as Vec2 or float or whatever fucking sue me */
public class Parse {
  public static Vec2 vec2(final String s) {
    final String[] spl = s.split(",");
    return new Vec2(Float.parseFloat(spl[0]), Float.parseFloat(spl[1]));
  }
  
  /* Oh yeah I'm gonna get sued baby :EMOJIS: */
  public static float f(final String s) {
    return Float.parseFloat(s);
  }
}
