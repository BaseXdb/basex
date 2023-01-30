package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.http.HTTPText.*;
import static org.basex.util.http.RequestAttribute.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.time.*;
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

/**
 * HTTP Client.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Rositsa Shadura
 * @author Michael Seiferle
 */
public final class Client {
  /** Input information. */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;

  /**
   * Constructor.
   * @param info input info
   * @param options database options
   */
  public Client(final InputInfo info, final MainOptions options) {
    this.info = info;
    this.options = options;
  }

  /**
   * Sends an HTTP request and returns the response.
   * @param href URL to send the request to (can be empty string)
   * @param request request data
   * @param bodies request body
   * @return HTTP response
   * @throws QueryException query exception
   */
  public Value sendRequest(final byte[] href, final ANode request, final Value bodies)
      throws QueryException {

    final Request req = new RequestParser(info).parse(request, bodies);
    final URI uri = uri(href, req);
    final String mediaType = req.attribute(OVERRIDE_MEDIA_TYPE);
    final String status = req.attribute(STATUS_ONLY);
    final boolean body = status == null || !Strings.toBoolean(status);
    try {
      return new Response(info, options).getResponse(send(uri, req), body, mediaType);
    } catch(final IOException ex) {
      throw HC_ERROR_X.get(info, ex);
    }
  }

  /**
   * Returns a URI.
   * @param href URL to send the request to (can be empty string)
   * @param request request
   * @return URI
   * @throws QueryException query exception
   */
  private URI uri(final byte[] href, final Request request) throws QueryException {
    final String uri = href.length == 0 ? request.attribute(HREF) : Token.string(href);
    if(uri == null || uri.isEmpty()) throw HC_URL.get(info);
    try {
      return new URI(uri);
    } catch(final URISyntaxException ex) {
      Util.debug(ex);
      throw HC_URI_X.get(info, uri);
    }
  }

  /**
   * Returns an HTTP response for the specified request.
   * @param uri target URI
   * @param request request
   * @return HTTP connection
   * @throws IOException I/O Exception
   * @throws MalformedURLException incorrect url
   */
  private static HttpResponse<InputStream> send(final URI uri, final Request request)
      throws IOException {

    final HttpRequest.Builder rb = HttpRequest.newBuilder(uri);

    try {
      // set timeout
      final String timeout = request.attribute(TIMEOUT);
      if(timeout != null) rb.timeout(Duration.ofSeconds(Strings.toInt(timeout)));

      // set method, attach payload
      final String method = request.attribute(METHOD);
      if(method != null) {
        final BodyPublisher publisher;
        if(request.payload.isEmpty() && request.parts.isEmpty()) {
          publisher = HttpRequest.BodyPublishers.noBody();
        } else {
          setContentType(rb, request);
          publisher = HttpRequest.BodyPublishers.ofByteArray(payload(request));
        }
        rb.method(method, publisher);
      }

      // assign headers to request; ensure that Accept header is sent; catch illegal header names
      request.headers.forEach(rb::header);
      if(((Checks<String>) name -> !name.equalsIgnoreCase(ACCEPT)).all(request.headers.keySet())) {
        rb.header(ACCEPT, MediaType.ALL_ALL.toString());
      }
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      throw new IOException(ex.getMessage());
    }

    final String fw = request.attribute(FOLLOW_REDIRECT);
    final HttpClient client = IOUrl.client(fw == null || Strings.toBoolean(fw));
    final BodyHandler<InputStream> handler = HttpResponse.BodyHandlers.ofInputStream();

    // send request (with optional authorization)
    try {
      final UserInfo ui = new UserInfo(uri, request);
      final boolean sa = Strings.toBoolean(request.attribute(SEND_AUTHORIZATION));
      if(sa && request.authMethod == AuthMethod.BASIC) {
        ui.basic(rb);
      } else {
        final HttpResponse<InputStream> response = client.send(rb.build(), handler);
        if(!ui.assign(rb, response)) return response;
      }
      return client.send(rb.build(), handler);
    } catch(final InterruptedException ex) {
      Util.debug(ex);
      throw new IOException(ex.getMessage());
    }
  }

  /**
   * Sets the content type of the HTTP request.
   * @param rb HTTP request builder
   * @param request request data
   */
  private static void setContentType(final HttpRequest.Builder rb, final Request request) {
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
    rb.header(CONTENT_TYPE, ct);
  }

  /**
   * Returns the authentication headers.
   * @param auth authorization string
   * @return values values
   */
  public static EnumMap<RequestAttribute, String> authHeaders(final String auth) {
    final EnumMap<RequestAttribute, String> values = new EnumMap<>(RequestAttribute.class);
    if(auth != null) {
      final String[] parts = Strings.split(auth, ' ', 2);
      values.put(AUTH_METHOD, parts[0]);
      if(parts.length > 1) {
        for(final String header : Strings.split(parts[1], ',')) {
          final String[] kv = Strings.split(header, '=', 2);
          final String key = kv[0].trim();
          if(!key.isEmpty() && kv.length == 2) {
            final RequestAttribute r = RequestAttribute.get(key);
            if(r != null) values.put(r, Strings.delete(kv[1], '"').trim());
          }
        }
      }
    }
    return values;
  }

  /**
   * Returns the payload.
   * @param request request data
   * @return input stream
   * @throws IOException I/O exception
   */
  public static byte[] payload(final Request request) throws IOException {
    final ArrayOutput out = new ArrayOutput();
    if(request.isMultipart) {
      final String boundary = request.boundary();
      for(final Part part : request.parts) {
        // write content to cache
        final ArrayOutput ao = new ArrayOutput();
        writePayload(part.contents, part.attributes, ao);

        // write boundary preceded by "--"
        out.write(Token.concat("--", boundary, CRLF));

        // write headers
        for(final Entry<String, String> header : part.headers.entrySet())
          writeHeader(header.getKey(), header.getValue(), out);
        if(!part.headers.containsKey(CONTENT_TYPE))
          writeHeader(CONTENT_TYPE, part.attributes.get(SerializerOptions.MEDIA_TYPE.name()), out);

        out.write(CRLF);
        out.write(ao.finish());
        out.write(CRLF);
      }
      out.write(Token.concat("--", boundary, "--", CRLF));
    } else {
      writePayload(request.payload, request.payloadAtts, out);
    }
    return out.finish();
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
    out.write(Token.concat(key, ": ", value, CRLF));
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
        ser.serialize(atom && item.type instanceof NodeType ?
          ((ANode) item).atomItem(null, null) : item);
      }
    }
  }
}
