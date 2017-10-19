package org.infpls.noxio.game.module.game.util;

import java.util.Date;

public class Oak {
  /* This is very stubby, I just needed a standard way to go about this stuff and I'll clean it up later. */
  /* Log levels -
     0 - info
     1 - warn
     2 - error
  */
  public static void log(final String msg, final int level) {
    System.out.println(new Date().toString() + " :: " + msg);
  }
}
