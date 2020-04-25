package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;

public class FlagAssault extends Flag {
  public FlagAssault(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, team);
    
    /* Settings */
    teamAttack = true; enemyAttack = false;
  }
  
  @Override
  public void step() {    
    if(isHeld()) {
      for(int i=0;i<game.objects.size();i++) {
        final GameObject obj = game.objects.get(i);
        if(obj.is(Types.FLAG)) {
          final Flag f = (Flag)(obj);
          if(f.team != team && f.base.distance(position) < f.getRadius()+held.getRadius()) { f.score(held); reset(); }
        }
      }
    }
    
    super.step();
  }
  
  @Override
  public boolean touch(Player p) {
    if(isHeld()) { return false; }
    if(p.team == team) { return pickup(p); }
    else { return flagReturn(p); }
  }
  
  @Override
  public void announceTaken(int team) {
    ((TeamGame)game).announceTeam(team, "fs");
    ((TeamGame)game).announceTeam(team==0?1:0, "ft");
  }
  
  @Override
  public void announceReset() {
    ((TeamGame)game).announceTeam(team, "ff");
  }
  
  @Override
  public void announceReturn() {
    ((TeamGame)game).announceTeam(team, "ff"); 
  }
  
}
