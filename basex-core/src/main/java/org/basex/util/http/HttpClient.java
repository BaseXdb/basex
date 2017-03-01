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
 * @author BaseX Team 2005-17, BSD License
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
  public BasicIter<Item> sendRequest(final byte[] href, final ANode request, final Iter bodies)
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

      if(!req.payload.isEmpty() || !req.parts.isEmpty()) {
        setContentType(conn, req);
        writePayload(conn.getOutputStream(), req);
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
                + OPAQUE + "=\"" + map.get(OPAQUE) + '"';

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
        conn.setRequestMethod(method);
      } catch(final ProtocolException ex) {
        try {
          // set field via reflection to circumvent string check
          Class<?> c = conn.getClass();
          while(c != HttpURLConnection.class) c = c.getSuperclass();
          final Field f = c.getDeclaredField("method");
          f.setAccessible(true);
          f.set(conn, method);
        } catch(final Throwable th) {
          // dump new exception, return original exception
          Util.debug(th);
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
        ct = new TokenBuilder().add(ct).add("; ").add(BOUNDARY).add('=').
            add(request.boundary()).toString();
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
        out.write(new TokenBuilder().add("--").add(boundary).add(CRLF).finish());

        // write headers
        for(final Entry<String, String> header : part.headers.entrySet())
          writeHeader(header.getKey(), header.getValue(), out);
        if(!part.headers.containsKey(CONTENT_TYPE))
          writeHeader(CONTENT_TYPE, part.bodyAtts.get(SerializerOptions.MEDIA_TYPE.name()), out);

        // choose Base64 if content includes non-ASCII characters
        byte[] contents = ao.finish();
        boolean base64 = false;
        for(final byte b : contents) base64 |= b < 0;

        // write content
        if(base64) {
          writeHeader(CONTENT_TRANSFER_ENCODING, BASE64, out);
          out.write(CRLF);
          contents = org.basex.util.Base64.encode(contents);
          final int bl = contents.length;
          for(int b = 0; b < bl; b += 76) {
            out.write(contents, b, Math.min(76, bl - b));
            out.write(CRLF);
          }
        } else {
          out.write(CRLF);
          out.write(contents);
        }
        out.write(CRLF);
      }
      out.write(new TokenBuilder("--").add(boundary).add("--").add(CRLF).finish());
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
  public static void writeHeader(final String key, final String value, final OutputStream out)
      throws IOException {
    out.write(new TokenBuilder().add(key).add(": ").add(value).add(CRLF).finish());
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
    sopts.set(SerializerOptions.NEWLINE, Newline.NL);

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
      try(Serializer ser = Serializer.get(out, sopts)) {
        for(final Item it : payload) ser.serialize(it);
      }
    }
  }
}
