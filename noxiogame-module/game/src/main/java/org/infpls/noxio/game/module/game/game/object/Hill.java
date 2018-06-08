package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Hill extends GameObject {
  private static final int TIMER_SCORE_ADJUST = 90;
  
  protected Vec2 size;
  private Polygon hitbox;
  private final Map<Integer, Integer> scoreTimers;
  
  public Hill(final NoxioGame game, final int oid, final Vec2 position, final Vec2 size) {
    super(game, oid, position, 0);
    /* Bitmask Type */
    bitIs = bitIs | Types.HILL;
    
    /* Vars */
    scoreTimers = new HashMap();
    
    moveTo(position, size);
  }
    
  @Override
  public void step() {    
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      final Controller con = game.getControllerByObject(obj);
      if(con != null && obj.is(Types.MOBILE)) {
        final Mobile mob = (Mobile)obj;
        final boolean inside = Intersection.pointInPolygon(mob.getPosition(), hitbox);
        final Intersection.Instance inst = Intersection.polygonCircle(mob.getPosition(), hitbox, mob.getRadius());
        final int timer = scoreTimers.containsKey(obj.getOid())?scoreTimers.get(obj.getOid()):0;
        if(inside || inst != null) {
          if(timer > TIMER_SCORE_ADJUST) { game.reportObjective(con, obj); scoreTimers.put(obj.getOid(), 1); }
          else { scoreTimers.put(obj.getOid(), timer+1); }
        }
        else {
          scoreTimers.put(obj.getOid(), Math.max(0, timer-1));
        }
      }
    }
  }
  
  public final void moveTo(final Vec2 pos, final Vec2 size) {
    setPosition(pos);
    this.size = size;
    
    final Vec2[] v = new Vec2[] {
      position.add(new Vec2(size.x*0.5f, size.y*0.5f)),
      position.add(new Vec2(size.x*-0.5f, size.y*0.5f)),
      position.add(new Vec2(size.x*-0.5f, size.y*-0.5f)),
      position.add(new Vec2(size.x*0.5f, size.y*-0.5f))
    };
    hitbox = new Polygon(v);
    
    scoreTimers.clear();
  }
  
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<int team>;<vec2 pos>;<vec2 size>;
  */
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    size.toString(sb); sb.append(";");
  }
  
  @Override
  public boolean isGlobal() { return true; }
  
  @Override
  public void kill() { }
  
  @Override
  public void destroyx() { }
  
  @Override
  public String type() { return "hil"; }
}
