package org.infpls.noxio.game.module.game.websocket;


import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.infpls.noxio.game.module.game.dao.server.InfoDao;

@Controller
public class GameStatusController {
  
    @Autowired
    private InfoDao infoDao;
    
    @RequestMapping(value = "/advinfo", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity getAdvInfo() {
        Gson gson = new GsonBuilder().create();
        return new ResponseEntity(gson.toJson(infoDao.getAdvInfo()), HttpStatus.OK);
    }
  
    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity getInfo() {
        Gson gson = new GsonBuilder().create();
        return new ResponseEntity(gson.toJson(infoDao.getServerInfo()), HttpStatus.OK);
    }
  
    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity getStatus() {
        return new ResponseEntity("{\"status\":\"OK\"}", HttpStatus.OK);
    }
}
