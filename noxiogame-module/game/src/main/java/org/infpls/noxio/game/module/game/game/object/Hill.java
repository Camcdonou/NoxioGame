package org.infpls.noxio.game.module.game.game.object;

import java.util.*;
import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Hill extends GameObject {
  private static final int TIMER_SCORE_ADJUST = 90;
  
  protected Vec2 size;
  private final Map<Integer, Integer> scoreTimers;
  
  public Hill(final NoxioGame game, final int oid, final Vec2 position, final Vec2 size) {
    super(game, oid, position);
    /* Bitmask Type */
    bitIs = bitIs | Types.HILL;
    
    /* Vars */
    this.size = size;
    scoreTimers = new HashMap();
  }
    
  @Override
  public void step() {
    final Vec2[] v = new Vec2[] {
      position.add(new Vec2(size.x*0.5f, size.y*0.5f)),
      position.add(new Vec2(size.x*-0.5f, size.y*0.5f)),
      position.add(new Vec2(size.x*-0.5f, size.y*-0.5f)),
      position.add(new Vec2(size.x*0.5f, size.y*-0.5f))
    };
    final Polygon hitbox = new Polygon(v);
    
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
  
  public void moveTo(final Vec2 pos, final Vec2 size) {
    setPosition(pos);
    this.size = size;
    scoreTimers.clear();
  }
  
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<int team>;<vec2 pos>;<vec2 size>;
  */
  public void generateUpdateData(final StringBuilder sb) {
    final Controller c = game.getControllerByObject(this);
    final String name = c!=null?c.getUser():"";
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    sb.append(team); sb.append(";");
    position.toString(sb); sb.append(";");
    size.toString(sb); sb.append(";");
  }
  
  @Override
  public boolean isGlobal() { return true; }
  
  @Override
  public void kill() {
    /* You can't kill a game objective! */
  }
  
  @Override
  public String type() { return "hil"; }
}
