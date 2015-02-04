package org.basex.query.func.http;

import static java.net.HttpURLConnection.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.http.HttpText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.http.HttpRequest.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * HTTP Client.
 *
 * @author BaseX Team 2005-15, BSD License
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
  Iter sendRequest(final byte[] href, final ANode request, final ValueBuilder bodies)
      throws QueryException {

    HttpURLConnection conn = null;
    try {
      byte[] mediaType = null;
      HttpRequest req = null;
      boolean body = true;
      byte[] url = href;

      // parse request data, set properties
      if(request != null) {
        req = new HttpRequestParser(info).parse(request, bodies);
        mediaType = req.attrs.get(OVERRIDE_MEDIA_TYPE);
        final byte[] status = req.attrs.get(STATUS_ONLY);
        if(status != null && Bln.parse(status, info)) body = false;
        if(url == null) url = req.attrs.get(HREF);
      }

      if(url == null || url.length == 0) throw HC_URL.get(info);
      conn = connect(string(url), req);
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
   * @param request request (can be {@code null})
   * @return HTTP connection
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   * @throws MalformedURLException incorrect url
   */
  private HttpURLConnection connect(final String url, final HttpRequest request)
      throws QueryException, IOException {

    final HttpURLConnection conn = connection(url, request);
    if(request != null) {
      // HTTP Basic Authentication
      final byte[] sendAuth = request.attrs.get(SEND_AUTHORIZATION);
      if(sendAuth != null && Bln.parse(sendAuth, info)) {
        final String user = string(request.attrs.get(USERNAME));
        final String pass = string(request.attrs.get(PASSWORD));
        conn.setRequestProperty(AUTHORIZATION,
            BASIC + ' ' + org.basex.util.Base64.encode(user + ':' + pass));
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
    if(request != null) {
      final String method = string(request.attrs.get(METHOD)).toUpperCase(Locale.ENGLISH);
      final HTTPMethod mth = HTTPMethod.get(method);
      if(mth == HTTPMethod.POST || mth == HTTPMethod.PUT) conn.setDoOutput(true);

      try {
        // set field via reflection to circumvent string check
        final Field f = conn.getClass().getSuperclass().getDeclaredField("method");
        f.setAccessible(true);
        f.set(conn, method);
      } catch(final Throwable th) {
        Util.debug(th);
        conn.setRequestMethod(method);
      }

      final byte[] timeout = request.attrs.get(TIMEOUT);
      if(timeout != null) conn.setConnectTimeout(Integer.parseInt(string(timeout)));
      final byte[] redirect = request.attrs.get(FOLLOW_REDIRECT);
      if(redirect != null) setFollowRedirects(Bln.parse(redirect, info));

      for(final byte[] headers : request.headers) {
        conn.addRequestProperty(string(headers), string(request.headers.get(headers)));
      }

      if(request.bodyContent.size() != 0 || !request.parts.isEmpty()) {
        setContentType(conn, request);
        setRequestContent(conn.getOutputStream(), request);
      }
    }
    return conn;
  }

  /**
   * Sets content type of HTTP request.
   * @param conn HTTP connection
   * @param request request data
   */
  private static void setContentType(final HttpURLConnection conn, final HttpRequest request) {
    String mt;
    final byte[] contTypeHdr = request.headers.get(lc(token(CONTENT_TYPE)));
    if(contTypeHdr != null) {
      // if header "Content-Type" is set explicitly by the user, its value is used
      mt = string(contTypeHdr);
    } else {
      // otherwise @media-type of <http:body/> is considered
      mt = string(request.payloadAttrs.get(MEDIA_TYPE));
      if(request.isMultipart) {
        final byte[] b = request.payloadAttrs.get(BOUNDARY);
        mt = new TokenBuilder().add(mt).add("; ").add(BOUNDARY).add('=').
            add(b != null ? b : DEFAULT_BOUND).toString();
      }
    }
    conn.setRequestProperty(CONTENT_TYPE, mt);
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
      writePayload(request.bodyContent, request.payloadAttrs, out);
    }
    out.close();
  }

  /**
   * Writes the payload of a body or part in the output stream of the connection.
   * @param payload body/part payload
   * @param payloadAtts payload attributes
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writePayload(final ValueBuilder payload, final TokenMap payloadAtts,
      final OutputStream out) throws IOException {

    // detect method (specified by @method or derived from @media-type)
    final byte[] m = payloadAtts.get(METHOD);
    final String method;
    if(m == null) {
      final byte[] tp = payloadAtts.get(MEDIA_TYPE);
      final String type = tp == null ? "" : string(tp);
      if(Strings.eq(type, APP_HTML_XML)) {
        method = SerialMethod.XHTML.toString();
      } else if(Strings.eq(type, TEXT_HTML)) {
        method = SerialMethod.HTML.toString();
      } else if(isXML(type)) {
        method = SerialMethod.XML.toString();
      } else if(isText(type)) {
        method = SerialMethod.TEXT.toString();
      } else {
        // default serialization method is XML
        method = SerialMethod.XML.toString();
      }
    } else {
      method = string(m);
    }

    // write content depending on the method
    final byte[] src = payloadAtts.get(SRC);
    if(src == null) {
      write(payload, payloadAtts, method, out);
    } else {
      final IOUrl io = new IOUrl(string(src));
      if(Strings.eq(method, BINARY)) {
        out.write(io.read());
      } else {
        final ValueBuilder vb = new ValueBuilder().add(Str.get(new TextInput(io).content()));
        write(vb, payloadAtts, method, out);
      }
    }
  }

  /**
   * Writes the payload of a body using the serialization parameters.
   * @param payload payload
   * @param attrs payload attributes
   * @param method serialization method
   * @param out connection output stream
   * @throws IOException I/O Exception
   */
  private static void write(final ValueBuilder payload, final TokenMap attrs,
      final String method, final OutputStream out) throws IOException {

    // extract serialization parameters
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, method);
    for(final byte[] key : attrs) {
      if(!eq(key, SRC)) sopts.assign(string(key), string(attrs.get(key)));
    }

    // serialize items according to the parameters
    try(final Serializer ser = Serializer.get(out, sopts)) {
      payload.serialize(ser);
    }
  }

  /**
   * Writes parts of multipart message in the output stream of the HTTP
   * connection.
   * @param r request data
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writeMultipart(final HttpRequest r, final OutputStream out)
      throws IOException {
    final byte[] boundary = r.payloadAttrs.get(BOUNDARY);
    for(final Part part : r.parts) writePart(part, out, boundary);
    out.write(new TokenBuilder("--").add(boundary).add("--").add(CRLF).finish());
  }

  /**
   * Writes a single part of a multipart message.
   * @param part part
   * @param out connection output stream
   * @param boundary boundary
   * @throws IOException I/O exception
   */
  private static void writePart(final Part part, final OutputStream out, final byte[] boundary)
      throws IOException {

    // write boundary preceded by "--"
    final TokenBuilder boundTb = new TokenBuilder();
    boundTb.add("--").add(boundary).add(CRLF);
    out.write(boundTb.finish());

    // write headers
    for(final byte[] header : part.headers) {
      final TokenBuilder hdrTb = new TokenBuilder();
      hdrTb.add(header).add(": ").add(part.headers.get(header)).add(CRLF);
      out.write(hdrTb.finish());
    }
    out.write(CRLF);

    // write content
    writePayload(part.bodyContent, part.bodyAttrs, out);
    out.write(CRLF);
  }
}
