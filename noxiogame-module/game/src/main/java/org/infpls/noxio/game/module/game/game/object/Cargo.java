package org.infpls.noxio.game.module.game.game.object; 

import java.util.ArrayList;
import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Cargo extends Player {
  public static enum Permutation {
    CRG_N(0, UserUnlocks.Key.CHAR_CARGO, new Mobile.HitStun[]{Mobile.HitStun.Fire}),
    CRG_VO(1, UserUnlocks.Key.ALT_CARGOVO, new Mobile.HitStun[]{Mobile.HitStun.Fire}),
    CRG_PL(2, UserUnlocks.Key.ALT_CARGOPLUS, new Mobile.HitStun[]{Mobile.HitStun.Fire}),
    CRG_RB(3, UserUnlocks.Key.ALT_CARGORAINBOW, new Mobile.HitStun[]{Mobile.HitStun.FireRainbow}),
    CRG_GLD(4, UserUnlocks.Key.ALT_CARGOGOLD, new Mobile.HitStun[]{Mobile.HitStun.FirePurple}),
    CRG_DEL(5, UserUnlocks.Key.ALT_CARGODELTA, new Mobile.HitStun[]{Mobile.HitStun.FireBlue}),
    CRG_BLK(6, UserUnlocks.Key.ALT_CARGOBLACK, new Mobile.HitStun[]{Mobile.HitStun.FireBlack}),
    CRG_MC(7, UserUnlocks.Key.ALT_CARGOMINE, new Mobile.HitStun[]{Mobile.HitStun.Fire}),
    CRG_RET(8, UserUnlocks.Key.ALT_CARGORETRO, new Mobile.HitStun[]{Mobile.HitStun.Fire});
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    public final Mobile.HitStun[] hits;
    Permutation(int permutation, UserUnlocks.Key unlock, Mobile.HitStun[] hits) {
       this.permutation = permutation;
       this.unlock = unlock;
       this.hits = hits;
    }
  }
  
  private static final int PUNCH_COOLDOWN_LENGTH = 45, PUNCH_CHARGE_LENGTH = 35, PUNCH_STUN_LENGTH = 30, PUNCH_PENALTY_TIME = 3, PUNCH_HITBOX_TIME = 3;
  private static final int KICK_COOLDOWN_LENGTH = 60, KICK_LENGTH = 7;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float PUNCH_IMPULSE = 1.65f, PUNCH_HITBOX_SIZE = 0.65f, PUNCH_HITBOX_OFFSET = 0.5f, PUNCH_RECOIL=0.33f;
  private static final float KICK_MIN_IMPULSE = 0.145f, KICK_MAX_IMPULSE = 0.425f, KICK_FALL_DAMPEN = 0.25f, KICK_RADIUS = 0.55f, KICK_OFFSET = 0.05f;
  
  private final List<Mobile> activeHit;     // Objects hit by active punch
  private Vec2 punchDirection, kickDirection;
  private boolean chargePunch;
  private int punchCooldown, kickCooldown, chargeTimer, kickTimer, hitboxTimer;
  private final Permutation cargoPermutation;
  public Cargo(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Cargo(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    cargoPermutation = perm;
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Special */
    activeHit = new ArrayList();
    
    /* Timers */
    punchCooldown = 0;
    kickCooldown = 0;
    
    punchDirection = new Vec2(1, 0);
    chargePunch = false;
    chargeTimer = 0;
    hitboxTimer = 0;
    
    kickDirection = new Vec2(1, 0);
    kickTimer = 0;
  }
  
  @Override
  public void movement() {
    if(kickTimer > 0) { return; }
    super.movement();
  }
  
  /* Updates various timers */
  @Override
  public void timers() {
    super.timers();
    if(hitboxTimer > 0) { hitboxTimer--; punching(); }
    if(chargeTimer > 0) { chargeTimer--; }
    if(chargePunch && chargeTimer <= 0) { punch(); }
    if(punchCooldown > 0) { punchCooldown--; }
    if(kickCooldown > 0) { kickCooldown--; }
    if(kickTimer > 0) { kicking(); }
    else { intangible = false; }
  }

  @Override   /* Charge Punch */
  public void actionA() {
    if(punchCooldown <= 0) {
      punchCooldown = PUNCH_COOLDOWN_LENGTH;
      effects.add("atk");
      chargePunch = true;
      chargeTimer = PUNCH_CHARGE_LENGTH;
      punchDirection = look;
    }
  }
  
  /* Do Punch */
  private void punch() {
    effects.add("pun");
    cameraShake(Mobile.CameraShake.MEDIUM);
    chargePunch = false;
    activeHit.clear();   
    hitboxTimer = PUNCH_HITBOX_TIME;
    
    punching(); // Hitbox test for first frame
    
    setVelocity(velocity.add(punchDirection.inverse().scale(PUNCH_RECOIL)));
    channelTimer = PUNCH_PENALTY_TIME;
  }
  
  /* Hitbox active for punch */
  private void punching() {
    if(getHeight() > -0.5) {
      final float rad = (float)(Math.PI/180.0);
      final Polygon hitbox = new Polygon(new Vec2[] {
        position.add(punchDirection.scale(PUNCH_HITBOX_OFFSET)).add(punchDirection.scale(PUNCH_HITBOX_SIZE)),
        position.add(punchDirection.scale(PUNCH_HITBOX_OFFSET)).add(punchDirection.rotate((float)(120*rad)).scale(PUNCH_HITBOX_SIZE)),
        position.add(punchDirection.scale(PUNCH_HITBOX_OFFSET)).add(punchDirection.rotate((float)(240*rad)).scale(PUNCH_HITBOX_SIZE))
      });
      
      final List<Mobile> hits = hitTest(hitbox);
      for(int i=0;i<hits.size();i++) {
        final Mobile mob = hits.get(i);
        if(activeHit.contains(mob)) { continue; }
       
        final Vec2 normal = punchDirection;
        mob.stun(PUNCH_STUN_LENGTH, cargoPermutation.hits[0], this, Mobile.CameraShake.HEAVY);
        mob.knockback(normal.scale(PUNCH_IMPULSE), this);
        activeHit.add(mob);
      }
    }
  }
  
  @Override   /* Kick */
  public void actionB() {
    if(kickCooldown <= 0) {
      kickCooldown = KICK_COOLDOWN_LENGTH;
      effects.add("mov");
      kickTimer = KICK_LENGTH;
      kickDirection = look;
      intangible = true;
      setVelocity(kickDirection.scale(Math.min(KICK_MIN_IMPULSE, velocity.magnitude())));
      baseImp = velocity.magnitude();
      
      drop();
    }
  }
  
  private float baseImp;
  public void kicking() { /* @TODO: POW USAGE HERE IS FUBAR */
    kickTimer--;
    setVelocity(kickDirection.scale(baseImp + (((1f-(kickTimer/KICK_LENGTH))*0.75f)+0.25f)*KICK_MAX_IMPULSE));
    setVSpeed(getVSpeed()*(KICK_FALL_DAMPEN*1f-((float)(Math.pow((1f-(kickTimer/KICK_LENGTH)),2f)))));
    
    final Vec2 kickBox = position.add(kickDirection.scale(KICK_OFFSET));
    
    final List<Mobile> hits = hitTest(kickBox, KICK_RADIUS);
    for(int i=0;i<hits.size();i++) {
      final Mobile mob = hits.get(i);
      final float pow = 1f-(float)(Math.pow(mob.getPosition().distance(kickBox)/(KICK_RADIUS+mob.getRadius()), 1.5f));
      mob.knockback(kickDirection.scale(pow), this);
    }
  }
  
  @Override
  public void jump() {
    if(kickTimer > 0) { return; }
    super.jump();
  }

  @Override
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
  
  @Override
  public void stun(int time, Mobile.HitStun type, int impact, Mobile.CameraShake shake) {
    super.stun(time, type, impact, shake);
    chargePunch = false;
    chargeTimer = 0;
    punchCooldown = 0;
    kickTimer = 0;
    kickCooldown = 0;
  }
  
  @Override
  public String type() { return "crg"; }
}
