package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.http.HTTPText.*;
import static org.basex.util.http.RequestAttribute.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.Map.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.io.serial.SerializerOptions.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * HTTP Client.
 *
 * @author BaseX Team, BSD License
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
   * @param info input info (can be {@code null})
   * @param options main options
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
   * @param qc query context
   * @return HTTP response
   * @throws QueryException query exception
   */
  public Value sendRequest(final byte[] href, final XNode request, final Value bodies,
      final QueryContext qc) throws QueryException {

    final Request req = new RequestParser(info).parse(request, bodies);
    final URI uri = uri(href, req);
    final String mediaType = req.attribute(OVERRIDE_MEDIA_TYPE);
    final String status = req.attribute(STATUS_ONLY);
    final boolean body = status == null || Strings.isFalse(status);

    final MainOptions mopts = new MainOptions(options);
    try {
      mopts.set(MainOptions.CSVPARSER,
          assign(new CsvParserOptions(mopts.get(MainOptions.CSVPARSER)), req.attribute(CSV)));
      mopts.set(MainOptions.JSONPARSER,
          assign(new JsonParserOptions(mopts.get(MainOptions.JSONPARSER)), req.attribute(JSON)));
      mopts.set(MainOptions.HTMLPARSER,
          assign(new HtmlOptions(mopts.get(MainOptions.HTMLPARSER)), req.attribute(HTML)));

      final Exchange exchange = new Exchange(uri, req, client(req));
      return new Response(info, mopts, exchange, qc).
        getResponse(exchange.send(), body, mediaType);
    } catch(final HttpTimeoutException ex) {
      throw HC_TIMEOUT.get(info).cause(ex);
    } catch(final IOException ex) {
      throw HC_ERROR_X.get(info, ex);
    }
  }

  /**
   * Assigns parser options.
   * @param <O> option type
   * @param opts options
   * @param value value to assign (can be {@code null})
   * @return supplied options
   * @throws IOException I/O exception
   */
  private static <O extends Options> O assign(final O opts, final String value) throws IOException {
    if(value != null) opts.assign(value);
    return opts;
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
      return new URI(IOUrl.toAscii(uri));
    } catch(final URISyntaxException ex) {
      throw HC_URI_X.get(info, uri).cause(ex);
    }
  }

  /**
   * Returns the HTTP client for a request.
   * @param request request
   * @return client
   */
  private static HttpClient client(final Request request) {
    final String fw = request.attribute(FOLLOW_REDIRECT);
    return IOUrl.client(fw == null || Strings.isTrue(fw));
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
        for(final String header : splitFields(parts[1])) {
          final String[] kv = Strings.split(header, '=', 2);
          final String key = kv[0].trim();
          if(!key.isEmpty() && kv.length == 2) {
            final RequestAttribute r = Enums.get(RequestAttribute.class, key);
            if(r != null) values.put(r, Strings.delete(kv[1], '"').trim());
          }
        }
      }
    }
    return values;
  }

  /**
   * Splits a comma-separated list of authentication fields, ignoring commas inside quoted
   * strings (e.g. a challenge with {@code qop="auth,auth-int"}).
   * @param string field list
   * @return single fields
   */
  private static ArrayList<String> splitFields(final String string) {
    final ArrayList<String> fields = new ArrayList<>();
    final StringBuilder sb = new StringBuilder();
    boolean quoted = false;
    final int sl = string.length();
    for(int s = 0; s < sl; s++) {
      final char ch = string.charAt(s);
      if(ch == '"') quoted = !quoted;
      if(ch == ',' && !quoted) {
        fields.add(sb.toString());
        sb.setLength(0);
      } else {
        sb.append(ch);
      }
    }
    fields.add(sb.toString());
    return fields;
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

      // serialization parameters (binary is resolved below, so that nodes get atomized)
      if(key.equals(SerializerOptions.METHOD.name())) {
        method = value;
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
      } else if(mt.isXml()) {
        method = SerialMethod.XML.toString();
      } else if(mt.isJSON()) {
        method = SerialMethod.JSON.toString();
      } else if(mt.isCSV()) {
        method = SerialMethod.CSV.toString();
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
        ser.serialize(atom && item instanceof final XNode xnode ?
          xnode.atomItem(null, null) : item);
      }
    }
  }
}
