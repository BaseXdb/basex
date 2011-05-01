package org.basex.api.jaxrx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is responsible to offer some utility methods.
 * 
 * @author Lukas Lewandowski, University of Konstanz.
 * 
 */
public final class Util {

  /**
   * Default constructor.
   */
  private Util() {
    // default constructor.
  }

  /**
   * This method returns a {@link String} representation of the Shiro .ini file.
   * @param filename The filename of configuration file.
   * @return A {@link String} representation of the configuration file.
   */
  public static String importShiroConfig(final String filename) {
    String ini = null;
    try {
      final StringBuilder sb = new StringBuilder();
      final BufferedReader br = new BufferedReader(new InputStreamReader(
          Util.class.getResourceAsStream("/" + filename)));
      String line;
      while((line = br.readLine()) != null) {
        sb.append(line + "\n");
      }
      ini = sb.toString();
      br.close();
    } catch(final IOException e) {
      e.printStackTrace();
    }
    return ini;

  }

}
