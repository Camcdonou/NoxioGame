package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Intersection;

public class Captain extends Player {
  private static final int PUNCH_COOLDOWN_LENGTH = 45, PUNCH_CHARGE_LENGTH = 35, PUNCH_STUN_LENGTH = 30;
  private static final int KICK_COOLDOWN_LENGTH = 45, KICK_LENGTH = 12;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float PUNCH_IMPULSE = 1.65f, PUNCH_HITBOX_SIZE = 0.75f, PUNCH_HITBOX_OFFSET = 0.5f, KICK_IMPULSE = 0.4f, KICK_VELOCITY_DAMPEN = 0.25f, KICK_ACCEL = 0.15f;
  
  private Vec2 punchDirection, kickDirection;
  private boolean chargePunch;
  private int punchCooldown, kickCooldown, chargeTimer, kickTimer;
  public Captain(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Captain(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.player.captain", position, team);
    
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

      for(int i=0;i<game.objects.size();i++) {
        if(game.objects.get(i).getType().startsWith("obj.mobile")&&game.objects.get(i)!=this) {
          Mobile mob = (Mobile)game.objects.get(i);
          final boolean full = Intersection.pointInPolygon(mob.getPosition(), hitbox);
          final Intersection.Instance inst = Intersection.polygonCircle(mob.getPosition(), hitbox, mob.getRadius());
          if(full || inst != null) {
            if(mob.getType().startsWith("obj.mobile.player")) {
              final Player ply = (Player)mob;
              ply.stun(PUNCH_STUN_LENGTH);
            }
            //final Vec2 normal = mob.getPosition().subtract(position).normalize();
            final Vec2 normal = punchDirection;
            mob.knockback(normal.scale(PUNCH_IMPULSE), this);
            final Controller c = game.getControllerByObject(this);
            if(c != null) { mob.tag(c); }
          }
        }
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
    }
  }
  
  public void kicking() {
    kickTimer--;
    setVelocity(kickDirection.scale(Math.min(kickTimer*KICK_ACCEL, KICK_IMPULSE)).add(velocity.scale(KICK_VELOCITY_DAMPEN)));
    setVSpeed(getVSpeed()*KICK_VELOCITY_DAMPEN);
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
}
