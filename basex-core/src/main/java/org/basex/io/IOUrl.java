package org.basex.io;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpClient.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.time.*;

import javax.net.ssl.*;
import javax.xml.transform.stream.*;

import org.basex.core.StaticOptions.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a URL.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class IOUrl extends IO {
  /** Optional SSL context for ignoring certificates. */
  private static SSLContext ssl;

  /**
   * Constructor.
   * @param url url
   */
  public IOUrl(final String url) {
    super(normalize(url));
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
    return response(HttpResponse.BodyHandlers.ofInputStream()).body();
  }

  /**
   * Returns an HTTP response.
   * @param <T> response type
   * @param handler body handler
   * @return response
   * @throws IOException I/O exception
   */
  public <T> HttpResponse<T> response(final BodyHandler<T> handler) throws IOException {
    try {
      final URI uri = new URI(pth);
      final HttpRequest.Builder hb = HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(1));
      // credentials in the url: use basic authentication
      final String ui = uri.getUserInfo();
      if(ui != null) hb.header(HttpText.AUTHORIZATION, AuthMethod.BASIC + " " + Base64.encode(ui));
      final HttpClient.Builder cb = HttpClient.newBuilder();
      if(ssl != null) cb.sslContext(ssl);
      return cb.followRedirects(Redirect.ALWAYS).build().send(hb.build(), handler);
    } catch(final IOException ex) {
      throw ex;
    } catch(final Exception  ex) {
      Util.debug(ex);
      throw new IOException(ex.getMessage());
    }
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
   * Normalizes the specified URI and creates a path URI.
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
   * Ignore SSL certificates.
   */
  public static void ignoreCertificates() {
    System.getProperties().setProperty("jdk.internal.httpclient.disableHostnameVerification",
        Boolean.TRUE.toString());

    try {
      ssl = SSLContext.getInstance("TLS");
      ssl.init(null, new TrustManager[] {
        new X509TrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() { return null; }
          @Override
          public void checkClientTrusted(final X509Certificate[] x509, final String type) { }
          @Override
          public void checkServerTrusted(final X509Certificate[] x509, final String type) { }
        }
      }, new SecureRandom());
    } catch(NoSuchAlgorithmException | KeyManagementException ex) {
      Util.stack(ex);
    }
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns a normalized URL.
   * @param string input URL
   * @return path
   */
  private static String normalize(final String string) {
    int i = string.indexOf("://") + 3;
    if(i == 2) return string;

    final String scheme = string.substring(0, i);
    String path = string.substring(i), query = "", anchor = "";
    i = path.indexOf('#');
    if(i != -1) {
      anchor = path.substring(i);
      path = path.substring(0, i);
    }
    i = path.indexOf('?');
    if(i != -1) {
      query = path.substring(i);
      path = path.substring(0, i);
    }

    final StringList segments = new StringList(Strings.split(path, '/'));
    for(int s = 1; s < segments.size(); s++) {
      final String segment = segments.get(s);
      if(segment.equals(".")) {
        segments.remove(s--);
      } else if(segment.equals("..") && s > 1) {
        segments.remove(s--);
        segments.remove(s--);
      }
    }
    return scheme + String.join("/", segments.finish()) + query + anchor;
  }
}
