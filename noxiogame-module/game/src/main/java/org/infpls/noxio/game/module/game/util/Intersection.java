package org.infpls.noxio.game.module.game.util;

import java.util.*;
import org.infpls.noxio.game.module.game.game.object.*;

public class Intersection {
  
  public static Instance lineLine(final Line2 A, final Line2 B) {
    float s1_x, s1_y, s2_x, s2_y;
    float i_x, i_y;
    s1_x = A.b.x - A.a.x;     s1_y = A.b.y - A.a.y;
    s2_x = B.b.x - B.a.x;     s2_y = B.b.y - B.a.y;

    float s, t;
    s = (-s1_y * (A.a.x - B.a.x) + s1_x * (A.a.y - B.a.y)) / (-s2_x * s1_y + s1_x * s2_y);
    t = ( s2_x * (A.a.y - B.a.y) - s2_y * (A.a.x - B.a.x)) / (-s2_x * s1_y + s1_x * s2_y);

    if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
    {
        // Collision detected
        i_x = A.a.x + (t * s1_x);
        i_y = A.a.y + (t * s1_y);
        final Vec2 intersection = new Vec2(i_x, i_y);
        final Vec2 normal = intersection.subtract(A.a).normalize();
        return new Instance(intersection, normal, intersection.distance(A.a));
    }

    return null; // No collision
  }
  
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
  
  public static Instance polygonCircle(final Vec2 P, final Polygon G, float r) {
    final List<Instance> hits = new ArrayList();
    for(int i=0;i<G.v.length;i++) {
      final Line2 L = new Line2(G.v[i], G.v[i+1<G.v.length?i+1:0]);
      final Instance inst = lineCircle(P, L, r);
      if(inst != null) { hits.add(inst); }
    }
    if(hits.size() < 1) { return null; }
    Instance nearest = hits.get(0);
    for(int i=1;i<hits.size();i++) {
      if(hits.get(i).distance < nearest.distance) {
        nearest = hits.get(i);
      }
    }
    return nearest;
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
  
  public static boolean pointInPolygon(final Vec2 P, final Polygon G) {
  boolean c = false;
  int nvert = G.v.length;
  for (int i=0, j=nvert-1;i<nvert;j=i++) {
    if (
      ((G.v[i].y>P.y) != (G.v[j].y>P.y)) &&
      (P.x < (G.v[j].x-G.v[i].x) * (P.y-G.v[i].y) / (G.v[j].y-G.v[i].y) + G.v[i].x)
    ) {
      c = !c;
    }
  }
  return c;
};
  
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
