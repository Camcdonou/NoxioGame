package org.infpls.noxio.game.module.game.game;

import org.infpls.noxio.game.module.game.game.object.Flag;
import org.infpls.noxio.game.module.game.game.object.GameObject;
import org.infpls.noxio.game.module.game.game.object.Player;
import org.infpls.noxio.game.module.game.game.object.Vec2;

public class FlagNormal extends Flag {

  public FlagNormal(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, team);
  }
  
  @Override
  public void step() {
    if(isHeld()) {
      for(int i=0;i<game.objects.size();i++) {
        final GameObject obj = game.objects.get(i);
        if(obj.is(GameObject.Types.FLAG)) {
          final Flag f = (Flag)(obj);
          if(f.team != team && f.onBase() && f.getPosition().distance(position) < f.getRadius()+held.getRadius()) { f.score(held); reset(); }
        }
      }
    }
    
    super.step();
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(super.pickup(p)) {
      if(onBase()) { announceTaken(team); }
      return true;
    }
    return false;
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
    ((TeamGame)game).announceTeam(team, "fr"); 
  }
  
}
