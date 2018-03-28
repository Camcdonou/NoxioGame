package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import static org.infpls.noxio.game.module.game.game.object.Player.VERTICAL_HIT_TEST_LENIENCY; /* L M A O */
import org.infpls.noxio.game.module.game.util.Intersection;

public class Bomb extends Mobile {
  private static final float DETONATION_RADIUS = 0.65f, DETONATION_IMPULSE = 0.65f, IMPACT_BOUNCE_MULT = 0.85f;
  private static final int DETONATION_STUN_LENGTH = 20;
  
  private Player owner;                // Player who threw this bomb
  
  private final List<String> effects;  // List of actions performed that will be sent to the client on the next update

  private int detonationTimer;
  public Bomb(final NoxioGame game, final int oid, final Vec2 position, final int team, final int timer, final Player owner) {
    super(game, oid, position, 0, team);
    
    this.owner = owner;
    
    effects = new ArrayList();
    
    /* Settings */
    radius = 0.1f; weight = 0.1f; friction = 0.625f;
    
    /* State */
    intangible = true;
    
    /* Timers */
    detonationTimer = timer;
  }
  
  @Override
  protected void physics() {
    Vec2 to = position.add(velocity);
    final float toh = getHeight() + getVSpeed();
    final List<Polygon> walls = game.map.getNearWalls(to, radius);
    final List<Polygon> floors = game.map.getNearFloors(to, radius);
    
    final List<Intersection.Instance> insts = new ArrayList();
    for(int i=0;i<walls.size();i++) {
      final Intersection.Instance inst = Intersection.polygonLine(new Line2(position, to), walls.get(i));
      if(inst != null) { insts.add(inst); }
    }
    
    /* @TODO: U LAZY FUG */
    if(insts.size() > 0) {
      final Intersection.Instance hit = insts.get(0);
      final Vec2 ihd = to.subtract(position).normalize().inverse();      // Inverse Hit Directino
      final float dp = hit.normal.dot(ihd);
      final Vec2 ref = hit.normal.scale(2*dp).subtract(ihd).normalize(); // Reflection normal
      setVelocity(ref.scale(velocity.magnitude()*IMPACT_BOUNCE_MULT));
      to = hit.intersection.add(hit.normal.scale(radius));
      effects.add("imp");
    }
    
    boolean overFloor = false;
    for(int i = 0;i<floors.size();i++) {
      if(Intersection.pointInPolygon(to, floors.get(i))) { overFloor = true; break; }
    }
    
    if(overFloor && getHeight() > 0f && toh <= 0f) {
      setHeight(0f);
      setVSpeed(-getVSpeed()*IMPACT_BOUNCE_MULT);
      effects.add("imp");
      grounded = true;
    }
    else {
      setHeight(getHeight()+getVSpeed());
      setVSpeed((getVSpeed()-0.03f)*AIR_DRAG);
      grounded = false;
    }

    setPosition(to);
    if(grounded) { setVelocity(velocity.scale(friction)); }
    else { setVelocity(velocity.scale(AIR_DRAG)); }
  }
  
  @Override
  public void step() {
    physics();
    if(detonationTimer > 0) { detonationTimer--; }
    else { detonate(); }
  }
  
  @Override
  public void post() {
    effects.clear();
  }
   
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<float height>;<float vspeed>;<vec2 look>;<float speed>;<string name>;<int onBase>;<string[] effects>
     Note: onBase is an int where 1 is true, 0 is false. Booleans suck for networking with javscript
  */
  public void generateUpdateData(final StringBuilder sb) {
    final Controller c = game.getControllerByObject(this);
    final String name;
    if(c != null) { name = c.getUser(); }
    else { name = ""; }
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  public void score(final Player p) {
    game.reportObjective(game.getControllerByObject(p), this);
  }
  
  @Override
  public void knockback(final Vec2 impulse, final Player player) {
    this.owner = player;
    super.knockback(impulse, player);
  }
  
  private void detonate() {
    final List<Mobile> hits = hitTest(position, DETONATION_RADIUS);
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      if(mob == owner) { continue; }
      if(mob.type().equals("bmb")) { continue; }
      final Vec2 normal = mob.getPosition().subtract(position).normalize();
      mob.stun(DETONATION_STUN_LENGTH, owner);
      mob.knockback(normal.scale(DETONATION_IMPULSE), owner);
    }
    kill();
  }
  
  /* Copy pasted from Player.java since this is not a subclass of that... */
  /* Test given circle at <vec2 p> w/ radius <float r> against other players and return hits */
  public List<Mobile> hitTest(final Vec2 p, final float r) {
    final List<Mobile> hits = new ArrayList();
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      if(obj != this && obj.is(Types.MOBILE)) {
        final Mobile mob = (Mobile)obj;
        final float cr = mob.getRadius() + r;
        if(p.distance(mob.getPosition()) <= cr && Math.abs(getHeight()-mob.getHeight()) <= cr*VERTICAL_HIT_TEST_LENIENCY) {
          hits.add(mob);
        }
      }
    }
    return hits;
  }
  
  @Override
  public void kill() {
    dead = true;
  }
  
  @Override
  public String type() { return "bmb"; }
}
