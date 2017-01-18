package org.infpls.noxio.game.module.game.dao;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.infpls.noxio.game.module.game.dao.user.UserDao;
import org.infpls.noxio.game.module.game.dao.server.ServerInfoDao;
import org.infpls.noxio.game.module.game.dao.lobby.LobbyDao;

@Component
public class DaoContainer {
  
  private final UserDao userDao;
  private final LobbyDao lobbyDao;
  
  @Autowired
  private ServerInfoDao serverInfoDao;
  
  public DaoContainer() {
    userDao = new UserDao();
    lobbyDao = new LobbyDao();
  }

  public UserDao getUserDao() { return userDao;  }
  public ServerInfoDao getServerInfoDao() { return serverInfoDao;  }
  public LobbyDao getLobbyDao() { return lobbyDao;  }
}
