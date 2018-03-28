package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.Controller;
import org.infpls.noxio.game.module.game.game.NoxioGame;
import org.infpls.noxio.game.module.game.util.Intersection;
import org.infpls.noxio.game.module.game.util.Intersection.Instance;

public abstract class Mobile extends GameObject {
  protected final float GROUNDED_BIAS_POS = 0.0001f, GROUNDED_BIAS_NEG = -0.4f;
  protected static final float AIR_DRAG = 0.98f, FATAL_IMPACT_SPEED = 0.335f;
  
  protected Controller tagged;
  protected int tagTime;
  
  protected float radius, weight, friction;
  
  public float height;
  private float vspeed;
  protected boolean intangible, immune, grounded;
    
  public Mobile(final NoxioGame game, final int oid, final Vec2 position, final int permutation, final int team) {
    super(game, oid, position, permutation, team);
    /* Bitmask Type */
    bitIs = bitIs | Types.MOBILE;
    
    /* Vars */
    tagged = null;
    tagTime = -1;
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    
    /* States */
    height = 0.0f; vspeed = 0.0f; grounded = false; intangible = false; immune = false;
  }
  
  protected void physics() {
    /* -- Objects -- */
    if(!intangible) {                                                            // Ignore if intangible
      for(int i=0;i<game.objects.size();i++) {
        final GameObject obj = game.objects.get(i);
        if(obj != this && obj.is(Types.MOBILE)) {                                // Object must be something physical (IE a barrel or a pillar or a player)
          final Mobile mob = (Mobile)obj;
          final float combinedRadius = radius+mob.getRadius();
          if(!mob.intangible && position.distance(mob.getPosition()) < combinedRadius && Math.abs(getHeight()-mob.getHeight()) < combinedRadius) {
            final float dist = position.distance(obj.getPosition());
            final Vec2 norm = position.subtract(obj.getPosition()).normalize();
            final float weightOffset = weight/(mob.getWeight()+weight);
            final Vec2 push = norm.scale((0.5f*weightOffset)*((combinedRadius-dist)/combinedRadius));
            setVelocity(velocity.add(push));
          }
        }
      }
    }
    /* -- Movement  -- */
    final Vec2 to = position.add(velocity);
    final List<Polygon> walls = game.map.getNearWalls(position, radius + velocity.magnitude());
    boolean fatalImpact = false;
    
    if(velocity.magnitude() > 0.00001f) {
      
      final Vec2[] mov = new Vec2[] {position, velocity};
      float aoi = rayWalls(mov, walls);
        fatalImpact = Math.min(Math.pow(aoi, 0.65f), 0.75)*velocity.magnitude()>FATAL_IMPACT_SPEED;
      for(int i=0;i<5&&aoi!=0f&&!fatalImpact;i++) {                             // Bound max collision tests to 5 incase of an object being stuck in an area to small for it to fit!
        aoi = Math.max(collideWalls(mov, walls), 0f);
        fatalImpact = Math.min(Math.pow(aoi, 0.65f), 0.75)*velocity.magnitude()>FATAL_IMPACT_SPEED;
        for(int j=0;j<walls.size()&!fatalImpact;j++) {
          final Polygon w = walls.get(j);
          if(Intersection.pointInPolygon(mov[0], w)) {
            fatalImpact=true; break; /* @TODO: Move out of clipped wall for post death effects */
          }
        }
      }
      
      setPosition(mov[0]);
      setVelocity(mov[1]);
    }
    
    /* Height */
    final List<Polygon> floors = game.map.getNearFloors(position, radius);
    boolean floorBounded = collideFloors(position, floors);
    
    if(floorBounded) {
      if(height > GROUNDED_BIAS_NEG && height < GROUNDED_BIAS_POS) {
        if(vspeed <= 0f) {
          height = 0f; vspeed = 0f;       // Grounded not moving up
          grounded = true;
        }
        else {
          height += vspeed; vspeed -= 0.03f; // Grounded and moving up
          grounded = false;
        }
      }
      else {
        if(height >= GROUNDED_BIAS_POS && height + vspeed < GROUNDED_BIAS_POS) {     // Falling above floor and hit it
          fatalImpact = vspeed >= FATAL_IMPACT_SPEED;
          height = 0f; vspeed = 0f;
          grounded = true;
        }
        else {
          height += vspeed; vspeed -= 0.03f;   // Falling while above or below the floor 
          grounded = false;
        }
      }
    }
    else {
      height += vspeed; vspeed -= 0.03f;     // Above void
      grounded = false;
    }

    /* Final */

    if(fatalImpact || height < -6.0f) {
      kill();
    }
    
    if(grounded) { setVelocity(velocity.scale(friction)); } // No ice skating on the lawn!
    else { setVelocity(velocity.scale(AIR_DRAG)); }         // No friction while moving airborne, but air drag is accounted.
  }
  
  private float rayWalls(final Vec2[] mov, final List<Polygon> walls) {
    final List<Instance> hits = new ArrayList();
    final Vec2 to = mov[0].add(mov[1]);
    for(int i=0;i<walls.size();i++) {
      final Polygon w = walls.get(i);
      Instance inst = Intersection.polygonLine(new Line2(mov[0], to), w);
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
      /* Reflect Off Wall */
      final Vec2 ihd = to.subtract(mov[0]).normalize().inverse();      // Inverse Hit Directino
      final float dp = nearest.normal.dot(ihd);
      final Vec2 ref = nearest.normal.scale(2*dp).subtract(ihd).normalize(); // Reflection normal
      /* Slide off nearest collision */
      float aoi = (float)Math.pow(1f-Math.max(0f, mov[1].normalize().inverse().dot(nearest.normal)), 0.5f); // 0.0 is straight into the wall 1.0 is parallel to it
      mov[1]=ref.scale(mov[1].magnitude()*((aoi*0.5f)+0.5f));
      mov[0]=corrected;
      return 1f-aoi;
    }
    
    mov[0]=to;
    return Float.NaN;
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
      float aoi = (float)Math.pow(1f-Math.max(0f, mov[1].normalize().inverse().dot(nearest.normal)), 0.5f); // 0.0 is straight into the wall 1.0 is parallel to it
      mov[1]=mov[1].scale((aoi*0.5f)+0.5f);
      mov[0]=corrected;
      return 1f-aoi;
    }
    return 0f;
  }
  
  /* This fuction is simple and just returns a boolean true/false if the object is over solid ground */
  private boolean collideFloors(final Vec2 pos, final List<Polygon> floors) {
    for(int i=0;i<floors.size();i++) {
      if(Intersection.pointInPolygon(position, floors.get(i))) {
        return true;
      }
      else {
        final Instance inst = Intersection.polygonCircle(pos, floors.get(i), radius);
        if(inst != null && inst.distance < (radius*0.5)) { return true; }
      }
    }
    return false;
  }
  
  public void stun(final int time, final Player p) { stun(time); tag(p); }
  public void stun(int time) { /* No Effect, Override */ }
  public void knockback(final Vec2 impulse, final Player p) { knockback(impulse); tag(p); }
  public void knockback(final Vec2 impulse) { setVelocity(velocity.add(impulse)); }
  public void popup(float power, final Player p) { popup(power); tag(p); }
  public void popup(float power) { vspeed += (power > 0.0f ? power : 0.0f); }
  
  @Override
  public void tag(final Player player) {
    final Controller c = game.getControllerByObject(player);
    if(c != null) { tagged = c; tagTime = game.getFrame(); }
  }
  
  public float getRadius() { return radius; }
  public float getWeight() { return weight; }
  public float getHeight() { return height; }
  public float getVSpeed() { return vspeed; }
  public void setHeight(final float h) { height = h; }
  public void setVSpeed(final float s) { vspeed = s; }
}
