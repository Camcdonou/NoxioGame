package org.infpls.noxio.game.module.game.game.object; 

import java.util.*;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Quad extends Player {
  public static enum Permutation {
    QUA_N(0, UserUnlocks.Key.CHAR_QUAD, new Mobile.HitStun[]{Mobile.HitStun.Slash}),
    QUA_VO(1, UserUnlocks.Key.ALT_QUADVO, new Mobile.HitStun[]{Mobile.HitStun.Slash}),
    QUA_RB(2, UserUnlocks.Key.ALT_QUADRAINBOW, new Mobile.HitStun[]{Mobile.HitStun.SlashRainbow}),
    QUA_GLD(3, UserUnlocks.Key.ALT_QUADGOLD, new Mobile.HitStun[]{Mobile.HitStun.SlashPurple}),
    QUA_DEL(4, UserUnlocks.Key.ALT_QUADDELTA, new Mobile.HitStun[]{Mobile.HitStun.Slash}),
    QUA_FIR(5, UserUnlocks.Key.ALT_QUADFIRE, new Mobile.HitStun[]{Mobile.HitStun.SlashFire}),
    QUA_LGN(6, UserUnlocks.Key.ALT_QUADLEGEND, new Mobile.HitStun[]{Mobile.HitStun.Slash}),
    QUA_RS(7, UserUnlocks.Key.ALT_QUADRUNE, new Mobile.HitStun[]{Mobile.HitStun.Slash});
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    public final Mobile.HitStun[] hits;
    Permutation(int permutation, UserUnlocks.Key unlock, Mobile.HitStun[] hits) {
       this.permutation = permutation;
       this.unlock = unlock;
       this.hits = hits;
    }
  }
  
  private static final int SLASH_COOLDOWN_LENGTH = 20, SLASH_COMBO_LENGTH = 3, SLASH_COMBO_DEGEN = 90, SLASH_STUN_LENGTH = 15, SLASH_COMBO_STUN_LENGTH = 25;
  private static final float SLASH_RANGE = 1.0f, SLASH_ANGLE = 120f, SLASH_SEGMENT_DISTANCE=5f, SLASH_IMPULSE = 0.55f, SLASH_COMBO_IMPULSE = 1.05f;
  private static final int COUNTER_COOLDOWN_LENGTH = 50, COUNTER_ACTIVE_LENGTH = 7, COUNTER_LAG_LENGTH = 35, COUNTER_IMPACT = 2, COUNTER_MIN_STUN = 5;
  private static final float COUNTER_MULTIPLIER = 1.5f, COUNTER_ANGLE = 45f, COUNTER_SEGMENT_DISTANCE=5f, COUNTER_RANGE = 1.25f, COUNTER_MIN_KNOCK = 0.55f;
  private static final int TAUNT_COOLDOWN_LENGTH = 60;
  
  private boolean channelCounter, counterHit;
  private float counterKnock, counterPop;
  private int counterStun;
  private Vec2 counterDirection;
  private int combo;
  private int slashCooldown, counterCooldown, comboTimer;
  private final Permutation quadPermutation;
  public Quad(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Quad(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    quadPermutation = perm;
    
    /* Settings */
    radius = 0.5f; weight = 0.9f; friction = 0.705f;
    moveSpeed = 0.0355f; jumpHeight = 0.175f;
    
    /* Timers */
    combo = 0;
    slashCooldown = 0;
    counterCooldown = 0;
    comboTimer = 0;
    
    channelCounter = false;
    counterHit = false;
    counterKnock = 0f;
    counterPop = 0f;
    counterStun = 0;
    counterDirection = null;
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(channelCounter && channelTimer <= 0) { channelCounter = false; }
    if(counterHit) { riposte(); }
    if(slashCooldown > 0) { slashCooldown--; }
    if(counterCooldown > 0) { counterCooldown--; }
    if(comboTimer > 0) { comboTimer--; }
    else if(comboTimer < 1 && combo > 0) { combo--; comboTimer = combo>0?SLASH_COMBO_DEGEN:0; }
  }
  
  @Override
  /* Player GameObject parameters:
     obj;<int oid>;<vec2 pos>;<vec2 vel>;<float height>;<float vspeed>;<vec2 look>;<float speed>;<string name>;<vec2 counterDirection>;<int objFlag>;<string[] effects>
  */
  public void generateUpdateData(final StringBuilder sb) {
    final Controller c = game.getControllerByObject(this);
    final String name = c!=null?c.getDisplay():"";
    
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
    sb.append(getHeight()); sb.append(";");
    sb.append(getVSpeed()); sb.append(";");
    look.toString(sb); sb.append(";");
    sb.append(speed); sb.append(";");
    sb.append(name); sb.append(";");
    if(counterDirection!=null) { counterDirection.toString(sb); }
    else { sb.append("1,0"); } sb.append(";");
    sb.append(objective?1:0); sb.append(";");
    for(int i=0;i<effects.size();i++) { sb.append(effects.get(i)); sb.append(","); }
    sb.append(";");
  }
  
  @Override   /* Slash */
  public void actionA() {
    if(slashCooldown <= 0) {
      slashCooldown = SLASH_COOLDOWN_LENGTH;
      final boolean isCombo = combo >= SLASH_COMBO_LENGTH;
      effects.add("atk");
      if(isCombo) { combo = 0; comboTimer = 0; effects.add("cmb"); }
      
      final float rad = (float)(Math.PI/180);
      final Polygon hitbox;
      final List<Vec2> verts = new ArrayList();
      final Vec2 start = look.scale(SLASH_RANGE).rotate(SLASH_ANGLE*rad*-0.5f);
      verts.add(position);
      for(float d=0f;d<SLASH_ANGLE*rad;d+=SLASH_SEGMENT_DISTANCE*rad) {
        verts.add(start.rotate(d).add(position));
      }
      hitbox = new Polygon(verts.toArray(new Vec2[0]));
      
      final List<Mobile> hits = hitTest(hitbox);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        final Vec2 normal = mob.getPosition().subtract(position).normalize();
        mob.stun(isCombo?SLASH_COMBO_STUN_LENGTH:SLASH_STUN_LENGTH, quadPermutation.hits[0], this, isCombo?Mobile.CameraShake.MEDIUM:Mobile.CameraShake.LIGHT);
        mob.knockback(normal.scale(isCombo?SLASH_COMBO_IMPULSE:SLASH_IMPULSE), this);
        combo++; comboTimer = SLASH_COMBO_DEGEN;
        effects.add(isCombo?"cht":"sht");
      }
      
      if(combo > SLASH_COMBO_LENGTH) { combo = SLASH_COMBO_LENGTH; }
      if(combo == SLASH_COMBO_LENGTH) { effects.add("rdy"); }
    }
  }
  
  @Override   /* Counter */
  public void actionB() {
    if(counterCooldown <= 0) {
      counterCooldown = COUNTER_COOLDOWN_LENGTH;
      channelCounter = true;
      channelTimer = COUNTER_LAG_LENGTH;
      counterDirection = null;
      counterHit = false;
      effects.add("cnt");
    }
  }
  
  public void riposte() {
    effects.add("rip");
    effects.add("crt");

    final float rad = (float)(Math.PI/180);
    final Vec2 dir = counterDirection!=null?counterDirection:look;
    final Polygon hitbox;
    final List<Vec2> verts = new ArrayList();
    final Vec2 start = dir.normalize().scale(COUNTER_RANGE).rotate(COUNTER_ANGLE*rad*-0.5f);
    verts.add(position);
    for(float d=0f;d<COUNTER_ANGLE*rad;d+=COUNTER_SEGMENT_DISTANCE*rad) {
      verts.add(start.rotate(d).add(position));
    }
    hitbox = new Polygon(verts.toArray(new Vec2[0]));
    
    final List<Mobile> hits = hitTest(hitbox);
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      final Vec2 normal = mob.getPosition().subtract(position).normalize();
      
      mob.stun((int)(Math.max(COUNTER_MIN_STUN, counterStun) * COUNTER_MULTIPLIER), quadPermutation.hits[0], this, COUNTER_IMPACT, Mobile.CameraShake.HEAVY);
      mob.knockback(normal.scale(Math.max(COUNTER_MIN_KNOCK, counterKnock) * COUNTER_MULTIPLIER), this);
      if(counterPop > 0) { mob.popup(counterPop * COUNTER_MULTIPLIER, this); }
      combo++; comboTimer = SLASH_COMBO_DEGEN;
      effects.add("cht");
      effects.add("rht");
      cameraShake(Mobile.CameraShake.MEDIUM);
    }

    if(combo > SLASH_COMBO_LENGTH) { combo = SLASH_COMBO_LENGTH; }
    if(combo == SLASH_COMBO_LENGTH) { effects.add("rdy"); }
    
    counterHit = false;
    channelCounter = false;
    channelTimer = 0;
    counterCooldown = 5;
    counterKnock = 0f;
    counterPop = 0f;
    counterStun = 0;
  }
  
  @Override
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
  
  @Override
  public void knockback(final Vec2 impulse, final Player player) {
    if(channelCounter && COUNTER_LAG_LENGTH-channelTimer < COUNTER_ACTIVE_LENGTH) {
      counterHit = true; counterKnock = impulse.magnitude(); counterDirection = player.getPosition().subtract(position).normalize();
      return; // Immune to stun/popup/knockback during counters active frames
    } 
    super.knockback(impulse, player);
  }
  
  @Override
  public void popup(float power, final Player player) {
    if(channelCounter && COUNTER_LAG_LENGTH-channelTimer < COUNTER_ACTIVE_LENGTH) {
      counterHit = true; counterPop = power; counterDirection = player.getPosition().subtract(position).normalize();
      return; // Immune to stun/popup/knockback during counters active frames
    } 
    super.popup(power, player);
  }
  
  @Override
  public void stun(int time, Mobile.HitStun type, final Player player, Mobile.CameraShake shake) {
    if(channelCounter && COUNTER_LAG_LENGTH-channelTimer < COUNTER_ACTIVE_LENGTH) {
      counterHit = true; counterStun = time; counterDirection = player.getPosition().subtract(position).normalize();
      return; // Immune to stun/popup/knockback during counters active frames
    } 
    super.stun(time, type, player, shake);
  }
  
  @Override
  public void stun(int time, Mobile.HitStun type, int impact, Mobile.CameraShake shake) {
    super.stun(time, type, impact, shake);
    channelCounter = false;
    channelTimer = 0;
    counterCooldown = 0;
  }
  
  @Override
  public String type() { return "qua"; }
}
