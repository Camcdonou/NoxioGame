package org.infpls.noxio.game.module.game.dao;

import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import org.infpls.noxio.game.module.game.dao.user.UserDao;
import org.infpls.noxio.game.module.game.dao.lobby.LobbyDao;
import org.infpls.noxio.game.module.game.util.Oak;
import org.infpls.noxio.game.module.game.util.Settable;

@Component
public class DaoContainer {
  
  private final UserDao userDao;
  private final LobbyDao lobbyDao;
  
  public DaoContainer() {
    Settable.update();                    // This call to Settable.update() ensures all properties are loaded before we start using them.
    Oak.open();                           // Starts logging
    userDao = new UserDao();
    lobbyDao = new LobbyDao();
  }

  @PreDestroy
  public void destroy() {
    lobbyDao.destroy();
    Oak.close();
  }

  public UserDao getUserDao() { return userDao;  }
  public LobbyDao getLobbyDao() { return lobbyDao;  }
}
