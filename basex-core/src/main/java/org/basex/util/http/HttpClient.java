package org.basex.util.http;

import static java.net.HttpURLConnection.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;
import static org.basex.util.http.HttpText.Request.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.io.serial.SerializerOptions.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.Base64;
import org.basex.util.http.HttpRequest.*;
import org.basex.util.options.Options.*;

/**
 * HTTP Client.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 * @author Michael Seiferle
 */
public final class HttpClient {
  /** Input information. */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;

  /**
   * Constructor.
   * @param info input info
   * @param options database options
   */
  public HttpClient(final InputInfo info, final MainOptions options) {
    this.info = info;
    this.options = options;
  }

  /**
   * Sends an HTTP request and returns the response.
   * @param href URL to send the request to
   * @param request request data
   * @param bodies request body
   * @return HTTP response
   * @throws QueryException query exception
   */
  public Value sendRequest(final byte[] href, final ANode request, final Value bodies)
      throws QueryException {

    final HttpRequest req = new HttpRequestParser(info).parse(request, bodies);
    HttpURLConnection conn = null;
    try {
      // parse request data, set properties
      final String mediaType = req.attribute(OVERRIDE_MEDIA_TYPE);
      final String status = req.attribute(STATUS_ONLY);
      final String sendAuth = req.attribute(SEND_AUTHORIZATION);

      final boolean body = status == null || !Strings.toBoolean(status);
      final boolean challenge = sendAuth == null || !Strings.toBoolean(sendAuth);
      final String url = href == null || href.length == 0 ? req.attribute(HREF) : string(href);

      if(url == null || url.isEmpty()) throw HC_URL.get(info);
      conn = connect(url, req, challenge);

      if(!req.payload.isEmpty() || !req.parts.isEmpty()) {
        setContentType(conn, req);
        writePayload(conn.getOutputStream(), req);
      }

      return new HttpResponse(info, options).getResponse(conn, body, mediaType);

    } catch(final IOException ex) {
      throw HC_ERROR_X.get(info, ex);
    } finally {
      if(conn != null) conn.disconnect();
    }
  }

  /**
   * Opens an HTTP connection.
   * @param url HTTP URL to open connection to
   * @param request request
   * @param challenge send challenge
   * @return HTTP connection
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   * @throws MalformedURLException incorrect url
   */
  private HttpURLConnection connect(final String url, final HttpRequest request,
      final boolean challenge) throws QueryException, IOException {

    // create connection, check if authentication data was supplied
    HttpURLConnection conn = connection(url, request);
    final String user = request.attribute(USERNAME), pass = request.attribute(PASSWORD);
    if(user == null) return conn;

    // Basic authentication
    final AuthMethod am = request.authMethod;
    if(am == AuthMethod.BASIC) {
      if(challenge) {
        // send challenge, create new connection
        if(challenge(conn, am) == null) return conn;
        conn = connection(url, request);
      }
      // send credentials
      conn.setRequestProperty(AUTHORIZATION, am + " " + Base64.encode(user + ':' + pass));

    } else if(am == AuthMethod.DIGEST) {
      final EnumMap<Request, String> map = challenge(conn, am);
      if(map == null) return conn;

      // generate authorization string
      final String
        realm = map.get(REALM),
        nonce = map.get(NONCE),
        uri = conn.getURL().getPath(),
        qop = map.get(QOP),
        nc = "00000001",
        cnonce = Strings.md5(Long.toString(System.nanoTime())),
        ha1 = Strings.md5(user + ':' + realm + ':' + pass),
        ha2 = Strings.md5(request.attribute(METHOD) + ':' + uri),
        rsp = Strings.md5(ha1 + ':' + nonce + ':' + nc + ':' + cnonce + ':' + qop + ':' + ha2);

      conn = connection(url, request);
      conn.setRequestProperty(AUTHORIZATION, am + " " +
        USERNAME + "=\"" + user + "\","
        + REALM + "=\"" + realm + "\","
        + NONCE + "=\"" + nonce + "\","
        + URI + "=\"" + uri + "\","
        + QOP + '=' + qop + ','
        + NC + '=' + nc + ','
        + CNONCE + "=\"" + cnonce + "\","
        + RESPONSE + "=\"" + rsp + "\","
        + ALGORITHM + '=' + MD5 + ','
        + OPAQUE + "=\"" + map.get(OPAQUE) + '"');
    }
    return conn;
  }

  /**
   * Sends a challenge.
   * @param conn HTTP connection
   * @param am authentication method
   * @return authentication data
   * @throws IOException I/O exception
   */
  private static EnumMap<Request, String> challenge(final HttpURLConnection conn,
      final AuthMethod am) throws IOException {

    // skip authentication if server needs no credentials or raises an error
    conn.setRequestProperty(AUTHORIZATION, am.toString());
    if(conn.getResponseCode() != 401) return null;

    // skip authentication if authentication method differs
    final EnumMap<Request, String> map = authHeaders(conn.getHeaderField(WWW_AUTHENTICATE));
    if(!am.toString().equals(map.get(AUTH_METHOD))) return null;

    // disconnect, return map
    conn.disconnect();
    return map;
  }

  /**
   * Returns a new HTTP connection.
   * @param url HTTP URL to open connection to
   * @param request request
   * @return HTTP connection
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   * @throws MalformedURLException incorrect url
   */
  private HttpURLConnection connection(final String url, final HttpRequest request)
      throws QueryException, IOException {

    final URLConnection uc = new IOUrl(url).connection();
    if(!(uc instanceof HttpURLConnection)) throw HC_ERROR_X.get(info, "Invalid URL: " + url);

    HttpURLConnection conn = (HttpURLConnection) uc;
    final String method = request.attribute(METHOD);
    if(method != null) {
      try {
        conn.setRequestMethod(method);
      } catch(final ProtocolException ex) {
        // method is not supported: try to inject method to circumvent check
        try {
          Class<?> clzz = conn.getClass();
          try {
            // implementation with delegator (sun.net.www.protocol.https.HttpsURLConnectionImpl)
            // get actual connection
            final Field f = clzz.getDeclaredField("delegate");
            f.setAccessible(true);
            conn = (HttpURLConnection) f.get(conn);
            clzz = conn.getClass();
          } catch(final Throwable e) {
            // ignore error: dump exception if debug is enabled
            Util.debug(e);
          }

          // assign request method
          while(clzz != HttpURLConnection.class) clzz = clzz.getSuperclass();
          final Field f = clzz.getDeclaredField("method");
          f.setAccessible(true);
          f.set(conn, method);
        } catch(final Throwable e) {
          // ignore error: dump exception if debug is enabled, return original exception
          Util.debug(e);
          throw ex;
        }
      }
      conn.setDoOutput(true);

      final String timeout = request.attribute(TIMEOUT);
      if(timeout != null) {
        // timeouts may occur while waiting for the connection or the response
        conn.setConnectTimeout(Strings.toInt(timeout) * 1000);
        conn.setReadTimeout(Strings.toInt(timeout) * 1000);
      }
      final String redirect = request.attribute(FOLLOW_REDIRECT);
      if(redirect != null) setFollowRedirects(Strings.toBoolean(redirect));

      request.headers.forEach(conn::addRequestProperty);
    }
    return conn;
  }

  /**
   * Sets the content type of the HTTP request.
   * @param conn HTTP connection
   * @param request request data
   */
  private static void setContentType(final HttpURLConnection conn, final HttpRequest request) {
    String ct;
    final String contType = request.headers.get(CONTENT_TYPE.toLowerCase(Locale.ENGLISH));
    if(contType != null) {
      // if content type is set in the header, its value is used
      ct = contType;
    } else {
      // otherwise @media-type of <http:body/> is considered
      ct = request.payloadAtts.get(SerializerOptions.MEDIA_TYPE.name());
      if(request.isMultipart) ct = Strings.concat(ct, "; ", BOUNDARY, "=", request.boundary());
    }
    conn.setRequestProperty(CONTENT_TYPE, ct);
  }

  /**
   * Returns the authentication headers.
   * @param auth authorization string
   * @return values values
   */
  public static EnumMap<Request, String> authHeaders(final String auth) {
    final EnumMap<Request, String> values = new EnumMap<>(Request.class);
    if(auth != null) {
      final String[] parts = Strings.split(auth, ' ', 2);
      values.put(AUTH_METHOD, parts[0]);
      if(parts.length > 1) {
        for(final String header : Strings.split(parts[1], ',')) {
          final String[] kv = Strings.split(header, '=', 2);
          final String key = kv[0].trim();
          if(!key.isEmpty() && kv.length == 2) {
            final Request r = Request.get(key);
            if(r != null) values.put(r, Strings.delete(kv[1], '"').trim());
          }
        }
      }
    }
    return values;
  }

  /**
   * Writes the HTTP request payload.
   * @param out output stream
   * @param request request data
   * @throws IOException I/O exception
   */
  public static void writePayload(final OutputStream out, final HttpRequest request)
      throws IOException {

    if(request.isMultipart) {
      final String boundary = request.boundary();
      for(final Part part : request.parts) {
        // write content to cache
        final ArrayOutput ao = new ArrayOutput();
        writePayload(part.bodyContents, part.bodyAtts, ao);

        // write boundary preceded by "--"
        out.write(concat("--", boundary, CRLF));

        // write headers
        for(final Entry<String, String> header : part.headers.entrySet())
          writeHeader(header.getKey(), header.getValue(), out);
        if(!part.headers.containsKey(CONTENT_TYPE))
          writeHeader(CONTENT_TYPE, part.bodyAtts.get(SerializerOptions.MEDIA_TYPE.name()), out);

        out.write(CRLF);
        out.write(ao.finish());
        out.write(CRLF);
      }
      out.write(concat("--", boundary, "--", CRLF));
    } else {
      writePayload(request.payload, request.payloadAtts, out);
    }
    out.close();
  }

  /**
   * Writes a single header.
   * @param key key
   * @param value value
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writeHeader(final String key, final String value, final OutputStream out)
      throws IOException {
    out.write(concat(key, ": ", value, CRLF));
  }

  /**
   * Writes the payload of a body or part in the output stream of the connection.
   * @param payload body/part payload
   * @param atts payload attributes
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writePayload(final ItemList payload, final Map<String, String> atts,
      final OutputStream out) throws IOException {

    // choose serialization parameters
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.INDENT, YesNo.NO);
    sopts.set(SerializerOptions.NEWLINE, Newline.NL);

    String method = null, type = null;
    for(final Entry<String, String> entry : atts.entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();

      // send specified source
      if(key.equals(SRC)) {
        out.write(IO.get(value).read());
        return;
      }

      // serialization parameters
      if(key.equals(SerializerOptions.METHOD.name())) {
        method = value.equals(BINARY) ? SerialMethod.BASEX.toString() : value;
      } else {
        sopts.assign(key, value);
        if(key.equals(SerializerOptions.MEDIA_TYPE.name())) type = value;
      }
    }

    // no method specified (yet): choose method based on media type
    if(method == null && type != null) {
      final MediaType mt = new MediaType(type);
      if(mt.is(MediaType.APPLICATION_HTML_XML)) {
        method = SerialMethod.XHTML.toString();
      } else if(mt.is(MediaType.TEXT_HTML)) {
        method = SerialMethod.HTML.toString();
      } else if(mt.isXML()) {
        method = SerialMethod.XML.toString();
      } else if(mt.isText()) {
        method = SerialMethod.TEXT.toString();
      }
    }
    // no method, EXPath binary method: use default serialization, atomize nodes
    final boolean atom = method == null || method.equals(BINARY);
    if(atom) method = SerialMethod.BASEX.toString();
    sopts.assign(SerializerOptions.METHOD.name(), method);

    // serialize payload
    try(Serializer ser = Serializer.get(out, sopts)) {
      for(final Item item : payload) {
        ser.serialize(atom && item.type instanceof NodeType ? ((ANode) item).atomItem() : item);
      }
    }
  }
}
