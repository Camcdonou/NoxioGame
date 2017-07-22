package org.infpls.noxio.game.module.game.game.object;

public class Polygon {
  public final Vec2[] v;             // Vertices of this Convex Polygon
  public transient final Vec2 c;     // Calculated center point
  public transient final float r;    // Calculated max distance from center
  public Polygon(final Vec2[] v) {
    this.v = v;
    Vec2 calc = new Vec2();
    for(int i=0;i<v.length;i++) { calc = calc.add(v[i]); }
    c = calc.scale(1.0f/v.length);
    float max = 0f;
    for(int i=0;i<v.length;i++) { max = (c.distance(v[i])>max?c.distance(v[i]):max); }
    r = max;
  }
  public Vec2[] getVertices() { return v; }
  public Vec2 getCenter() { return c; }
  public float getRadius() { return r; }
}
