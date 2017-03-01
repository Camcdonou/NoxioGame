package org.infpls.noxio.game.module.game.util;

import org.infpls.noxio.game.module.game.game.object.*;

public class Intersection {
  
  public static Instance lineCircle(final Vec2 P, final Line2 L, float r) {
    final Vec2 nearest = lineNearestPoint(P, L);
    if(nearest.equals(L.a)) {
      final Vec2 dir = P.subtract(L.a);
      final float dist = dir.magnitude();
      if(dist >= r) { return null; }
      final Vec2 norm = dir.normalize();
      return new Instance(L.a.copy(), norm.isNaN() ? new Vec2(1.0f, 0.0f) : norm, dist);
    }
    else if(nearest.equals(L.b)) {
      final Vec2 dir = P.subtract(L.b);
      final float dist = dir.magnitude();
      if(dist >= r) { return null; }
      final Vec2 norm = dir.normalize();
      return new Instance(L.b.copy(), norm.isNaN() ? new Vec2(1.0f, 0.0f) : norm, dist);
    }
    else {
      final Vec2 dir = P.subtract(nearest);
      final float dist = dir.magnitude();
      if(dist >= r) { return null; }
      final Vec2 norm = dir.normalize();
      return new Instance(nearest.copy(), norm.isNaN() ? new Vec2(1.0f, 0.0f) : norm, dist);
    }
  }
  
  private static Vec2 lineNearestPoint(final Vec2 P, final Line2 L) {   
    final Vec2 v = L.b.subtract(L.a);
    final Vec2 w = P.subtract(L.a);
    float c1 = w.dot(v);
    if ( c1 <= 0 ) { return L.a.copy(); }
    float c2 = v.dot(v);
    if ( c2 <= c1 ) { return L.b.copy(); }
    float b = c1 / c2;
    return L.a.add(v.scale(b));
  }
  
  public static class Instance {
    public final Vec2 intersection, normal;
    public final float distance;
    public Instance(final Vec2 intersection, final Vec2 normal, final float distance) {
      this.intersection = intersection;
      this.normal = normal;
      this.distance = distance;
    }
  }
}
