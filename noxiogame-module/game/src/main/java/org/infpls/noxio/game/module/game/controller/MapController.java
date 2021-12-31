package org.infpls.noxio.game.module.game.controller;

import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.infpls.noxio.game.module.game.game.NoxioMap;

@Controller
public class MapController {
    final Gson gson = new GsonBuilder().create();
    
    @RequestMapping(value = "/map/{map}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity getMap(@PathVariable String map) {      
      NoxioMap m = NoxioMap.GET(map);
      if(m == null) {
        return new ResponseEntity(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity(gson.toJson(m), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/maps", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity getMap() {
      final List<NoxioMap> maps = NoxioMap.GET();
      final List<MapInfo> mi = new ArrayList();
      for(int i=0;i<maps.size();i++) {
        final NoxioMap m = maps.get(i);
        mi.add(new MapInfo(m.getFile(), m.getName(), "InfernoPlus"));
      }
      return new ResponseEntity(gson.toJson(mi), HttpStatus.OK);
    }
    
    public class MapInfo {
      public final String id, name, author;
      public MapInfo(String id, String name, String author) {
        this.id = id; this.name = name; this.author = author;
      }
    }
}
