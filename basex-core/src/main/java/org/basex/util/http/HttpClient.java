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
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.HttpRequest.*;
import org.basex.util.options.Options.*;

/**
 * HTTP Client.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Rositsa Shadura
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
   * @param bodies content items
   * @return HTTP response
   * @throws QueryException query exception
   */
  public ValueIter sendRequest(final byte[] href, final ANode request, final Iter bodies)
      throws QueryException {

    final HttpRequest req = new HttpRequestParser(info).parse(request, bodies);
    HttpURLConnection conn = null;
    try {
      // parse request data, set properties
      final String mediaType = req.attribute(OVERRIDE_MEDIA_TYPE);
      final String status = req.attribute(STATUS_ONLY);
      final boolean body = status == null || !Strings.yes(status);
      final String url = href == null || href.length == 0 ? req.attribute(HREF) : string(href);

      if(url == null || url.isEmpty()) throw HC_URL.get(info);
      conn = connect(url, req);

      if(!req.bodyContent.isEmpty() || !req.parts.isEmpty()) {
        setContentType(conn, req);
        setRequestContent(conn.getOutputStream(), req);
      }

      return new HttpResponse(info, options).getResponse(conn, body, mediaType).iter();

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
   * @return HTTP connection
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   * @throws MalformedURLException incorrect url
   */
  private HttpURLConnection connect(final String url, final HttpRequest request)
      throws QueryException, IOException {

    HttpURLConnection conn = connection(url, request);
    final String user = request.attribute(USERNAME), pass = request.attribute(PASSWORD);
    if(user != null) {
      if(request.authMethod == AuthMethod.BASIC) {
        conn.setRequestProperty(AUTHORIZATION, BASIC + ' ' +
            org.basex.util.Base64.encode(user + ':' + pass));

      } else if(request.authMethod == AuthMethod.DIGEST) {
        conn.setRequestProperty(AUTHORIZATION, DIGEST);

        final EnumMap<Request, String> map = digestHeaders(conn.getHeaderField(WWW_AUTHENTICATE));
        final String
          realm = map.get(REALM),
          nonce = map.get(NONCE),
          uri = conn.getURL().getPath(),
          qop = map.get(QOP),
          nc = "00000001",
          cnonce = Strings.md5(Long.toString(System.nanoTime())),
          ha1 = Strings.md5(user + ':' + realm + ':' + pass),
          ha2 = Strings.md5(request.attribute(METHOD) + ':' + uri),
          rsp = Strings.md5(ha1 + ':' + nonce + ':' + nc + ':' + cnonce + ':' + qop + ':' + ha2),
          creds = USERNAME + "=\"" + user + "\","
                + REALM + "=\"" + realm + "\","
                + NONCE + "=\"" + nonce + "\","
                + URI + "=\"" + uri + "\","
                + QOP + '=' + qop + ','
                + NC + '=' + nc + ','
                + CNONCE + "=\"" + cnonce + "\","
                + RESPONSE + "=\"" + rsp + "\","
                + ALGORITHM + '=' + MD5 + ','
                + OPAQUE + "=\"" + map.get(OPAQUE) + "\"";

        conn.disconnect();
        conn = connection(url, request);
        conn.setRequestProperty(AUTHORIZATION, DIGEST + ' ' + creds);
      }
    }
    return conn;
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

    final HttpURLConnection conn = (HttpURLConnection) uc;
    final String method = request.attribute(METHOD);
    if(method != null) {
      try {
        // set field via reflection to circumvent string check
        final Field f = conn.getClass().getSuperclass().getDeclaredField("method");
        f.setAccessible(true);
        f.set(conn, method);
      } catch(final Throwable th) {
        Util.debug(th);
        conn.setRequestMethod(method);
      }
      conn.setDoOutput(true);

      final String timeout = request.attribute(TIMEOUT);
      if(timeout != null) conn.setConnectTimeout(Strings.toInt(timeout) * 1000);
      final String redirect = request.attribute(FOLLOW_REDIRECT);
      if(redirect != null) setFollowRedirects(Strings.yes(redirect));

      for(final Entry<String, String> header : request.headers.entrySet()) {
        conn.addRequestProperty(header.getKey(), header.getValue());
      }
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
      if(request.isMultipart) {
        final String b = request.payloadAtts.get(BOUNDARY);
        ct = new TokenBuilder().add(ct).add("; ").add(BOUNDARY).add('=').
            add(b.isEmpty() ? DEFAULT_BOUNDARY : b).toString();
      }
    }
    conn.setRequestProperty(CONTENT_TYPE, ct);
  }

  /**
   * Parses the header for digest authentication.
   * @param auth authorization string
   * @return values values
   */
  public static EnumMap<Request, String> digestHeaders(final String auth) {
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
   * Set HTTP request content.
   * @param out output stream
   * @param request request data
   * @throws IOException I/O exception
   */
  public static void setRequestContent(final OutputStream out, final HttpRequest request)
      throws IOException {

    if(request.isMultipart) {
      writeMultipart(request, out);
    } else {
      writePayload(request.bodyContent, request.payloadAtts, out);
    }
    out.close();
  }

  /**
   * Writes the payload of a body or part in the output stream of the connection.
   * @param payload body/part payload
   * @param atts payload attributes
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writePayload(final ItemList payload, final HashMap<String, String> atts,
      final OutputStream out) throws IOException {

    // choose serialization parameters
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.INDENT, YesNo.NO);

    String src = null, method = null;
    for(final Entry<String, String> entry : atts.entrySet()) {
      final String key = entry.getKey(), value = entry.getValue();
      if(key.equals(SRC)) {
        src = value;
      } else if(key.equals(SerializerOptions.METHOD.name())) {
        method = value.equals(BINARY) ? SerialMethod.BASEX.toString() : value;
      } else {
        sopts.assign(key, value);
        // no method specified (yet): choose method based on media type
        if(method == null && key.equals(SerializerOptions.MEDIA_TYPE.name())) {
          final MediaType type = new MediaType(value);
          if(type.is(MediaType.APPLICATION_HTML_XML)) {
            method = SerialMethod.XHTML.toString();
          } else if(type.is(MediaType.TEXT_HTML)) {
            method = SerialMethod.HTML.toString();
          } else if(type.isXML()) {
            method = SerialMethod.XML.toString();
          } else if(type.isText()) {
            method = SerialMethod.TEXT.toString();
          } else {
            method = SerialMethod.BASEX.toString();
          }
        }
      }
    }
    sopts.assign(SerializerOptions.METHOD.name(), method);

    // serialize payload
    if(src != null) {
      out.write(IO.get(src).read());
    } else {
      try(final Serializer ser = Serializer.get(out, sopts)) {
        for(final Item it : payload) ser.serialize(it);
      }
    }
  }

  /**
   * Writes parts of multipart message in the output stream of the HTTP
   * connection.
   * @param request request data
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writeMultipart(final HttpRequest request, final OutputStream out)
      throws IOException {
    final String boundary = request.payloadAtts.get(BOUNDARY);
    for(final Part part : request.parts) writePart(part, out, boundary);
    out.write(new TokenBuilder("--").add(boundary).add("--").add(CRLF).finish());
  }

  /**
   * Writes a single part of a multipart message.
   * @param part part
   * @param out connection output stream
   * @param boundary boundary
   * @throws IOException I/O exception
   */
  private static void writePart(final Part part, final OutputStream out, final String boundary)
      throws IOException {

    // write boundary preceded by "--"
    final TokenBuilder boundTb = new TokenBuilder();
    boundTb.add("--").add(boundary).add(CRLF);
    out.write(boundTb.finish());

    final HashMap<String, String> bodyAtts = part.bodyAtts;
    final String mt = bodyAtts.get(SerializerOptions.MEDIA_TYPE.name());
    if(!part.headers.containsKey(CONTENT_TYPE)) part.headers.put(CONTENT_TYPE, mt);

    // write headers
    for(final Entry<String, String> header : part.headers.entrySet()) {
      final TokenBuilder hdrTb = new TokenBuilder();
      hdrTb.add(header.getKey()).add(": ").add(header.getValue()).add(CRLF);
      out.write(hdrTb.finish());
    }
    out.write(CRLF);

    // write content
    writePayload(part.bodyContent, bodyAtts, out);
    out.write(CRLF);
  }
}
