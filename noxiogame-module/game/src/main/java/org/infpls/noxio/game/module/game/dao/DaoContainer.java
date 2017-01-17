package org.infpls.noxio.game.module.game.dao;

import org.springframework.stereotype.Component;

import org.infpls.noxio.game.module.game.dao.user.UserDao;
import org.infpls.noxio.game.module.game.dao.server.ServerInfoDao;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DaoContainer {
  
  private final UserDao userDao;
  
  @Autowired
  private ServerInfoDao serverInfoDao;
  
  public DaoContainer() {
    userDao = new UserDao();
  }

  public UserDao getUserDao() { return userDao;  }
  public ServerInfoDao getServerInfoDao() { return serverInfoDao;  }
}
