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

    try {
      if(request == null) {
        if(href == null || href.length == 0) throw HC_PARAMS.get(info);
        final HttpURLConnection conn = openConnection(string(href));
        try {
          return new HttpResponse(info, options).getResponse(conn, Bln.FALSE.string(), null);
        } finally {
          conn.disconnect();
        }
      }

      final HttpRequest r = new HttpRequestParser(info).parse(request, bodies);
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
        return new HttpResponse(info, options).getResponse(conn, r.attrs.get(STATUS_ONLY),
            mt == null ? null : string(mt));
      } finally {
        conn.disconnect();
      }
    } catch(final IOException ex) {
      throw HC_ERROR_X.get(info, ex);
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
    throw HC_ERROR_X.get(info, "Invalid URL: " + url);
  }

  /**
   * Sets the connection properties.
   * @param conn HTTP connection
   * @param r request data
   * @throws ProtocolException protocol exception
   * @throws QueryException query exception
   */
  private void setConnectionProps(final HttpURLConnection conn, final HttpRequest r)
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
  private static void setContentType(final HttpURLConnection conn, final HttpRequest r) {
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
   * @throws IOException
   */
  private void setRequestHeaders(HttpURLConnection conn, final HttpRequest r)
      throws QueryException, IOException {

    for(final byte[] headers : r.headers)
      conn.addRequestProperty(string(headers), string(r.headers.get(headers)));

    final byte[] sendAuth = r.attrs.get(SEND_AUTHORIZATION);
    final byte[] am = r.attrs.get(AUTH_METHOD);
    System.out.println(string(am));
    if(!string(am).equals(null) && string(am).equals(BASIC)) {
   // HTTP Basic Authentication
      if(sendAuth != null && Bln.parse(sendAuth, info))
      conn.setRequestProperty(AUTHORIZATION,
      encodeCredentials(string(r.attrs.get(USERNAME)), string(r.attrs.get(PASSWORD))));
  } else if(!string(am).equals(null) && string(am).equals(DIGEST)) {
   // HTTP Digest Authentication
      if(sendAuth != null && Bln.parse(sendAuth, info))
      //try {
        conn.setRequestProperty(AUTHORIZATION, DIGEST);
        String sr = conn.getHeaderField(WWW_AUTHENTICATE);
        System.out.println(sr);
        HashMap<String, String> headerValues = parseHeader(sr);
        String realm = headerValues.get("realm");
        String nonce = headerValues.get("nonce");
        String qop = headerValues.get("qop");

     // Client request
        String username = string(r.attrs.get(USERNAME));
        String password = string(r.attrs.get(PASSWORD));
        String cnonce = "0a4f113b";
        String nc = "00000001";
        String uri = "/uri";
        String ha1 = Strings.md5(username + ":" + realm + ":" + password);
        String ha2 = Strings.md5("GET:" + uri);
        String response = Strings.md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);

        String creds = "username=" + username + ", " + "realm=" + realm + ", " + "nonce=" + nonce
            + ", " + "uri=" + uri + ", " + "qop=" + qop + ", " + "nc=" + nc + ", " + "cnonce=" + cnonce
            + ", " + "response=" + response;

        conn = openConnection(string(r.attrs.get(HREF)));
        conn.setAllowUserInteraction(true);
        System.out.println(creds);
        conn.setRequestProperty(AUTHORIZATION, DIGEST + ' ' + creds);
  } else {
    System.out.println("Specify Authentication");
  }
    }

  /**
   * Parses Authentication Header.
   * @param headerString response header
   * @return headerValues
   */
  private static HashMap<String, String> parseHeader(final String headerString) {

    String headerStringWithoutScheme = headerString.substring(headerString.indexOf(" ") + 1).trim();
    HashMap<String, String> values = new HashMap<>();
    String[] keyValueArray = headerStringWithoutScheme.split(",");
    for(String keyval : keyValueArray) {
      if(keyval.contains("=")) {
        String key = keyval.substring(0, keyval.indexOf("="));
        String value = keyval.substring(keyval.indexOf("=") + 1);
        values.put(key.trim(), value.replaceAll("\"", "").trim());
      }
    }
    return values;
  }

  /**
   * Set HTTP request content.
   * @param out output stream
   * @param r request data
   * @throws IOException I/O exception
   */
  public void setRequestContent(final OutputStream out, final HttpRequest r) throws IOException {
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
