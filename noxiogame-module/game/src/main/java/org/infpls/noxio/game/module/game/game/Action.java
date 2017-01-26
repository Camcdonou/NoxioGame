package org.infpls.noxio.game.module.game.game;

import org.infpls.noxio.game.module.game.game.object.Vec2;

public class Action {
  private final String action;
  private final Vec2 target;
  public Action(final String action, final Vec2 target) {
    this.action = action;
    this.target = target;
  }

  public String getAction() { return action; }
  public Vec2 getTarget() { return target; }
}