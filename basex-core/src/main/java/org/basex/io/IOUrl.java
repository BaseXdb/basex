package org.basex.io;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;
import javax.xml.transform.stream.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a URL.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IOUrl extends IO {
  /** Timeout for connecting to a resource (seconds). */
  private static final int TIMEOUT = 10;

  /**
   * Constructor.
   * @param url url
   */
  public IOUrl(final String url) {
    super(url);
  }

  @Override
  public byte[] read() throws IOException {
    return BufferInput.get(this).content();
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(pth);
  }

  @Override
  public StreamSource streamSource() {
    return new StreamSource(pth);
  }

  @Override
  public InputStream inputStream() throws IOException {
    URLConnection conn = null;
    try {
      conn = connection();
      return conn.getInputStream();
    } catch(final IOException ex) {
      final TokenBuilder msg = new TokenBuilder().add(ex);
      // try to retrieve more information on why the request failed
      if(conn instanceof HttpURLConnection) {
        final InputStream es = ((HttpURLConnection) conn).getErrorStream();
        if(es != null) {
          final byte[] error = new IOStream(es).read();
          if(error.length != 0) msg.add(NL).add(INFORMATION).add(COL).add(NL).add(error);
        }
      }
      final IOException io = new IOException(msg.toString());
      io.setStackTrace(ex.getStackTrace());
      throw io;
    } catch(final RuntimeException ex) {
      // catch unexpected runtime exceptions
      Util.debug(ex);
      throw new BaseXException(NOT_PARSED_X, pth);
    }
  }

  /**
   * Returns a connection to the URL.
   * @return connection
   * @throws IOException I/O exception
   */
  public URLConnection connection() throws IOException {
    final URL url = new URL(pth);
    final URLConnection conn = url.openConnection();
    conn.setConnectTimeout(TIMEOUT * 1000);
    // use basic authentication if credentials are contained in the url
    final String ui = url.getUserInfo();
    if(ui != null) conn.setRequestProperty(HttpText.AUTHORIZATION,
        AuthMethod.BASIC + " " + Base64.encode(ui));
    return conn;
  }

  @Override
  public boolean isDir() {
    return Strings.endsWith(pth, '/');
  }

  /**
   * Checks if the specified string is a valid URL.
   * @param url url string
   * @return file path
   */
  static boolean isValid(final String url) {
    final int ul = url.length();
    int u = url.indexOf(':');
    if(u < 2 || u + 1 == ul || url.charAt(u + 1) != '/') return false;
    while(--u >= 0) {
      final char c = url.charAt(u);
      if(!(c >= 'a' && c <= 'z' || c == '+' || c == '-' || c == '.' || c == '_')) return false;
    }
    return true;
  }

  /**
   * Normalizes the specified URI and creates a new instance of this class.
   * @param uri uri to be converted
   * @return file path
   */
  static String toFile(final String uri) {
    try {
      final String path = Paths.get(new URI(uri)).toString();
      return Strings.endsWith(uri, '/') || Strings.endsWith(uri, '\\') ?
        path + File.separator : path;
    } catch(final Exception ex) {
      Util.errln(ex);
      return uri;
    }
  }

  /**
   * Checks if the specified string is a valid file URL.
   * @param url url to be tested
   * @return result of check
   */
  static boolean isFileURL(final String url) {
    return url.startsWith(FILEPREF);
  }

  /**
   * Ignore Hostname verification.
   */
  public static void ignoreHostname() {
    // http://www.rgagnon.com/javadetails/java-fix-certificate-problem-in-HTTPS.html
    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
  }

  /**
   * Ignore certificates.
   */
  public static void ignoreCert() {
    ignoreHostname();

    final TrustManager[] tm = { new X509TrustManager() {
      @Override
      public X509Certificate[] getAcceptedIssuers() { return null; }
      @Override
      public void checkClientTrusted(final X509Certificate[] certs, final String authType) { }
      @Override
      public void checkServerTrusted(final X509Certificate[] certs, final String authType) { }
    }};
    try {
      final SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, tm, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch(final Exception ex) {
      Util.errln(ex);
    }
  }
}
