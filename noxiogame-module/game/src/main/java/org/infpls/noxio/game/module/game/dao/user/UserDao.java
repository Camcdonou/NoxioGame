package org.infpls.noxio.game.module.game.dao.user;

import java.io.IOException;
import java.util.*;
import org.springframework.web.socket.WebSocketSession;

import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.DaoContainer;

/* UserDao handles both user info and logged in user NoxioSessions.
   This is because theres is an overlap in data here
   and seperating these things seems counter-intuitive.
*/

public class UserDao {
  private final List<NoxioSession> sessions; /* This is a list of all active user NoxioSessions. */
  
  public UserDao() {
    sessions = new ArrayList();
  }
  
  public NoxioSession createSession(final WebSocketSession webSocket, DaoContainer dao) throws IOException {
    NoxioSession session = new NoxioSession(webSocket, dao);
    sessions.add(session);
    return session;
  }
  
  public void destroySession(final WebSocketSession webSocket) throws IOException {
    for(int i=0;i<sessions.size();i++) {
      if(sessions.get(i).getWebSocketId().equals(webSocket.getId())) {
        sessions.get(i).destroy();
        sessions.remove(i);
        return;
      }
    }
  }
  
  public NoxioSession getSessionByUser(final String user) {
    for(int i=0;i<sessions.size();i++) {
      if(sessions.get(i).loggedIn()) {
        if(sessions.get(i).getUser().equals(user)) {
          return sessions.get(i);
        }
      }
    }
    return null;
  }
}
