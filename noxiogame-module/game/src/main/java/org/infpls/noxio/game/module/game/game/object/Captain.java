package org.infpls.noxio.game.module.game.game.object; 

import java.util.List;
import org.infpls.noxio.game.module.game.dao.user.UserUnlocks;
import org.infpls.noxio.game.module.game.game.*;

public class Captain extends Player {
  public static enum Permutation {
    CRG_N(0, UserUnlocks.Key.CHAR_CARGO);
    
    public final int permutation;
    public final UserUnlocks.Key unlock;
    Permutation(int permutation, UserUnlocks.Key unlock) {
       this.permutation = permutation;
       this.unlock = unlock;
    }
  }
  
  private static final int PUNCH_COOLDOWN_LENGTH = 45, PUNCH_CHARGE_LENGTH = 35, PUNCH_STUN_LENGTH = 30;
  private static final int KICK_COOLDOWN_LENGTH = 60, KICK_LENGTH = 7;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float PUNCH_IMPULSE = 1.65f, PUNCH_HITBOX_SIZE = 0.75f, PUNCH_HITBOX_OFFSET = 0.5f;
  private static final float KICK_MIN_IMPULSE = 0.145f, KICK_MAX_IMPULSE = 0.425f, KICK_FALL_DAMPEN = 0.25f, KICK_RADIUS = 0.55f, KICK_OFFSET = 0.05f;
  
  private Vec2 punchDirection, kickDirection;
  private boolean chargePunch;
  private int punchCooldown, kickCooldown, chargeTimer, kickTimer;
  public Captain(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm) {
    this(game, oid, position, perm, -1);
  }
  
  public Captain(final NoxioGame game, final int oid, final Vec2 position, final Permutation perm, final int team) {
    super(game, oid, position, perm.permutation, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    punchCooldown = 0;
    kickCooldown = 0;
    
    punchDirection = new Vec2(1, 0);
    chargePunch = false;
    chargeTimer = 0;
    
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
    chargePunch = false;
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
        final Vec2 normal = punchDirection;
        mob.stun(PUNCH_STUN_LENGTH, this);
        mob.knockback(normal.scale(PUNCH_IMPULSE), this);
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
  public void stun(int time) {
    super.stun(time);
    chargePunch = false;
    chargeTimer = 0;
    punchCooldown = 0;
    kickTimer = 0;
    kickCooldown = 0;
  }
  
  @Override
  public String type() { return "crg"; }
}
