package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.game.object.*;
import org.infpls.noxio.game.module.game.util.Intersection;
import org.infpls.noxio.game.module.game.util.Intersection.Instance;

public abstract class Mobile extends GameObject {
  private final float radius, weight, friction;
  private float height, vspeed;
  private boolean grounded;
  private static final float AIR_DRAG = 0.98f;
  public Mobile(final NoxioGame game, final long oid, final String type, final Vec2 position, final float radius, final float weight, final float friction) {
    super(game, oid, type, position);
    this.height = 0.0f; this.vspeed = 0.0f; this.grounded = false;
    this.radius = radius; this.weight = weight; this.friction = friction;
  }
  
  public void physics() {
    /* -- Objects -- */
    if(height > -0.5) {                                             // If this object is to low we ignore object collision.
      for(int i=0;i<game.objects.size();i++) {
        final GameObject obj = game.objects.get(i);
        if(obj != this && obj.getType().startsWith("obj.mobile")) { // Object must be something physical (IE a barrel or a pillar or a player)
          final Mobile mob = (Mobile)obj;
          final float combinedRadius = radius+mob.getRadius();
          if(position.distance(mob.getPosition()) < combinedRadius && mob.getHeight() > -0.5) {
            final float dist = position.distance(obj.getPosition());
            final Vec2 norm = position.subtract(obj.getPosition()).normalize();
            final float weightOffset = weight/(mob.getWeight()+weight);
            //final float aoi = 1.0f-Math.abs((velocity.normalize().isNaN() ? new Vec2(0.0f, 1.0f) : velocity.normalize()).dot(norm.isNaN() ? new Vec2(0.0f, 1.0f) : norm));
            //setVelocity(velocity.scale((aoi*0.25f)+0.75f)); // Pushing another object head on slows this object down BUGGED!
            final Vec2 push = (norm.isNaN() ? new Vec2(0.0f, 1.0f) : norm).scale((0.5f*weightOffset)*((combinedRadius-dist)/combinedRadius));
            setVelocity(velocity.add(push));
          }
        }
      }
    }
    /* -- Intersection -- */
    
    /* -- Movement  -- */
    final Vec2 to = position.add(velocity);
    final List<Polygon> walls = game.map.getNearWalls(to, radius);
    final List<Polygon> floors = game.map.getNearFloors(to, radius);
    float impact = 0.0f;
    if(velocity.magnitude() > 0.00001) {
      
      boolean move = true;
      for(int i=0;i<walls.size();i++) {
        final Polygon w = walls.get(i);
        if(Intersection.pointInPolygon(to, w)) {
          move = false; impact = 1.0f; break;
          /* @TODO: Move out of clipped wall for post death effects */
        }
      }
      if(move) {
        final List<Instance> hits = new ArrayList();
        for(int i=0;i<walls.size();i++) {
          final Polygon w = walls.get(i);
          Instance inst = Intersection.polygonCircle(to, w, radius);
          if(inst != null) { hits.add(inst); }
        }
        if(hits.size() > 0) {
          Instance nearest = hits.get(0);
          for(int i=1;i<hits.size();i++) {
            if(hits.get(i).distance < nearest.distance) {
              nearest = hits.get(i);
            }
          }
          move = false; impact = (radius-nearest.distance)/radius;
          /* Move to point of impact */
          final Vec2 corrected = nearest.intersection.add(nearest.normal.scale(radius));
          /* Slide off nearest collision */
          float aoi = 1.0f-Math.abs(velocity.dot(nearest.normal)); // 0.0 is straight into the wall 1.0 is parallel to it
          setVelocity(velocity.scale((aoi*0.5f)+0.5f));
          setPosition(corrected);
        }
      }
      
      if(move) {
        setPosition(to);
      }
    }
    
    /* Height */
    boolean floorBounded = false;
    for(int i=0;i<floors.size();i++) {
      if(Intersection.pointInPolygon(position, floors.get(i))) {
        floorBounded = true; break;
      }
    }
    if(floorBounded) {
      if(height > -0.4f && height <= 0.0f) {
        if(vspeed < 0.0f) {
          height = 0.0f; vspeed = 0.0f;       // Grounded not moving up
          grounded = true;
        }
        else {
          height += vspeed; vspeed -= 0.03f; // Grounded but above floor
          grounded = false;
        }
      }
      else {
        height += vspeed; vspeed -= 0.03f;   // Falling while below floor @FIXME check for full passthrough at high speeds?
        grounded = false;
      }
    }
    else {
      height += vspeed; vspeed -= 0.03f;     // Falling
      grounded = false;
    }

    /* Final */

    if(impact > 0.75f || height < -6.0f) {
      kill();
    }
    
    if(isGrounded()) { setVelocity(velocity.scale(friction)); } // No ice skating on the lawn!
    else { setVelocity(velocity.scale(AIR_DRAG)); }             // No friction while moving airborne, but air drag is accounted.
  }
  
  public void popup(float power) { vspeed += (power > 0.0f ? power : 0.0f); }
  public boolean isGrounded() { return grounded; }
  
  public float getRadius() { return radius; }
  public float getWeight() { return weight; }
  public float getHeight() { return height; }
  public float getVSpeed() { return vspeed; }
}
