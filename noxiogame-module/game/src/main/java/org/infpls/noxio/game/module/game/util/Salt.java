  package org.infpls.noxio.game.module.game.util;

import java.util.UUID;

public class Salt {
  public static String generate() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }
}
