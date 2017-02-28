package org.infpls.noxio.game.module.game.game;

import java.io.*;
import java.util.*;
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
       map[i][j] = Integer.parseInt(m[k++]);
      }
    }
    
    /* Field#4 - Collision Data */
    final String[] c = fields[4].split(",");
    k = 0;
    collision = new int[bounds[1]][bounds[0]];
    for(int i=0;i<bounds[1];i++) {
      for(int j=0;j<bounds[0];j++) {
       collision[i][j] = Integer.parseInt(c[k++]);
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
}
