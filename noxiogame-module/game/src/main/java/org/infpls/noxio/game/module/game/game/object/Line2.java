package org.infpls.noxio.game.module.game.game.object;

public class Line2 {
  public final Vec2 a, b;
  public Line2(final Vec2 a, final Vec2 b) {
    this.a = a.copy();
    this.b = b.copy();
  }
  
  public Vec2 normal() {
    return new Vec2(b.y-a.y, -1*(b.x-a.x)).normalize();
  };
}
