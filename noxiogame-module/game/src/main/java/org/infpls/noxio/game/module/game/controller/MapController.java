package org.infpls.noxio.game.module.game.controller;

import com.google.gson.*;
import java.io.IOException;
import java.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.infpls.noxio.game.module.game.game.NoxioMap;
import org.infpls.noxio.game.module.game.util.Oak;

@Controller
public class MapController {
    final private List<NoxioMap> cache = new ArrayList();
  
    final Gson gson = new GsonBuilder().create();
    
    @RequestMapping(value = "/map/{map}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity getStatus(@PathVariable String map) {      
      NoxioMap m = null;
      for(int i=0;i<cache.size();i++) {
        final NoxioMap c = cache.get(i);
        if(c.getFile().equals(map)) {
          m = c; break;
        }
      }
      if(m == null) {
        try {
          m = new NoxioMap(map);
          cache.add(m);
        }
        catch(IOException ex) {
          Oak.log(Oak.Type.SESSION, Oak.Level.ERR, "Error parsing map file: " + map + " FIELD_1", ex);
          return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
      }      
      return new ResponseEntity(gson.toJson(m), HttpStatus.OK);
    }
}
