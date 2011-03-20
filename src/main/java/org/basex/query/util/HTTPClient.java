package org.basex.query.util;

import static java.lang.Integer.*;
import static java.net.HttpURLConnection.*;
import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;

import org.basex.core.Prop;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenMap;

/**
 * HTTP requestor - either request or part in case of multipart message.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class HTTPClient {

  /** Request attribute: HTTP method. */
  private static final byte[] METHOD = token("method");
  /** Request attribute: username. */
  private static final byte[] USRNAME = token("username");
  /** Request attribute: password. */
  private static final byte[] PASSWD = token("password");
  /** Request attribute: send-authorization. */
  private static final byte[] SENDAUTH = token("send-authorization");
  /** Body attribute: media-type. */
  private static final byte[] MEDIATYPE = token("media-type");

  /** Request attribute: href. */
  private static final byte[] HREF = token("href");
  /** Request attribute: status-only. */
  private static final byte[] STATUSONLY = token("status-only");
  /** Request attribute: override-media-type. */
  private static final byte[] OVERMEDIATYPE = token("override-media-type");
  /** Request attribute: follow-redirect. */
  private static final byte[] REDIR = token("follow-redirect");
  /** Request attribute: timeout. */
  private static final byte[] TIMEOUT = token("timeout");

  /** boundary marker. */
  private static final byte[] BOUNDARY = token("boundary");
  /** Carriage return/linefeed. */
  private static final byte[] CRLF = token("\r\n");
  /** Default multipart boundary. */
  private static final String DEFAULT_BOUND = "1BEF0A57BE110FD467A";

  /** Body attribute: src. */
  private static final byte[] SRC = token("src");

  /** Media Types. */
  /** XML media type. */
  private static final byte[] APPL_XHTML = token("application/html+xml");
  /** XML media type. */
  private static final byte[] APPL_XML = token("application/xml");
  /** XML media type. */
  private static final byte[] APPL_EXT_XML = token("application/xml-external-parsed-entity");
  /** XML media type. */
  private static final byte[] TXT_XML = token("text/xml");
  /** XML media type. */
  private static final byte[] TXT_EXT_XML = token("text/xml-external-parsed-entity");
  /** XML media types' suffix. */
  private static final byte[] MIME_XML_SUFFIX = token("+xml");
  /** HTML media type. */
  private static final byte[] TXT_HTML = token("text/html");
  /** Text media types' prefix. */
  private static final byte[] MIME_TEXT_PREFIX = token("text/");

  /** HTTP header: Content-Type. */
  private static final String CONT_TYPE = "Content-Type";
  /** HTTP header: Authorization. */
  private static final String AUTH = "Authorization";
  /** HTTP basic authentication. */
  private static final String AUTH_BASIC = "Basic ";

  /**
   * Constructor.
   */
  private HTTPClient() {

  }

  /**
   * Sends an HTTP request.
   * @param href URL to send the request to
   * @param request request data
   * @param ii input info
   * @param prop query context properties
   * @return HTTP response
   * @throws QueryException query exception
   */
  public static Iter sendRequest(final byte[] href, final ANode request,
      final InputInfo ii, final Prop prop) throws QueryException {

    final Request r = RequestParser.parse(request, ii);

    final byte[] dest = href != null ? href : r.attrs.get(HREF);
    if(dest == null) NOURL.thrw(ii);

    try {
      final URL url = new URL(string(dest));
      final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      try {
        setConnectionProps(conn, r, ii);
        setRequestHeaders(conn, r, ii);

        if(r.bodyContent.size() != 0 || r.parts.size() != 0) {
          setContentType(conn, r);
          setRequestContent(conn.getOutputStream(), r);
        }
        return ResponseHandler.getResponse(conn, r.attrs.get(STATUSONLY),
            r.attrs.get(OVERMEDIATYPE), prop, ii);
      } finally {
        conn.disconnect();
      }
    } catch(final MalformedURLException ex) {
      throw URLINV.thrw(ii, href);
    } catch(final ProtocolException ex) {
      throw PROTINV.thrw(ii);
    } catch(final IOException ex) {
      throw HTTPERR.thrw(ii, ex);
    }
  }

  /**
   * Sets the connection properties.
   * @param conn HTTP connection
   * @param r request data
   * @param ii input info
   * @throws ProtocolException protocol exception
   * @throws QueryException query exception
   */
  private static void setConnectionProps(final HttpURLConnection conn,
      final Request r, final InputInfo ii) throws ProtocolException,
      QueryException {
    if(r.bodyContent != null || r.parts.size() != 0) conn.setDoOutput(true);
    conn.setRequestMethod(string(r.attrs.get(METHOD)).toUpperCase());
    // TODO: Investigate more about timeout
    final byte[] timeout = r.attrs.get(TIMEOUT);
    if(timeout != null) conn.setConnectTimeout(parseInt(string(timeout)));
    // TODO: Investigate more about follow-redirects
    final byte[] redirect = r.attrs.get(REDIR);
    if(redirect != null) setFollowRedirects(Bln.parse(redirect, ii));
  }

  /**
   * Sets content type of HTTP request.
   * @param conn http connection
   * @param r request data
   */
  private static void setContentType(final HttpURLConnection conn,
      final Request r) {
    final String mediaType = string(r.payloadAttrs.get(MEDIATYPE));
    if(r.isMultipart) {
      final String b = string(r.payloadAttrs.get(BOUNDARY));
      final String boundary = (b != null) ? b : DEFAULT_BOUND;
      StringBuilder sb = new StringBuilder();
      sb.append(mediaType).append("; ").append("boundary=").append(boundary);
      conn.setRequestProperty(CONT_TYPE, sb.toString());
    } else {
      conn.setRequestProperty(CONT_TYPE, mediaType);
    }
  }

  /**
   * Sets HTTP request headers.
   * @param conn HTTP connection
   * @param r requets data
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void setRequestHeaders(final HttpURLConnection conn,
      final Request r, final InputInfo ii) throws QueryException {

    final byte[][] headerNames = r.headers.keys();

    for(final byte[] headerName : headerNames)
      conn.addRequestProperty(string(headerName),
          string(r.headers.get(headerName)));
    // HTTP Basic Authentication
    final byte[] sendAuth = r.attrs.get(SENDAUTH);
    // TODO: more test cases w/o username, sendauth, pass
    if(sendAuth != null && Bln.parse(sendAuth, ii)) conn.setRequestProperty(
        AUTH,
        encodeCredentials(string(r.attrs.get(USRNAME)),
            string(r.attrs.get(PASSWD))));
  }

  /**
   * Set HTTP request content.
   * @param out output stream
   * @param r request data
   * @throws IOException I/O exception
   */
  private static void setRequestContent(final OutputStream out, final Request r)
      throws IOException {
    if(r.isMultipart) {
      writeMultipart(r, out);
    } else {
      writePayload(r.bodyContent, r.payloadAttrs, out);
    }
  }

  /**
   * Encodes credentials with Base64 encoding.
   * @param u user name
   * @param p password
   * @return encoded credentials
   */
  private static String encodeCredentials(final String u, final String p) {
    final B64 b64 = new B64(token(u + ":" + p));
    return AUTH_BASIC + string(b64.atom());
  }

  /**
   * Writes the payload(content) of a body or part in the output stream of the
   * connection.
   * @param payload body/part payload
   * @param payloadAtts payload attributes
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writePayload(final ItemCache payload,
      final TokenMap payloadAtts, final OutputStream out) throws IOException {
    final StringBuilder sb = new StringBuilder();
    byte[] mediaType = payloadAtts.get(MEDIATYPE);
    String src = null;
    String method = null;

    for(int i = 0; i < payloadAtts.size(); i++) {
      byte[] key = payloadAtts.keys()[i];
      if(eq(key, MEDIATYPE)) mediaType = payloadAtts.get(key);
      else if(eq(key, SRC)) src = string(payloadAtts.get(key));
      else if(eq(key, METHOD)) method = string(payloadAtts.get(key));
      sb.append(string(key)).append("=").append(string(payloadAtts.get(key)));
    }

    if(src == null) {
      // Set serial parameter "method" according to MIME type
      if(sb.length() != 0) sb.append(',');
      sb.append("method=");
      if(method == null) {
        if(eq(mediaType, APPL_XHTML)) sb.append(M_XHTML);
        else if(eq(mediaType, APPL_XML) || eq(mediaType, APPL_EXT_XML)
            || eq(mediaType, TXT_XML) || eq(mediaType, TXT_EXT_XML)
            || endsWith(mediaType, MIME_XML_SUFFIX)) sb.append(M_XML);
        else if(eq(mediaType, TXT_HTML)) sb.append(M_HTML);
        else if(startsWith(mediaType, MIME_TEXT_PREFIX)) sb.append(M_TEXT);
        else sb.append(M_XML);
      } else {
        sb.append(method);
      }

      // Serialize request content according to the
      // serialization parameters
      final SerializerProp serialProp = new SerializerProp(sb.toString());

      final XMLSerializer xml = new XMLSerializer(out, serialProp);
      try {
        // final AxisIter ai = body.children();
        for(int i = 0; i < payload.size(); i++) {
          payload.get(i).serialize(xml);
        }
      } finally {
        xml.cls();
      }
    } else {
      // [RS] If the src attribute is present, the serialization
      // parameters shall be ignored
    }
  }

  /**
   * Writes the parts of multipart message in the output stream of the URL
   * connection.
   * @param r request data
   * @param out output stream
   * @throws IOException I/O exception
   */
  private static void writeMultipart(final Request r, final OutputStream out)
      throws IOException {

    final byte[] boundary = r.payloadAttrs.get(BOUNDARY);
    final Iterator<Part> i = r.parts.iterator();
    while(i.hasNext())
      writePart(i.next(), out, boundary);
    out.write(new TokenBuilder().add("--").add(boundary).add("--").add(CRLF).finish());
  }

  /**
   * Writes a single part of a multipart message.
   * @param part part
   * @param out connection output stream
   * @param boundary boundary
   * @throws IOException I/O exception
   */
  private static void writePart(final Part part, final OutputStream out,
      final byte[] boundary) throws IOException {
    // Write boundary preceded by "--"
    TokenBuilder boundTb = new TokenBuilder();
    boundTb.add("--").add(boundary).add(CRLF);
    out.write(boundTb.finish());

    // Write headers
    for(final byte[] headerName : part.headers.keys()) {
      TokenBuilder hdrTb = new TokenBuilder();
      hdrTb.add(headerName).add(": ".getBytes()).add(
          part.headers.get(headerName)).add(CRLF);
      out.write(hdrTb.finish());
    }
    out.write(CRLF);

    // Write content
    writePayload(part.bodyContent, part.bodyAttrs, out);
    out.write(CRLF);
  }
}
