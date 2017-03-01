package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.util.Intersection;

public abstract class Mobile extends GameObject {
  private final float radius, weight, friction;
  public Mobile(final NoxioGame game, final long oid, final String type, final Vec2 position, final float radius, final float weight, final float friction) {
    super(game, oid, type, position);
    this.radius = radius; this.weight = weight; this.friction = friction;
  }
  
  /* @FIXME optimization. This is probaly slow due to how complex it is. */
  /* @FIXME on high velocity movements (magnitude >= 1.0f) we need to add ray test to prevent pass through */
  public void physics() {
    /* Check if any other objects are close enough to push this one */
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      if(obj != this && obj.getType().contains("obj.mobile")) { // Object must be something physical (IE a barrel or a pillar or a player)
        final float combinedRadius = radius+((Mobile)obj).getRadius();
        if(position.distance(obj.getPosition()) < combinedRadius) {
          final float dist = position.distance(obj.getPosition());
          final Vec2 norm = position.subtract(obj.getPosition()).normalize();
          final float weightOffset = weight/(((Mobile)obj).getWeight()+weight);
          final float aoi = 1.0f-Math.abs((velocity.normalize().isNaN() ? new Vec2(0.0f, 1.0f) : velocity.normalize()).dot(norm.isNaN() ? new Vec2(0.0f, 1.0f) : norm));
          setVelocity(velocity.scale((aoi*0.25f)+0.75f)); // Pushing another object head on slows this object down
          final Vec2 push = (norm.isNaN() ? new Vec2(0.0f, 1.0f) : norm).scale((0.5f*weightOffset)*((combinedRadius-dist)/combinedRadius));
          setVelocity(velocity.add(push));
        }
      }
    }
    if(velocity.magnitude() > 0.00001) {
      /* Get a list of walls that are near where this object is moving */
      final Vec2 to = position.add(velocity);
      final List<Line2> walls = game.map.getWallsNear(new Line2(position, to), radius);
      final List<Intersection.Instance> intersections = new ArrayList();
      /* Test for collisions */
      for(int i=0;i<walls.size();i++) {
        Intersection.Instance intersection = Intersection.lineCircle(to, walls.get(i), radius);
        if(intersection != null) { intersections.add(intersection); }
      }
      if(intersections.isEmpty()) { setPosition(to); } /* No collisions! */
      else {
        /* Get nearest collision */
        Intersection.Instance nearest = intersections.get(0);
        for(int i=1;i<intersections.size();i++) {
          final Intersection.Instance next = intersections.get(i);
          if(to.distance(next.intersection) < to.distance(nearest.intersection)) { nearest = next; }
        }
        /* Slide off nearest collision */
        float aoi = 1.0f-Math.abs(velocity.dot(nearest.normal)); // 0.0 is straight into the wall 1.0 is parallel to it
        final Vec2 movedPos = nearest.intersection.add(nearest.normal.scale(radius));
        setVelocity(velocity.scale((aoi*0.5f)+0.5f));
        /* Check if movement has caused an intersection */
        intersections.clear();
        for(int i=0;i<walls.size();i++) {
          Intersection.Instance intersection = Intersection.lineCircle(movedPos, walls.get(i), radius);
          if(intersection != null) { intersections.add(intersection); }
        }
        if(intersections.isEmpty()) { setPosition(movedPos); } /* No collisions! */
        else {
          /* Get nearest collision */
          nearest = intersections.get(0);
          for(int i=1;i<intersections.size();i++) {
            final Intersection.Instance next = intersections.get(i);
            if(movedPos.distance(next.intersection) < movedPos.distance(nearest.intersection)) { nearest = next; }
          }
          /* Slide off nearest collision */
          aoi = 1.0f-Math.abs(velocity.dot(nearest.normal)); // 0.0 is straight into the wall 1.0 is parallel to it
          final Vec2 correctedPos = nearest.intersection.add(nearest.normal.scale(radius));
          setVelocity(velocity.scale((aoi*0.5f)+0.5f));
          setPosition(correctedPos);
        }
      }
    }
    setVelocity(velocity.scale(friction));
  }
  
  public float getRadius() { return radius; }
  public float getWeight() { return weight; }
}
