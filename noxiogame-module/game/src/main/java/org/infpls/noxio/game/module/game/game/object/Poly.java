package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Poly extends Player {
  public static enum Permutation {
    POL_N(0, UserUnlocks.Key.CHAR_POLY, new Mobile.HitStun[]{Mobile.HitStun.Electric, Mobile.HitStun.Generic});
        
    public final int permutation;
    public final UserUnlocks.Key unlock;
    public final Mobile.HitStun[] hits;
    Permutation(int permutation, UserUnlocks.Key unlock, Mobile.HitStun[] hits) {
       this.permutation = permutation;
       this.unlock = unlock;
       this.hits = hits;
    }
  }
  
  private static final int POLY_INIT_COUNT = 3, POLY_MAX_COUNT = 12;
  
  public Vec2 lastTo;
  private final List<PolyBit> bits;
  
  private final Permutation polyPermutation;
  public Poly(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Poly(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    polyPermutation = perm;
    
    /* Settings */
    intangible = true;
    immune = true;
    
    /* Timers */

    /* State */
    lastTo = position;
    bits = new ArrayList();
    for(int i=0;i<POLY_INIT_COUNT;i++) {
      final float rotat = (float)(Math.PI * 2 * ((float)i/(POLY_INIT_COUNT)));
      final Vec2 magic = new Vec2((float)Math.sin(rotat), (float)-Math.cos(rotat)).normalize().scale(PolyBit.POLY_BIT_RADIUS*1.05f);
      final PolyBit pb = new PolyBit(game, game.createOid(), position.add(magic), polyPermutation, team, this);
      game.addObject(pb);
      bits.add(pb);
    }
  }
  
  private void createBit() {
    if(bits.size() < POLY_MAX_COUNT) {
      final PolyBit pb = new PolyBit(game, game.createOid(), position, polyPermutation, team, this);
      pb.setColor(color);
      game.addObject(pb);
      bits.add(pb);
    }
  }
  
  @Override
  /* Always send objective false so it doesn't render on client */
  public void generateUpdateData(final StringBuilder sb) {
    final Controller c = game.getControllerByObject(this);
    final String name = c!=null?c.user.display:""; 
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    look.toString(sb); sb.append(";");
    sb.append(speed); sb.append(";");
    sb.append(name.replaceAll("(\\;|\\,)", "")); sb.append(";");
    sb.append(0); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  /* Override - Passthrough to bits */
  @Override
  public void setInput(final Vec2 to, final boolean move) {
    lastTo = to;
    for(int i=0;i<bits.size();i++) {
      final PolyBit pb = bits.get(i);
      pb.setInput(to, move);
    }
  }
  
  /* Override - Passthrough to bits */
  @Override
  public void queueAction(final String a) {
    for(int i=0;i<bits.size();i++) {
      bits.get(i).queueAction(a);
    }
    action.add(a);
  }
  
  /* Override, do nothing basically */
  @Override
  public void movement() { }
  
  /* Override - Call bit steps */
  @Override
  public void step() {
    super.step();
    
    int c = 0; // How many bits are alive
    for(int i=0;i<bits.size();i++) {
      final PolyBit pb = bits.get(i);
      if(!pb.destroyed && !pb.dead) { c++; }
      if(pb.destroyed) { bits.remove(i--); }
    }
    
    if(c < 1) { destroyx(); }
  }
  
  /* We are not doing this */
  @Override
  public void pickups() { }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    
  }

  @Override   /* Shine */
  public void actionA() {
    
  }
  
  @Override   /* Wavedash */
  public void actionB() {
    
  }

  @Override
  public void taunt() {
    
  }
  
  @Override
  public void physics() {
    /* Polys main class is more or less a dummy and just passes inputs to it's parts. No physics, just position at center of group */
    
    Vec2 avg = new Vec2();
    int c = 0;
    float w = 0f; // Total weighting
    for(int i=0;i<bits.size();i++) {
      final PolyBit pb = bits.get(i);
      
      if(pb.dead || pb.destroyed) { continue; }
      
      float h = ((-KILL_PLANE ) + 1.5f) + Math.min(-1.5f, Math.max(KILL_PLANE, pb.height)); // Height clamped
      float hr = (float)Math.pow(h/3f, 2); // Height ratio
      
      avg = avg.add(pb.position.scale(hr));  // Weight camera centering against below stage height, eases camera back instaed of snapping when a bit dies.
      w += hr;
      c++;
    }
    
    if(c < 1) { return; } // If all bits are dead/destroyed then simply leave camera where it is.
    
    avg = avg.scale(1f/w);
    position = avg;
  }
  
  @Override
  public void killCredit(Player player) {
    createBit();
  }
  
  @Override
  public void destroyx() {
    for(int i=0;i<bits.size();i++) {
      bits.get(i).destroyx();
    }
    if(destroyed()) { return; }
    kill();
    destroyed = true;
  }
  
  
  @Override
  public void objective() {
    super.objective();
    for(int i=0;i<bits.size();i++) {
      final PolyBit pb = bits.get(i);
      pb.objective();
    }
  }
  
  @Override
  public void dejective() {
    super.dejective();
    for(int i=0;i<bits.size();i++) {
      final PolyBit pb = bits.get(i);
      pb.dejective();
    }
  }
  
  /* Pass through color to bits */
  @Override
  public void setColor(int c) {
    super.setColor(c);
    for(int i=0;i<bits.size();i++) {
      final PolyBit pb = bits.get(i);
      pb.setColor(c);
    }
  }

  @Override
  public String type() { return "pol"; }
}
