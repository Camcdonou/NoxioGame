package org.infpls.noxio.game.module.game.dao.user;

import java.io.IOException;
import java.util.*;
import org.springframework.web.socket.WebSocketSession;

import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.util.Oak;

/* UserDao handles both user info and logged in user NoxioSessions.
   This is because theres is an overlap in data here
   and seperating these things seems counter-intuitive.
*/

public class UserDao {
  private final List<NoxioSession> sessions; /* This is a list of all active user NoxioSessions. */
  
  public UserDao() {
    sessions = Collections.synchronizedList(new ArrayList());
  }
  
  public synchronized NoxioSession createSession(final WebSocketSession webSocket, DaoContainer dao) throws IOException {
    NoxioSession session = new NoxioSession(webSocket, dao);
    sessions.add(session);
    return session;
  }
  
  public synchronized void destroySession(final WebSocketSession webSocket) throws IOException {
    for(int i=0;i<sessions.size();i++) {
      final NoxioSession s = sessions.get(i);
      if(s.getWebSocketId().equals(webSocket.getId())) {
        try { s.destroy(); sessions.remove(i); }
        catch(Exception ex) { Oak.log(Oak.Type.SESSION, Oak.Level.ERR, "Failed to remove session: " + (s.loggedIn() ? s.getUser() : "S##"+s.getSessionId()), ex); }
        return;
      }
    }
  }
  
  public synchronized NoxioSession getSessionByUser(final String user) {
    for(int i=0;i<sessions.size();i++) {
      final NoxioSession s = sessions.get(i);
      if(s.loggedIn()) {
        if(s.getUser().equals(user)) {
          return s;
        }
      }
    }
    return null;
  }

  public synchronized List<String> getOnlineUserList() {
    final List<String> users = new ArrayList();
    for(int i=0;i<sessions.size();i++) {
      final NoxioSession session = sessions.get(i);
      users.add((session.loggedIn() ? session.getUser() : "S##"+session.getSessionId()) + "  |  " + (session.isOpen() ? "(OKAY)" : "(INVALID)"));
    }
    return users;
  }
  
  public int getOnlineUserCount() {
    return sessions.size();
  }
}
