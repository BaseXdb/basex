package org.basex.query.util.http;

import static java.lang.Integer.*;
import static java.net.HttpURLConnection.*;
import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Locale;

import org.basex.core.Prop;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.B64;
import org.basex.query.item.Bln;
import org.basex.query.item.Hex;
import org.basex.query.item.Item;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.util.http.Request.Part;
import org.basex.util.Base64;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.hash.TokenMap;

/**
 * HTTP Client.
 *
 * @author BaseX Team 2005-12, BSD License
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
  /** Carriage return/line feed. */
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
  private static final byte[] APPL_EXT_XML =
    token("application/xml-external-parsed-entity");
  /** XML media type. */
  private static final byte[] TXT_XML = token("text/xml");
  /** XML media type. */
  private static final byte[] TXT_EXT_XML =
    token("text/xml-external-parsed-entity");
  /** XML media types' suffix. */
  private static final byte[] MIME_XML_SUFFIX = token("+xml");
  /** HTML media type. */
  private static final byte[] TXT_HTML = token("text/html");
  /** Text media types' prefix. */
  private static final byte[] MIME_TEXT_PREFIX = token("text/");

  /** Serialization methods defined by the EXPath specification. */
  /** Method http:base64Binary. */
  private static final byte[] BASE64 = token("http:base64Binary");
  /** Method http:hexBinary. */
  private static final byte[] HEXBIN = token("http:hexBinary");

  /** HTTP header: Content-Type. */
  private static final String CONT_TYPE = "Content-Type";
  /** HTTP header: Authorization. */
  private static final String AUTH = "Authorization";
  /** HTTP basic authentication. */
  private static final String AUTH_BASIC = "Basic ";

  /** Input information. */
  private final InputInfo input;
  /** Database properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param ii input info
   * @param pr database properties
   */
  public HTTPClient(final InputInfo ii, final Prop pr) {
    input = ii;
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
      final ItemCache bodies) throws QueryException {

    try {
      if(request == null) {
        if(href == null || href.length == 0) NOPARAMS.thrw(input);
        final HttpURLConnection conn = openConnection(string(href));
        try {
          return new ResponseHandler(input, prop).getResponse(
              conn, Bln.FALSE.string(), null);
        } finally {
          conn.disconnect();
        }
      }

      final Request r = new RequestParser(input).parse(request, bodies);
      final byte[] dest = href == null ? r.attrs.get(HREF) : href;
      if(dest == null) NOURL.thrw(input);

      final HttpURLConnection conn = openConnection(string(dest));
      try {
        setConnectionProps(conn, r);
        setRequestHeaders(conn, r);

        if(r.bodyContent.size() != 0 || r.parts.size() != 0) {
          setContentType(conn, r);
          setRequestContent(conn.getOutputStream(), r);
        }
        return new ResponseHandler(input, prop).getResponse(
            conn, r.attrs.get(STATUSONLY), r.attrs.get(OVERMEDIATYPE));
      } finally {
        conn.disconnect();
      }
    } catch(final MalformedURLException ex) {
      throw HTTPERR.thrw(input, "Invalid URL");
    } catch(final ProtocolException ex) {
      throw HTTPERR.thrw(input, "Invalid HTTP method");
    } catch(final IOException ex) {
      throw HTTPERR.thrw(input, ex);
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
      HTTPERR.thrw(input, "Invalid URL");
    return (HttpURLConnection) url.openConnection();
  }

  /**
   * Sets the connection properties.
   * @param conn HTTP connection
   * @param r request data
   * @throws ProtocolException protocol exception
   * @throws QueryException query exception
   */
  private void setConnectionProps(final HttpURLConnection conn, final Request r)
      throws ProtocolException, QueryException {

    if(r.bodyContent != null || r.parts.size() != 0) conn.setDoOutput(true);
    conn.setRequestMethod(
        string(r.attrs.get(METHOD)).toUpperCase(Locale.ENGLISH));
    final byte[] timeout = r.attrs.get(TIMEOUT);
    if(timeout != null) conn.setConnectTimeout(parseInt(string(timeout)));
    final byte[] redirect = r.attrs.get(REDIR);
    if(redirect != null) setFollowRedirects(Bln.parse(redirect, input));
  }

  /**
   * Sets content type of HTTP request.
   * @param conn HTTP connection
   * @param r request data
   */
  private static void setContentType(final HttpURLConnection conn,
      final Request r) {

    final byte[] contTypeHdr = r.headers.get(lc(token(CONT_TYPE)));
    // if header "Content-Type" is set explicitly by the user, its value is used
    if(contTypeHdr != null) {
      conn.setRequestProperty(CONT_TYPE, string(contTypeHdr));
      // otherwise @media-type of <http:body/> is considered
    } else {
      final String mediaType = string(r.payloadAttrs.get(MEDIATYPE));
      if(r.isMultipart) {
        final byte[] b = r.payloadAttrs.get(BOUNDARY);
        final String boundary = b != null ? string(b) : DEFAULT_BOUND;
        final StringBuilder sb = new StringBuilder();
        sb.append(mediaType).append("; ").append("boundary=").append(boundary);
        conn.setRequestProperty(CONT_TYPE, sb.toString());
      } else {
        conn.setRequestProperty(CONT_TYPE, mediaType);
      }
    }
  }

  /**
   * Sets HTTP request headers.
   * @param conn HTTP connection
   * @param r request data
   * @throws QueryException query exception
   */
  private void setRequestHeaders(final HttpURLConnection conn,
      final Request r) throws QueryException {

    final byte[][] headerNames = r.headers.keys();
    for(final byte[] headerName : headerNames)
      conn.addRequestProperty(string(headerName),
          string(r.headers.get(headerName)));
    // HTTP Basic Authentication
    final byte[] sendAuth = r.attrs.get(SENDAUTH);
    if(sendAuth != null && Bln.parse(sendAuth, input))
      conn.setRequestProperty(AUTH,
        encodeCredentials(string(r.attrs.get(USRNAME)),
            string(r.attrs.get(PASSWD))));
  }

  /**
   * Set HTTP request content.
   * @param out output stream
   * @param r request data
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public void setRequestContent(final OutputStream out, final Request r)
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
    return AUTH_BASIC + Base64.encode(u + ':' + p);
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
  private void writePayload(final ItemCache payload, final TokenMap payloadAtts,
      final OutputStream out) throws IOException, QueryException {

    final byte[] mediaType = payloadAtts.get(MEDIATYPE);
    byte[] method = payloadAtts.get(METHOD);
    final byte[] src = payloadAtts.get(SRC);

    // no resource to set the content from
    if(src == null) {
      // default value @method is determined by @media-type
      if(method == null) {
        if(eq(mediaType, APPL_XHTML)) method = token(M_XHTML);
        else if(eq(mediaType, APPL_XML) || eq(mediaType, APPL_EXT_XML)
            || eq(mediaType, TXT_XML) || eq(mediaType, TXT_EXT_XML)
            || endsWith(mediaType, MIME_XML_SUFFIX)) method = token(M_XML);
        else if(eq(mediaType, TXT_HTML)) method = token(M_HTML);
        else if(startsWith(mediaType, MIME_TEXT_PREFIX)) method = token(M_TEXT);
        // default serialization method is XML
        else method = token(M_XML);
      }
      // write content depending on the method
      if(eq(method, BASE64)) {
        writeBase64(payload, out);
      } else if(eq(method, HEXBIN)) {
        writeHex(payload, out);
      } else {
        write(payload, payloadAtts, method, out);
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
  private void writeBase64(final ItemCache payload,
      final OutputStream out) throws IOException, QueryException {

    for(int i = 0; i < payload.size(); i++) {
      final Item item = payload.get(i);
      if(item instanceof B64) {
        out.write(((B64) item).toJava());
      } else {
        out.write(new B64(item.string(input)).toJava());
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
  private void writeHex(final ItemCache payload, final OutputStream out)
      throws IOException, QueryException {

    for(int i = 0; i < payload.size(); i++) {
      final Item item = payload.get(i);
      if(item instanceof Hex) {
        out.write(((Hex) item).toJava());
      } else {
        out.write(new Hex(item.string(input)).toJava());
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
  private static void write(final ItemCache payload, final TokenMap attrs,
      final byte[] method, final OutputStream out) throws IOException {

    // extract serialization parameters
    final TokenBuilder tb = new TokenBuilder();
    tb.add(METHOD).add('=').add(method);
    for(final byte[] key : attrs.keys()) {
      if(!eq(key, SRC))
        tb.add(',').add(key).add('=').add(attrs.get(key));
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
  private void writeMultipart(final Request r, final OutputStream out)
      throws IOException, QueryException {

    final byte[] boundary = r.payloadAttrs.get(BOUNDARY);
    for(final Part part : r.parts) writePart(part, out, boundary);
    out.write(new TokenBuilder().add("--").
        add(boundary).add("--").add(CRLF).finish());
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
      hdrTb.add(headerName).add(": ").add(
          part.headers.get(headerName)).add(CRLF);
      out.write(hdrTb.finish());
    }
    out.write(CRLF);

    // write content
    writePayload(part.bodyContent, part.bodyAttrs, out);
    out.write(CRLF);
  }
}
