package org.infpls.noxio.game.module.game.game;

import java.io.*;
import java.util.*;
import org.infpls.noxio.game.module.game.game.object.Line2;
import org.infpls.noxio.game.module.game.game.object.Vec2;
import org.springframework.core.io.*;

public class NoxioMap {
  private final String name, description;
  private final List<String> gametypes;
  
  private final List<Tile> tileSet;
  private final int[] bounds;
  private final int[][] map, collision;
  public NoxioMap(final String mapName) throws IOException {
    final String data = readFile("map/" + mapName + ".map");
    final String[] fields = data.split("\\|");
    
    /* Field#0 - Info */
    final String[] info = fields[0].split(";");
    name = info[0];
    description = info[1];
    gametypes = new ArrayList();
    final String[] gts = info[2].split(",");
    for(int i=0;i<gts.length;i++) { 
      gametypes.add(gts[i]);
    }
    
    /* Field#1 - Tile Set */
    final String[] ts = fields[1].split(";");
    tileSet = new ArrayList();
    for(int i=0;i<ts.length;i++) {
      final String[] t = ts[i].split(",");
      tileSet.add(new Tile(t[0], t[1]));
    }
    
    /* Field#2 - Map Bounds */
    final String[] bnds = fields[2].split(",");
    bounds = new int[2];
    for(int i=0;i<bounds.length;i++) {
      bounds[i] = Integer.parseInt(bnds[i]);
    }
    
    /* Field#3 - Map Data */
    final String[] m = fields[3].split(",");
    int k = 0;
    map = new int[bounds[1]][bounds[0]];
    for(int i=0;i<bounds[1];i++) {
      for(int j=0;j<bounds[0];j++) {
       map[bounds[1]-i-1][j] = Integer.parseInt(m[k++]); //Read map y backwards because GL renders from bottom not top
      }
    }
    
    /* Field#4 - Collision Data */
    final String[] c = fields[4].split(",");
    k = 0;
    collision = new int[bounds[1]][bounds[0]];
    for(int i=0;i<bounds[1];i++) {
      for(int j=0;j<bounds[0];j++) {
       collision[bounds[1]-i-1][j] = Integer.parseInt(c[k++]); //Read map y backwards because GL renders from bottom not top
      }
    }
  }
  
  private String readFile(final String path) throws IOException {
    final Resource resource = new ClassPathResource(path);
    final InputStream in = resource.getInputStream();
    final BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    final StringBuilder sb = new StringBuilder();
    String line;
    while((line=br.readLine()) != null) {
       sb.append(line);
    }
    br.close();
    in.close();
    return sb.toString();
  }
  
  /* Returns walls facing the normal of <Line2 direction> (@FIXME not doing this yet) within the <float radius> of the <Line2 direction> */
  public List<Line2> getWallsNear(final Line2 direction, float radius) {
    final List<int[]> tilesHit = new ArrayList();
    final Vec2 dirNorm = direction.b.subtract(direction.a).normalize();
    final int steps = (int)Math.floor(direction.a.subtract(direction.b).magnitude());
    for(int s=0;s<=steps;s++) {
      final Vec2 step = direction.a.add(dirNorm.scale(s));
      final int x = (int)Math.floor(step.x);
      final int y = (int)Math.floor(step.y);
      for(int i=0;i<5;i++) {
        for(int j=0;j<5;j++) {
          boolean dupe = false;
          for(int k=0;k<tilesHit.size();k++) {
            final int[] chk = tilesHit.get(k);
            if(chk[0]==x+i-2 && chk[1]==y+j-2) { dupe=true; break; }
          }
          if(dupe || !(x+i-2<bounds[0]) || !(y+j-2<bounds[1]) || !(x+i-2>=0) || !(y+j-2>=0)) { break; } // Out of bounds & duplicate check
          tilesHit.add(new int[]{x+i-2,y+j-2}); /* @FIXME does not account for radius properly */
        }
      }
    }
    final List<Line2> walls = new ArrayList();
    for(int i=0;i<tilesHit.size();i++) {
      final int[] tile = tilesHit.get(i);
      switch(collision[tile[1]][tile[0]]) {
        /* No Collision */
        case 0 : { break; }
        /* Solid 1x1 Block */
        case 1 : { final Line2[] lines = CollisionDef.createSolidBlock(new Vec2(tile[0],tile[1])); for(int j=0;j<lines.length;j++) { walls.add(lines[j]); } break; }
        /* Solid 1x1 Pit */
        case 2 : { break; }
        /* Default to No Collision */
        default : { break; }
      }
    }
    return walls;
  }
  
  public String getName() { return name; }
  public String getDescription() { return description; }
  public List<String> getGametypes() { return gametypes; }
  public List<Tile> getTileSet() { return tileSet; }
  public int[] getBounds() { return bounds; }
  public int[][] getMap() { return map; }
  public int[][] getCollision() { return collision; }
  
  public class Tile {
    private final String model, material;
    public Tile(final String model, final String material) {
      this.model = model; this.material = material;
    }
    public String getModel() { return model; }
    public String getMaterial() { return material; }
  }
  
  public static class CollisionDef {
    public static Line2[] createSolidBlock(final Vec2 pos) {
      return new Line2[] {
        new Line2(new Vec2(-0.5f,-0.5f).add(pos), new Vec2( 0.5f,-0.5f).add(pos)),
        new Line2(new Vec2( 0.5f,-0.5f).add(pos), new Vec2( 0.5f, 0.5f).add(pos)),
        new Line2(new Vec2( 0.5f, 0.5f).add(pos), new Vec2(-0.5f, 0.5f).add(pos)),
        new Line2(new Vec2(-0.5f, 0.5f).add(pos), new Vec2(-0.5f,-0.5f).add(pos))
      };
    }
  }
}
