package org.infpls.noxio.game.module.game.dao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.infpls.noxio.game.module.game.dao.user.UserDao;
import org.infpls.noxio.game.module.game.dao.server.InfoDao;
import org.infpls.noxio.game.module.game.dao.lobby.LobbyDao;

@Component
public class DaoContainer {
  
  private final UserDao userDao;
  private final LobbyDao lobbyDao;
  
  @Autowired
  private InfoDao infoDao;
  
  public DaoContainer() {
    userDao = new UserDao();
    lobbyDao = new LobbyDao();
  }
  
  /* This is hacky @TODO: */
  @PostConstruct
  public void start() {
    lobbyDao.start(infoDao);
  }
  
  @PreDestroy
  public void destroy() {
    lobbyDao.destroy();
  }

  public UserDao getUserDao() { return userDao;  }
  public InfoDao getInfoDao() { return infoDao;  }
  public LobbyDao getLobbyDao() { return lobbyDao;  }
}
