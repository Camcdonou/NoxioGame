package org.infpls.noxio.game.module.game.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class Scung {
  
  public static boolean exists(final String path) {
    return new ClassPathResource(path).exists();
  }
  
  /* Reads a file from the classpath and return as a string. Murders new lines */
  public static String readFile(final String path) throws IOException {
    final Resource resource = new ClassPathResource(path);
    return readFile(resource);
  }
  
  public static String readFile(final Resource res) throws IOException {
    final InputStream in = res.getInputStream();
    final BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    final StringBuilder sb = new StringBuilder();
    String line;
    while((line=br.readLine()) != null) {
       sb.append(line);
    }
    br.close();
    in.close();
    return sb.toString();
  }
}
