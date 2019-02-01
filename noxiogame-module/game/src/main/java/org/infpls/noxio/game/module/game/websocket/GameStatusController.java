package org.infpls.noxio.game.module.game.websocket;


import com.google.gson.*;
import java.util.List;
import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.util.Settable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class GameStatusController {
  
  @Autowired
  private DaoContainer dao;
    
  @RequestMapping(value = "/advinfo", method = RequestMethod.GET, produces = "application/json")
  public @ResponseBody ResponseEntity getAdvInfo() {
      Gson gson = new GsonBuilder().create();
      
      final Settable.ServerInfo info = Settable.getServerInfo();
      final List<String> users = dao.getUserDao().getOnlineUserList();
      final List<String> lobbies = dao.getLobbyDao().getOnlineLobbyList();
      
      return new ResponseEntity(gson.toJson(new AdvInfo(info.name, info.location, Settable.getAuthAddress(), users, lobbies)), HttpStatus.OK);
  }

  @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "application/json")
  public @ResponseBody ResponseEntity getInfo() {
      Gson gson = new GsonBuilder().create();
      Settable.ServerInfo si = Settable.getServerInfo();
      final Info info = new Info(si.name, si.description, si.location, si.port, dao.getUserDao().getOnlineUserCount(), si.max);

      return new ResponseEntity(gson.toJson(info), HttpStatus.OK);
  }

  @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json")
  public @ResponseBody ResponseEntity getStatus() {
      return new ResponseEntity("{\"status\":\"OK\"}", HttpStatus.OK);
  }
  
  public class Info {
    public final String name, description, location;
    public final int port, users, max;
    public Info(String name, String description, String location, int port, int users, int max) {
      this.name = name;
      this.description = description;
      this.location = location;
      this.port = port;
      this.users = users;
      this.max = max;
    }
  }
  
  public class AdvInfo {
    public final String name, location, address;
    public final List<String> users, lobbies;
    public AdvInfo(final String name, final String location, final String address, final List users, final List lobbies) {
      this.name = name;
      this.location = location;
      this.address = address;
      this.users = users;
      this.lobbies = lobbies;
    }
  }
}
