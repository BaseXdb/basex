package org.basex.query.util.http;

import static java.lang.Integer.*;
import static java.net.HttpURLConnection.*;
import static org.basex.data.DataText.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.query.util.Err.*;
import static org.basex.query.util.http.HTTPText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class HTTPClient {
  /** Input information. */
  private final InputInfo info;
  /** Database properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param ii input info
   * @param pr database properties
   */
  public HTTPClient(final InputInfo ii, final Prop pr) {
    info = ii;
    prop = pr;
  }

  /**
   * Sends an HTTP request and returns the response.
   * @param href URL to send the request to
   * @param request request data
   * @param bodies content items
   * @return HTTP response
   * @throws QueryException query exception
   */
  public Iter sendRequest(final byte[] href, final ANode request,
      final ValueBuilder bodies) throws QueryException {

    try {
      if(request == null) {
        if(href == null || href.length == 0) HC_PARAMS.thrw(info);
        final HttpURLConnection conn = openConnection(string(href));
        try {
          return new HTTPResponse(info, prop).getResponse(
              conn, Bln.FALSE.string(), null);
        } finally {
          conn.disconnect();
        }
      }

      final HTTPRequest r = new HTTPRequestParser(info).parse(request, bodies);
      final byte[] dest = href == null ? r.attrs.get(HREF) : href;
      if(dest == null) HC_URL.thrw(info);

      final HttpURLConnection conn = openConnection(string(dest));
      try {
        setConnectionProps(conn, r);
        setRequestHeaders(conn, r);

        if(r.bodyContent.size() != 0 || r.parts.size() != 0) {
          setContentType(conn, r);
          setRequestContent(conn.getOutputStream(), r);
        }
        return new HTTPResponse(info, prop).getResponse(conn, r.attrs.get(STATUS_ONLY),
            r.attrs.get(OVERRIDE_MEDIA_TYPE));
      } finally {
        conn.disconnect();
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      throw HC_ERROR.thrw(info, ex);
    }
  }

  /**
   * Opens an HTTP connection.
   * @param dest HTTP URI to open connection to
   * @return HHTP connection
   * @throws QueryException query exception
   * @throws IOException I/O Exception
   * @throws MalformedURLException incorrect url
   */
  private HttpURLConnection openConnection(final String dest)
      throws QueryException, IOException {

    final URL url = new URL(dest);
    if(!eqic(url.getProtocol(), "HTTP", "HTTPS"))
      HC_ERROR.thrw(info, "Invalid URL: " + url);
    return (HttpURLConnection) url.openConnection();
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

    if(r.bodyContent != null || r.parts.size() != 0) conn.setDoOutput(true);
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
    if(timeout != null) conn.setConnectTimeout(parseInt(string(timeout)));
    final byte[] redirect = r.attrs.get(FOLLOW_REDIRECT);
    if(redirect != null) setFollowRedirects(Bln.parse(redirect, info));
  }

  /**
   * Sets content type of HTTP request.
   * @param conn HTTP connection
   * @param r request data
   */
  private static void setContentType(final HttpURLConnection conn,
      final HTTPRequest r) {

    final byte[] contTypeHdr = r.headers.get(lc(token(CONTENT_TYPE)));
    // if header "Content-Type" is set explicitly by the user, its value is used
    if(contTypeHdr != null) {
      conn.setRequestProperty(CONTENT_TYPE, string(contTypeHdr));
      // otherwise @media-type of <http:body/> is considered
    } else {
      String mt = string(r.payloadAttrs.get(MEDIA_TYPE));
      if(r.isMultipart) {
        final byte[] b = r.payloadAttrs.get(BOUNDARY);
        final byte[] boundary = b != null ? b : DEFAULT_BOUND;
        final TokenBuilder tb = new TokenBuilder();
        mt = tb.add(mt).add("; ").add(BOUNDARY).add('=').add(boundary).toString();
      }
      conn.setRequestProperty(CONTENT_TYPE, mt);
    }
  }

  /**
   * Sets HTTP request headers.
   * @param conn HTTP connection
   * @param r request data
   * @throws QueryException query exception
   */
  private void setRequestHeaders(final HttpURLConnection conn,
      final HTTPRequest r) throws QueryException {

    final byte[][] headerNames = r.headers.keys();
    for(final byte[] headerName : headerNames)
      conn.addRequestProperty(string(headerName), string(r.headers.get(headerName)));
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
   * @throws QueryException query exception
   */
  public void setRequestContent(final OutputStream out, final HTTPRequest r)
      throws IOException, QueryException {

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
    return BASIC + ' ' + Base64.encode(u + ':' + p);
  }

  /**
   * Writes the payload of a body or part in the output stream of the
   * connection.
   * @param payload body/part payload
   * @param payloadAtts payload attributes
   * @param out output stream
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private void writePayload(final ValueBuilder payload, final TokenMap payloadAtts,
      final OutputStream out) throws IOException, QueryException {

    final byte[] t = payloadAtts.get(MEDIA_TYPE);
    final String type = t == null ? null : string(t);

    // no resource to set the content from
    final byte[] src = payloadAtts.get(SRC);
    if(src == null) {
      // default value @method is determined by @media-type
      byte[] m = payloadAtts.get(METHOD);
      if(m == null) {
        if(eq(type, APP_HTML_XML)) {
          m = token(M_XHTML);
        } else if(isXML(type)) {
          m = token(M_XML);
        } else if(eq(type, TEXT_HTML)) {
          m = token(M_HTML);
        } else if(type != null && type.startsWith(MIME_TEXT_PREFIX)) {
          m = token(M_TEXT);
        } else {
          // default serialization method is XML
          m = token(M_XML);
        }
      }
      // write content depending on the method
      if(eq(m, BASE64)) {
        writeBase64(payload, out);
      } else if(eq(m, HEXBIN)) {
        writeHex(payload, out);
      } else {
        write(payload, payloadAtts, m, out);
      }
    } else {
      // if the src attribute is present, the content is set as the content of
      // the linked resource
      writeResource(src, out);
    }
  }

  /**
   * Writes the payload of a body in case method is base64Binary.
   * @param payload payload
   * @param out connection output stream
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private void writeBase64(final ValueBuilder payload, final OutputStream out)
      throws IOException, QueryException {

    for(int i = 0; i < payload.size(); i++) {
      final Item item = payload.get(i);
      if(item instanceof B64) {
        out.write(((B64) item).toJava());
      } else {
        out.write(new B64(item.string(info)).toJava());
      }
    }
  }

  /**
   * Writes the payload of a body in case method is hexBinary.
   * @param payload payload
   * @param out connection output stream
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  private void writeHex(final ValueBuilder payload, final OutputStream out)
      throws IOException, QueryException {

    for(int i = 0; i < payload.size(); i++) {
      final Item item = payload.get(i);
      if(item instanceof Hex) {
        out.write(((Hex) item).toJava());
      } else {
        out.write(new Hex(item.string(info)).toJava());
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
      final byte[] method, final OutputStream out) throws IOException {

    // extract serialization parameters
    final TokenBuilder tb = new TokenBuilder();
    tb.add(METHOD).add('=').add(method);
    for(final byte[] key : attrs.keys()) {
      if(!eq(key, SRC)) tb.add(',').add(key).add('=').add(attrs.get(key));
    }

    // serialize items according to the parameters
    final SerializerProp sp = new SerializerProp(tb.toString());
    final Serializer ser = Serializer.get(out, sp);
    try {
      payload.serialize(ser);
    } finally {
      ser.close();
    }
  }

  /**
   * Reads the content of the linked resource.
   * @param src resource link
   * @param out output stream
   * @throws IOException I/O Exception
   */
  private static void writeResource(final byte[] src, final OutputStream out)
      throws IOException {
    final InputStream bis = new URL(string(src)).openStream();
    try {
      final byte[] buf = new byte[256];
      while(true) {
        final int len = bis.read(buf, 0, buf.length);
        if(len <= 0) break;
        out.write(buf, 0, len);
      }
    } finally {
      bis.close();
    }
  }

  /**
   * Writes parts of multipart message in the output stream of the HTTP
   * connection.
   * @param r request data
   * @param out output stream
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private void writeMultipart(final HTTPRequest r, final OutputStream out)
      throws IOException, QueryException {

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
   * @throws QueryException query exception
   */
  private void writePart(final Part part, final OutputStream out,
      final byte[] boundary) throws IOException, QueryException {

    // write boundary preceded by "--"
    final TokenBuilder boundTb = new TokenBuilder();
    boundTb.add("--").add(boundary).add(CRLF);
    out.write(boundTb.finish());

    // write headers
    for(final byte[] headerName : part.headers.keys()) {
      final TokenBuilder hdrTb = new TokenBuilder();
      hdrTb.add(headerName).add(": ").add(part.headers.get(headerName)).add(CRLF);
      out.write(hdrTb.finish());
    }
    out.write(CRLF);

    // write content
    writePayload(part.bodyContent, part.bodyAttrs, out);
    out.write(CRLF);
  }
}
