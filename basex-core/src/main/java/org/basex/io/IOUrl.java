package org.basex.io;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.time.*;
import java.util.*;

import javax.net.ssl.*;
import javax.xml.transform.stream.*;

import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.xml.sax.*;

/**
 * {@link IO} reference, representing a URL.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class IOUrl extends IO {
  /** Reason phrases. */
  private static final HashMap<Integer, String> REASONS = new HashMap<>();
  /** Optional SSL context for ignoring certificates. */
  private static SSLContext ssl;
  /** Cached HTTP client instances. */
  private static final HttpClient[] CLIENTS = new HttpClient[2];

  static {
    REASONS.put(100, "Continue");
    REASONS.put(101, "Switching Protocols");
    REASONS.put(102, "Processing");
    REASONS.put(200, "OK");
    REASONS.put(201, "Created");
    REASONS.put(202, "Accepted");
    REASONS.put(203, "Non-Authoritative Information");
    REASONS.put(204, "No Content");
    REASONS.put(205, "Reset Content");
    REASONS.put(206, "Partial Content");
    REASONS.put(207, "Multi-Status");
    REASONS.put(208, "Already Reported");
    REASONS.put(300, "Multiple Choices");
    REASONS.put(301, "Moved Permanently");
    REASONS.put(302, "Found");
    REASONS.put(303, "See Other");
    REASONS.put(304, "Not Modified");
    REASONS.put(305, "Use Proxy");
    REASONS.put(307, "Temporary Redirect");
    REASONS.put(308, "Permanent Redirect");
    REASONS.put(400, "Bad Request");
    REASONS.put(401, "Unauthorized");
    REASONS.put(402, "Payment Required");
    REASONS.put(403, "Forbidden");
    REASONS.put(404, "Not Found");
    REASONS.put(405, "Method Not Allowed");
    REASONS.put(406, "Not Acceptable");
    REASONS.put(407, "Proxy Authentication Required");
    REASONS.put(408, "Request Timeout");
    REASONS.put(409, "Conflict");
    REASONS.put(410, "Gone");
    REASONS.put(411, "Length Required");
    REASONS.put(412, "Precondition Failed");
    REASONS.put(413, "Payload Too Large");
    REASONS.put(414, "Request-URI Too Long");
    REASONS.put(415, "Unsupported Media Type");
    REASONS.put(416, "Requested Range Not Satisfiable");
    REASONS.put(417, "Expectation Failed");
    REASONS.put(418, "I'm a teapot");
    REASONS.put(420, "Enhance Your Calm");
    REASONS.put(421, "Misdirected Request");
    REASONS.put(422, "Unprocessable Entity");
    REASONS.put(423, "Locked");
    REASONS.put(424, "Failed Dependency");
    REASONS.put(426, "Upgrade Required");
    REASONS.put(428, "Precondition Required");
    REASONS.put(429, "Too Many Requests");
    REASONS.put(431, "Request Header Fields Too Large");
    REASONS.put(444, "Connection Closed Without Response");
    REASONS.put(451, "Unavailable For Legal Reasons");
    REASONS.put(499, "Client Closed Request");
    REASONS.put(500, "Internal Server Error");
    REASONS.put(501, "Not Implemented");
    REASONS.put(502, "Bad Gateway");
    REASONS.put(503, "Service Unavailable");
    REASONS.put(504, "Gateway Timeout");
    REASONS.put(505, "HTTP Version Not Supported");
    REASONS.put(506, "Variant Also Negotiates");
    REASONS.put(507, "Insufficient Storage");
    REASONS.put(508, "Loop Detected");
    REASONS.put(510, "Not Extended");
    REASONS.put(511, "Network Authentication Required");
    REASONS.put(599, "Network Connect Timeout Error");
  }

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
    return isJarURL(pth) ? new URL(pth).openStream() : response().body();
  }


  /**
   * Returns an HTTP response.
   * @return response
   * @throws IOException I/O exception
   */
  public HttpResponse<InputStream> response() throws IOException {
    final HttpClient client = client(true);

    final HttpResponse<InputStream> response;
    try {
      final URI uri = new URI(pth);
      final HttpRequest.Builder rb = HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(1));
      rb.header(HTTPText.ACCEPT, MediaType.ALL_ALL.toString());
      new UserInfo(uri).basic(rb);
      response = client.send(rb.build(), HttpResponse.BodyHandlers.ofInputStream());
    } catch(final IOException ex) {
      throw ex;
    } catch(final Exception ex) {
      /* possible exceptions, among others:
       * - construct URI: invalid argument
       * - build request: invalid URI scheme
       * - send request: interrupted requests */
      Util.debug(ex);
      throw new IOException(ex.getMessage());
    }

    // create exception if response was not successful
    final int status = response.statusCode();
    if(status >= 400) {
      final StringBuilder sb = new StringBuilder().append(status);
      final String reason = reason(status);
      if(!reason.isEmpty()) sb.append(": ").append(reason);
      throw new IOException(sb.toString());
    }
    return response;
  }

  /**
   * Returns a singleton HTTP client instance.
   * @param redirect follow redirects
   * @return client builder
   */
  public static HttpClient client(final boolean redirect) {
    final int i = redirect ? 1 : 0;
    if(CLIENTS[i] == null) {
      final HttpClient.Builder cb = HttpClient.newBuilder();
      if(ssl != null) cb.sslContext(ssl).connectTimeout(Duration.ofMinutes(1));
      CLIENTS[i] = cb.followRedirects(redirect ? Redirect.ALWAYS : Redirect.NEVER).build();
    }
    return CLIENTS[i];
  }

  /**
   * Returns the reason phrase for a status code.
   * @param status HTTP status code
   * @return reason or empty string
   */
  public static String reason(final int status) {
    final String reason = REASONS.get(status);
    return reason != null ? reason : "";
  }

  /**
   * Checks if the specified string is a valid URL.
   * @param url url string
   * @return file path
   */
  static boolean isValid(final String url) {
    final int ul = url.length();
    int u = url.indexOf(':');
    if(u < 2 || u + 1 == ul || !isJarURL(url) && url.charAt(u + 1) != '/') return false;
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
   * Checks if the specified string is a jar URL.
   * @param url url to be tested
   * @return result of check
   */
  static boolean isJarURL(final String url) {
    return url.startsWith(JARPREF);
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
