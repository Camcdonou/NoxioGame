package org.infpls.noxio.game.module.game.game;

import java.io.*;
import java.util.*;
import org.infpls.noxio.game.module.game.game.object.*;
import org.springframework.core.io.*;

public class NoxioMap {
  private final String name, description;
  private final List<String> gametypes;
  
  private final List<Tile> tileSet;
  private final List<Tile> doodadSet;
  
  private final List<Doodad> doodads;
  private final List<Spawn> spawns;
  
  private final int[] bounds;         // Map boundary in tiles
  private final int[][] map;          // 2D Array of tile indexes
  private final List<Polygon> floor;  // Floor collision data
  private final List<Polygon> wall;   // Wall collision data
  
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
      if(t.length < 2) { continue; } /* @TODO: Error reporting... */
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
    map = new int[bounds[1]][bounds[0]];
    for(int i=0, k=0;i<bounds[1];i++) {
      for(int j=0;j<bounds[0];j++) {
       map[bounds[1]-i-1][j] = Integer.parseInt(m[k++]); //Read map y backwards because GL renders from bottom not top
      }
    }
    
    /* Field#4 - Doodad Pallete */
    final String[] dp = fields[4].split(";");
    doodadSet = new ArrayList();
    for(int i=0;i<dp.length;i++) {
      final String[] d = ts[i].split(",");
      if(d.length < 2) { continue; } /* @TODO: Error reporting... */
      doodadSet.add(new Tile(d[0], d[1]));
    }
    
    /* Field#5 - Doodads */
    final String[] dds = fields[5].split(";");
    doodads = new ArrayList();
    for(int i=0;i<dds.length;i++) {
      final String[] d = dds[i].split(",");
      if(d.length < 4) { continue; } /* @TODO: Error reporting... */
      doodads.add(new Doodad(Integer.parseInt(d[0]), new Vec2(Float.parseFloat(d[1]), Float.parseFloat(d[2])), Float.parseFloat(d[3])));
    }
    
    /* Field#6 - Collision Floor */
    final String[] cf = fields[6].split(";");
    floor = new ArrayList();
    for(int i=0;i<cf.length;i++) {
      final String[] f = cf[i].split(",");
      if(f.length < 6) { continue; } /* @TODO: Error reporting... */
      final Vec2[] p = new Vec2[f.length/2]; //If this isn't even then you are fucked in so many different ways.
      for(int j=0, k=0;j<f.length;j+=2) {
        p[k++] = new Vec2(Float.parseFloat(f[j])-0.5f, Float.parseFloat(f[j+1])-0.5f); /* @TODO: Offset is sort of??? Unexplained??? Map editor error? */
      }
      floor.add(new Polygon(p));
    }
    
    /* Field#7 - Collision Wall */
    final String[] cw = fields[7].split(";");
    wall = new ArrayList();
    for(int i=0;i<cw.length;i++) {
      final String[] w = cw[i].split(",");
      if(w.length < 6) { continue; } /* @TODO: Error reporting... */
      final Vec2[] p = new Vec2[w.length/2]; //If this isn't even then you are fucked in so many different ways.
      for(int j=0, k=0;j<w.length;j+=2, k++) {
        p[k] = new Vec2(Float.parseFloat(w[j])-0.5f, Float.parseFloat(w[j+1])-0.5f); /* @TODO: Offset is sort of??? Unexplained??? Map editor error? */
      }
      wall.add(new Polygon(p));
    }
    
    /* Field#8 - Spawns */
    final String[] sps = fields[8].split(";");
    spawns = new ArrayList();
    for(int i=0;i<sps.length;i++) {
      final String[] spwn = sps[i].split(",");
      if(spwn.length < 4) { continue; } /* @TODO: Error reporting... */
      final String[] sgts = new String[spwn.length-4];
      for(int j=4, k=0;j<spwn.length;j++, k++) {
        sgts[k] = spwn[j];
      }
      spawns.add(new Spawn(spwn[0], Integer.parseInt(spwn[1]), new Vec2(Float.parseFloat(spwn[2]), Float.parseFloat(spwn[3])), sgts));
    }
  }
  
  /* Return a list of all floors that are within range of potential interaction with the given position and radius */
  public List<Polygon> getNearFloors(final Vec2 pos, final float r) {
    List<Polygon> f = new ArrayList();
    for(int i=0;i<floor.size();i++) {
      if(floor.get(i).c.distance(pos) <= floor.get(i).r + r + 0.1f) { // 0.1f is an extra offset to eliminate possible floating point errors
        f.add(floor.get(i));
      }
    }
    return f;
  }
  
  /* Return a list of all walls that are within range of potential interaction with the given position and radius */
  public List<Polygon> getNearWalls(final Vec2 pos, final float r) {
    List<Polygon> w = new ArrayList();
    for(int i=0;i<wall.size();i++) {
      if(wall.get(i).c.distance(pos) <= wall.get(i).r + r + 0.1f) { // 0.1f is an extra offset to eliminate possible floating point errors
        w.add(wall.get(i));
      }
    }
    return w;
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
  
  public List<Spawn> getSpawns(final String type) {
    final List<Spawn> sps = new ArrayList();
    for(int i=0;i<spawns.size();i++) {
      if(spawns.get(i).getType().equals(type)) { sps.add(spawns.get(i)); }
    }
    return sps;
  }
  
  public String getName() { return name; }
  public String getDescription() { return description; }
  public List<String> getGametypes() { return gametypes; }
  public List<Tile> getTileSet() { return tileSet; }
  public int[] getBounds() { return bounds; }
  public int[][] getMap() { return map; }
  
  public class Tile {
    private final String model, material;
    public Tile(final String model, final String material) {
      this.model = model; this.material = material;
    }
    public String getModel() { return model; }
    public String getMaterial() { return material; }
  }
  
  public class Doodad {
    private final int index;
    private final Vec2 pos;
    private final float rot;
    public Doodad(final int index, final Vec2 pos, final float rot) {
      this.index = index;
      this.pos = pos; this.rot = rot;
    }
    public int getIndex() { return index; }
    public Vec2 getPos() { return pos; }
    public float getRot() { return rot; }
  }
  
  public class Spawn {
    private final String type;
    private final int team;
    private final Vec2 pos;
    private final String[] gametype;
    public Spawn(final String type, final int team, final Vec2 pos, final String[] gametype) {
      this.type = type; this.team = team;
      this.pos = pos;
      this.gametype = gametype;
    }
    public String getType() { return type; }
    public int getTeam() { return team; }
    public Vec2 getPos() { return pos; }
    public String[] getGametype() { return gametype; }
  }
}
