package org.basex.query.util.http;

import static java.net.HttpURLConnection.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.query.util.Err.*;
import static org.basex.query.util.http.HTTPText.*;
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
import org.basex.query.iter.*;
import org.basex.query.util.http.HTTPRequest.Part;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * HTTP Client.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class HTTPClient {
  /** Input information. */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;

  /**
   * Constructor.
   * @param info input info
   * @param options database options
   */
  public HTTPClient(final InputInfo info, final MainOptions options) {
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
  public Iter sendRequest(final byte[] href, final ANode request, final ValueBuilder bodies)
      throws QueryException {

    try {
      if(request == null) {
        if(href == null || href.length == 0) throw HC_PARAMS.get(info);
        final HttpURLConnection conn = openConnection(string(href));
        try {
          return new HTTPResponse(info, options).getResponse(conn, Bln.FALSE.string(), null);
        } finally {
          conn.disconnect();
        }
      }

      final HTTPRequest r = new HTTPRequestParser(info).parse(request, bodies);
      final byte[] dest = href == null ? r.attrs.get(HREF) : href;
      if(dest == null) throw HC_URL.get(info);

      final HttpURLConnection conn = openConnection(string(dest));
      try {
        setConnectionProps(conn, r);
        setRequestHeaders(conn, r);

        if(r.bodyContent.size() != 0 || !r.parts.isEmpty()) {
          setContentType(conn, r);
          setRequestContent(conn.getOutputStream(), r);
        }
        final byte[] mt = r.attrs.get(OVERRIDE_MEDIA_TYPE);
        return new HTTPResponse(info, options).getResponse(conn, r.attrs.get(STATUS_ONLY),
            mt == null ? null : string(mt));
      } finally {
        conn.disconnect();
      }
    } catch(final IOException ex) {
      throw HC_ERROR.get(info, ex);
    }
  }

  /**
   * Opens an HTTP connection.
   * @param url HTTP URL to open connection to
   * @return HHTP connection
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   * @throws MalformedURLException incorrect url
   */
  private HttpURLConnection openConnection(final String url) throws QueryException, IOException {
    final URLConnection conn = new IOUrl(url).connection();
    if(conn instanceof HttpURLConnection) return (HttpURLConnection) conn;
    throw HC_ERROR.get(info, "Invalid URL: " + url);
  }

  /**
   * Sets the connection properties.
   * @param conn HTTP connection
   * @param r request data
   * @throws ProtocolException protocol exception
   * @throws QueryException query exception
   */
  private void setConnectionProps(final HttpURLConnection conn, final HTTPRequest r)
      throws ProtocolException, QueryException {

    conn.setDoOutput(true);
    final String method = string(r.attrs.get(METHOD)).toUpperCase(Locale.ENGLISH);
    try {
      // set field via reflection to circumvent string check
      final Field f = conn.getClass().getSuperclass().getDeclaredField("method");
      f.setAccessible(true);
      f.set(conn, method);
    } catch(final Throwable th) {
      conn.setRequestMethod(method);
    }

    final byte[] timeout = r.attrs.get(TIMEOUT);
    if(timeout != null) conn.setConnectTimeout(Integer.parseInt(string(timeout)));
    final byte[] redirect = r.attrs.get(FOLLOW_REDIRECT);
    if(redirect != null) setFollowRedirects(Bln.parse(redirect, info));
  }

  /**
   * Sets content type of HTTP request.
   * @param conn HTTP connection
   * @param r request data
   */
  private static void setContentType(final HttpURLConnection conn, final HTTPRequest r) {
    String mt;
    final byte[] contTypeHdr = r.headers.get(lc(token(CONTENT_TYPE)));
    if(contTypeHdr != null) {
      // if header "Content-Type" is set explicitly by the user, its value is used
      mt = string(contTypeHdr);
    } else {
      // otherwise @media-type of <http:body/> is considered
      mt = string(r.payloadAttrs.get(MEDIA_TYPE));
      if(r.isMultipart) {
        final byte[] b = r.payloadAttrs.get(BOUNDARY);
        final byte[] boundary = b != null ? b : DEFAULT_BOUND;
        final TokenBuilder tb = new TokenBuilder();
        mt = tb.add(mt).add("; ").add(BOUNDARY).add('=').add(boundary).toString();
      }
    }
    conn.setRequestProperty(CONTENT_TYPE, mt);
  }

  /**
   * Sets HTTP request headers.
   * @param conn HTTP connection
   * @param r request data
   * @throws QueryException query exception
   */
  private void setRequestHeaders(final HttpURLConnection conn, final HTTPRequest r)
      throws QueryException {

    for(final byte[] headers : r.headers)
      conn.addRequestProperty(string(headers), string(r.headers.get(headers)));
    // HTTP Basic Authentication
    final byte[] sendAuth = r.attrs.get(SEND_AUTHORIZATION);
    if(sendAuth != null && Bln.parse(sendAuth, info))
      conn.setRequestProperty(AUTHORIZATION,
      encodeCredentials(string(r.attrs.get(USERNAME)), string(r.attrs.get(PASSWORD))));
  }

  /**
   * Set HTTP request content.
   * @param out output stream
   * @param r request data
   * @throws IOException I/O exception
   */
  public void setRequestContent(final OutputStream out, final HTTPRequest r) throws IOException {
    if(r.isMultipart) {
      writeMultipart(r, out);
    } else {
      writePayload(r.bodyContent, r.payloadAttrs, out);
    }
    out.close();
  }

  /**
   * Encodes credentials with Base64 encoding.
   * @param u user name
   * @param p password
   * @return encoded credentials
   */
  private static String encodeCredentials(final String u, final String p) {
    return BASIC + ' ' + org.basex.util.Base64.encode(u + ':' + p);
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
      if(eq(type, APP_HTML_XML)) {
        method = SerialMethod.XHTML.toString();
      } else if(eq(type, TEXT_HTML)) {
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
      if(eq(method, BINARY)) {
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
    final Serializer ser = Serializer.get(out, sopts);
    try {
      payload.serialize(ser);
    } finally {
      ser.close();
    }
  }

  /**
   * Writes parts of multipart message in the output stream of the HTTP
   * connection.
   * @param r request data
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writeMultipart(final HTTPRequest r, final OutputStream out)
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
