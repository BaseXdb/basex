package org.basex.io;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;

import javax.xml.transform.stream.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a URL.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class IOUrl extends IO {
  /** HTTP header: Authorization. */
  private static final String AUTHORIZATION = "Authorization";
  /** Timeout for connecting to a resource (seconds). */
  private static final int TIMEOUT = 10;

  /**
   * Constructor.
   * @param u url
   */
  public IOUrl(final String u) {
    super(u);
  }

  @Override
  public byte[] read() throws IOException {
    return new BufferInput(this).content();
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(path);
  }

  @Override
  public StreamSource streamSource() {
    return new StreamSource(path);
  }

  @Override
  public InputStream inputStream() throws IOException {
    URLConnection conn = null;
    try {
      conn = connection();
      return conn.getInputStream();
    } catch(final IOException ex) {
      final TokenBuilder msg = new TokenBuilder(Util.message(ex));
      // try to retrieve more information on why the request failed
      if(conn instanceof HttpURLConnection) {
        final InputStream es = ((HttpURLConnection) conn).getErrorStream();
        if(es != null) {
          final byte[] err = new IOStream(es).read();
          if(err.length != 0) msg.add(NL).add(INFORMATION).add(COL).add(NL).add(err);
        }
      }
      final IOException io = new IOException(msg.toString());
      io.setStackTrace(ex.getStackTrace());
      throw io;
    } catch(final RuntimeException ex) {
      // catch unexpected runtime exceptions
      Util.debug(ex);
      throw new BaseXException(NOT_PARSED_X, path);
    }
  }

  /**
   * Returns a connection to the URL.
   * @return connection
   * @throws IOException I/O exception
   */
  public URLConnection connection() throws IOException {
    final URL url = new URL(path);
    final URLConnection conn = url.openConnection();
    conn.setConnectTimeout(TIMEOUT * 1000);
    // use basic authentication if credentials are contained in the url
    final String ui = url.getUserInfo();
    if(ui != null) conn.setRequestProperty(AUTHORIZATION, "Basic " + Base64.encode(ui));
    return conn;
  }

  @Override
  public String dirPath() {
    return path.endsWith("/") ? path : path.substring(0, path.lastIndexOf('/') + 1);
  }

  @Override
  public IO merge(final String f) {
    final IO io = IO.get(f);
    if(!(io instanceof IOFile) || f.contains(":") || f.startsWith("/")) return io;
    return IO.get((path.endsWith("/") ? path : path.replace("^(.*/).*", "$1")) + f);
  }

  /**
   * Checks if the specified uri starts with a valid scheme.
   * @param url url to be converted
   * @return file path
   */
  static boolean isValid(final String url) {
    int u = -1;
    final int us = url.length();
    while(++u < us) {
      final char c = url.charAt(u);
      if(!Token.letterOrDigit(c) && c != '+' && c != '-' && c != '.') break;
    }
    return u > 2 && u + 1 < us && url.charAt(u) == ':' && url.charAt(u + 1) == '/';
  }

  /**
   * Checks if the specified string is a valid file URI.
   * @param s source
   * @return result of check
   */
  public static boolean isFileURL(final String s) {
    return s.startsWith(FILEPREF + '/');
  }
}
