package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.NoxioSession;

public abstract class SessionEvent {
  private final NoxioSession session;
  private final String type;
  public SessionEvent(final String type, final NoxioSession session) { this.type = type; this.session = session; }
  public NoxioSession getSession() { return session; }
  public String getType() { return type; }
}
