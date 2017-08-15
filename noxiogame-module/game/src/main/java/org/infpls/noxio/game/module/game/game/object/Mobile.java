package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.session.ingame.PacketG15;
import org.infpls.noxio.game.module.game.util.Intersection;
import org.infpls.noxio.game.module.game.util.Intersection.Instance;

public abstract class Mobile extends GameObject {
  private final float radius, weight, friction;
  private float height, vspeed;
  private boolean grounded;
  
  private static final float AIR_DRAG = 0.98f, FATAL_IMPACT_SPEED = 0.175f;
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
    /* -- Movement  -- */
    final Vec2 to = position.add(velocity);
    final List<Polygon> walls = game.map.getNearWalls(to, radius);
    final List<Polygon> floors = game.map.getNearFloors(to, radius);
    boolean fatalImpact = false;
    if(velocity.magnitude() > 0.00001f) {
      
      final Vec2[] mov = new Vec2[] {to, velocity};
      float aoi = Float.NaN;
      for(int i=0;i<5&&aoi!=0f&&!fatalImpact;i++) {                             // Bound max collision tests to 5 incase of an object being stuck in an area to small for it to fit!
        aoi = Math.max(collideWalls(mov, walls), 0f);
        fatalImpact = Math.min(Math.pow(aoi,2), 0.9)*velocity.magnitude()>FATAL_IMPACT_SPEED;
        for(int j=0;j<walls.size()&!fatalImpact;j++) {
          final Polygon w = walls.get(j);
          if(Intersection.pointInPolygon(mov[0], w)) {
            fatalImpact=true; break; /* @TODO: Move out of clipped wall for post death effects */
          }
        }
      }
      
      if(!fatalImpact) {
        setPosition(mov[0]);
        setVelocity(mov[1]);
      }
      else {
        final List<Instance> hits = new ArrayList();
        for(int i=0;i<walls.size();i++) {
          Instance test = Intersection.polygonLine(new Line2(position, to), walls.get(i));
          if(test != null) { hits.add(test); }
        }
        if(hits.size() < 1) { setPosition(to); setVelocity(new Vec2()); }
        else {
          Instance nearest = hits.get(0);
          for(int i=1;i<hits.size();i++) {
            if(hits.get(i).distance < nearest.distance) { nearest = hits.get(i); }
          }
          setPosition(nearest.intersection.add(nearest.normal.scale(radius*0.5f))); setVelocity(new Vec2());
        }
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

    if(fatalImpact || height < -6.0f) {
      kill();
    }
    
    if(isGrounded()) { setVelocity(velocity.scale(friction)); } // No ice skating on the lawn!
    else { setVelocity(velocity.scale(AIR_DRAG)); }             // No friction while moving airborne, but air drag is accounted.
  }
  
  /* Takes current position & velociyt as well as a list of wall polygons and returns the result of movement. */
  /* 0.0   - no hits, mov unchanged
     !0.0  - hit,     updated mov, returned value is impact angle
  */
  private float collideWalls(final Vec2[] mov, final List<Polygon> walls) {
    final List<Instance> hits = new ArrayList();
    for(int i=0;i<walls.size();i++) {
      final Polygon w = walls.get(i);
      Instance inst = Intersection.polygonCircle(mov[0], w, radius);
      if(inst != null) { hits.add(inst); }
    }
    if(hits.size() > 0) {
      Instance nearest = hits.get(0);
      for(int i=1;i<hits.size();i++) {
        if(hits.get(i).distance < nearest.distance) {
          nearest = hits.get(i);
        }
      }
      /* Move to point of impact */
      final Vec2 corrected = nearest.intersection.add(nearest.normal.scale(radius));
      /* Slide off nearest collision */
      float aoi = 1f-Math.abs(mov[1].dot(nearest.normal)); // 0.0 is straight into the wall 1.0 is parallel to it
      mov[1]=mov[1].scale((aoi*0.5f)+0.5f);
      mov[0]=corrected;
      return 1f-aoi;
    }
    return 0f;
  }
  
  public void popup(float power) { vspeed += (power > 0.0f ? power : 0.0f); }
  public boolean isGrounded() { return grounded; }
  
  public float getRadius() { return radius; }
  public float getWeight() { return weight; }
  public float getHeight() { return height; }
  public float getVSpeed() { return vspeed; }
}
