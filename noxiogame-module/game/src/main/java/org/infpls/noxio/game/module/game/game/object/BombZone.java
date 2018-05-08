package org.infpls.noxio.game.module.game.game.object;

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class BombZone extends GameObject {
  
  public final Vec2 size;
  public final Polygon hitbox;
  
  public BombZone(final NoxioGame game, final int oid, final Vec2 position, final int team, final Vec2 size) {
    super(game, oid, position, 0, team);
    /* Bitmask Type */
    bitIs = bitIs | Types.ZONE;
    
    /* Vars */
    this.size = size;
    final Vec2[] v = new Vec2[] {
      position.add(new Vec2(size.x*0.5f, size.y*0.5f)),
      position.add(new Vec2(size.x*-0.5f, size.y*0.5f)),
      position.add(new Vec2(size.x*-0.5f, size.y*-0.5f)),
      position.add(new Vec2(size.x*0.5f, size.y*-0.5f))
    };
    hitbox = new Polygon(v);
  }
    
  @Override
  public void step() {    
    for(int i=0;i<game.objects.size();i++) {
      final GameObject obj = game.objects.get(i);
      if(obj.is(Types.BOMB)) {
        final Bomb bomb = (Bomb)obj;
        final boolean inside = Intersection.pointInPolygon(bomb.getPosition(), hitbox);
        final Intersection.Instance inst = Intersection.polygonCircle(bomb.getPosition(), hitbox, bomb.getRadius());
        if(inside || inst != null) {
          bomb.arm();
        }
      }
    }
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
  public void kill() {
    dead = true;
  }
  
  @Override
  public String type() { return "zon"; }
}
