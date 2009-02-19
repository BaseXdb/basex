package org.basex.test.w3c;

import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.basex.io.IO;

/**
 * XQuery FullText Test Suite Wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQFTTS extends W3CTS {
   /**
   * Main method of the test class.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQFTTS().init(args);
  }
  
  /**
   * Constructor.
   */
  public XQFTTS() {
    super("XQFTTS");
  }
  
  @Override
  String read(final IO f) throws IOException {
    final StringBuilder sb = new StringBuilder();
    final BufferedReader br = new BufferedReader(new
        InputStreamReader(new FileInputStream(f.path()), UTF8));
    String l;
    while((l = br.readLine()) != null) {
      l = l.trim();
      if(l.length() == 0) continue;
      sb.append(l.indexOf(" />") != -1 ? l.replaceAll(" />", "/>") : l);
      if(!l.endsWith(">")) sb.append(' ');
    }
    br.close();
    return sb.toString().trim();
  }

  @Override
  String norm(final byte[] string) {
    final String str = string(string);
    final StringBuilder sb = new StringBuilder();
    boolean nl = true;
    for(int l = 0; l < str.length(); l++) {
      final char c = str.charAt(l);
      if(nl) {
        nl = c >= 0 && c <= ' ';
      } else {
        nl = c == '\r' || c == '\n';
        if(nl) {
          // delete trailing whitespaces
          while(sb.charAt(sb.length() - 1) <= ' ')
            sb.deleteCharAt(sb.length() - 1);
          if(sb.charAt(sb.length() - 1) != '>') sb.append(' ');
        }
      }
      if(!nl) sb.append(c);
    }
    return sb.toString().trim();
  }
}
