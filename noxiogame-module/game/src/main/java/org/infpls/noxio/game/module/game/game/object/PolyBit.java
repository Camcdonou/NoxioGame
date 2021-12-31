package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.game.*;

public class PolyBit extends Player {
  
  public static final float POLY_BIT_RADIUS = 0.25f, POLY_BIT_SIZE_PENALTY = 1.45f;
  private static final float DASH_INVERSE_RADIUS = 0.95f;
  private static final int BLIP_COOLDOWN_LENGTH = 13, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 25, BLIP_RANDOM_DELAY_MAX = 4;
  private static final int DASH_COOLDOWN_LENGTH = 18, DASH_POWER_MAX = 60, DASH_POWER_ADD = 35, DASH_STUN_TIME = 35;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float BLIP_IMPULSE = 0.65f, DASH_IMPULSE = 0.25f, BLIP_RADIUS = POLY_BIT_RADIUS + 0.1f;
  
  public final Poly owner;
  private int blipCooldown, dashCooldown, blipPower, dashPower;
  private int delayedBlip;
  private final Poly.Permutation polyBitPermutation;
  public PolyBit(final NoxioGame game, final int oid, final Vec2 position, final Poly.Permutation perm, Poly owner) {
    this(game, oid, position, perm, -1, owner);
  }
  
  public PolyBit(final NoxioGame game, final int oid, final Vec2 position, final Poly.Permutation perm, final int team, Poly owner) {
    super(game, oid, position, perm.permutation, team);
    polyBitPermutation = perm;
    
    /* Settings */
    radius = POLY_BIT_RADIUS; weight = 0.5f; friction = 0.725f; springyness = 0.75f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    blipCooldown = 0;
    dashCooldown = 0;
    blipPower = BLIP_POWER_MAX;
    dashPower = 0;
    delayedBlip = 0;
    
    /* State */
    this.owner = owner;
  }
  
  /* Don't send name data for bits so we dont have a nameplate on every single one of them */
  @Override
  public void generateUpdateData(final StringBuilder sb) {
    final Controller c = game.getControllerByObject(this);
    final String name = "";
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    look.toString(sb); sb.append(";");
    sb.append(speed); sb.append(";");
    sb.append(name.replaceAll("(\\;|\\,)", "")); sb.append(";"); /* @TODO: send this a single time instead all the time */
    sb.append(objective?1:0); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(blipCooldown > 0) { blipCooldown--; }
    if(dashCooldown > 0) { dashCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
    if(dashPower > 0) { dashPower--; }
    if(delayedBlip == 1) { blip(); }
    if(delayedBlip > 0) { delayedBlip--; }
  }

  @Override   /* Shine */
  public void actionA() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      if(hits.size() > 0) { blip(); }
      else { delayedBlip = (int)(Math.random()*BLIP_RANDOM_DELAY_MAX)+1; }
    }
  }
  
  private void blip() {
      final List<Mobile> hits = hitTest(position, BLIP_RADIUS);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun((int)(BLIP_STUN_TIME*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), polyBitPermutation.hits[0], owner, Mobile.CameraShake.LIGHT);
        mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.4f)+0.6f)), owner);
      }
      
      delayedBlip = 0;
      blipPower = 0;
      effects.add("atk");
  }
  
  @Override   /* Wavedash */
  public void actionB() {
    final float centerToDist = owner.position.distance(owner.lastTo);
    if(dashCooldown <= 0 && dashPower < DASH_POWER_MAX) {
      drop();
      dashCooldown = DASH_COOLDOWN_LENGTH;
      dashPower += DASH_POWER_ADD;
      setVelocity(velocity.add((centerToDist>DASH_INVERSE_RADIUS?look:position.subtract(owner.position).normalize()).scale(DASH_IMPULSE)));  // LOL
      effects.add("mov");
      if(dashPower >= DASH_POWER_MAX) { stun(DASH_STUN_TIME, polyBitPermutation.hits[1], 0, Mobile.CameraShake.LIGHT); }
    }
  }

  @Override
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
  
  /* Adjusted to not hit own bits */
  @Override
  public List<Mobile> hitTest(final Vec2 p, final float r) {
    final List<Mobile> hits = super.hitTest(p, r);
    for(int i=0;i<hits.size();i++) {
      final Mobile m = hits.get(i);
      if(m instanceof PolyBit) {
        PolyBit pb = (PolyBit)m;
        if(pb.owner == owner) {
          hits.remove(i--);
        }
      }
    }
    return hits;
  }
  
  /* Tags go up to main poly class */
  @Override
  public void tag(final Player player) {
    owner.tag(player);
  }
  
  @Override
  public void stun(int time, Mobile.HitStun type, int impact, Mobile.CameraShake shake) {
    super.stun(time, type, impact, shake);
    delayedBlip = 0;
  }
  
  /* Bits are small and take extra knockback. we just multiply the incoming values a bit */
  /* Only adjusts knockback that comes from another player, we don't want to boost popup/knockback from ourselves or neutral sources */
  @Override
  public void knockback(final Vec2 impulse, final Player p) {
    super.knockback(impulse.scale(POLY_BIT_SIZE_PENALTY), p);
  }
  
  @Override
  public void popup(float power, final Player p) {
    super.popup(power * POLY_BIT_SIZE_PENALTY, p);
  }
    
  @Override
  public String type() { return "bit"; }
}
