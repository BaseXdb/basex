package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.http.HTTPText.*;
import static org.basex.util.http.AuthParam.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.time.*;
import java.util.*;
import java.util.Map.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.core.jobs.*;
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
  /** Maximum number of redirects. */
  private static final int MAX_REDIRECTS = 5;

  /** Input information (can be {@code null}). */
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
   * @param resources query resources
   * @return HTTP response
   * @throws QueryException query exception
   */
  public Value sendRequest(final byte[] href, final XNode request, final Value bodies,
      final QueryResources resources) throws QueryException {

    final Request req = new RequestParser(info).parse(request, bodies);
    final URI uri = uri(href, req);
    final boolean body = !req.statusOnly;

    final MainOptions mopts = new MainOptions(options);
    try {
      mopts.set(MainOptions.CSVPARSER,
          assign(new CsvParserOptions(mopts.get(MainOptions.CSVPARSER)), req.csv));
      mopts.set(MainOptions.JSONPARSER,
          assign(new JsonParserOptions(mopts.get(MainOptions.JSONPARSER)), req.json));
      mopts.set(MainOptions.HTMLPARSER,
          assign(new HtmlOptions(mopts.get(MainOptions.HTMLPARSER)), req.html));

      final HttpResponse<InputStream> response = send(uri, req, client(req, resources));
      return new Response(info, mopts, resources).getResponse(response, body,
          req.overrideMediaType);
    } catch(final IOException ex) {
      throw error(ex, info);
    }
  }

  /**
   * Sends the request and returns the response.
   * @param uri target URI
   * @param request request data
   * @param client HTTP client
   * @return HTTP response
   * @throws IOException I/O exception
   */
  private static HttpResponse<InputStream> send(final URI uri, final Request request,
      final HttpClient client) throws IOException {

    final Duration timeout = request.timeout;
    final HttpRequest.Builder rb;
    try {
      rb = HttpRequest.newBuilder(uri);
      if(timeout != null) rb.timeout(timeout);

      // set method, attach payload
      final String method = request.method;
      final String src = request.isMultipart ? null : request.payloadAtts.get(SRC);
      final boolean hasBody = src != null ||
          !(request.payload.isEmpty() && request.parts.isEmpty());
      if(method != null) {
        if(hasBody) setContentType(rb, request);
        rb.method(method, hasBody ? publisher(src, request) : BodyPublishers.noBody());
      }

      // assign headers to request; the Content-Type of a payload request is already set above,
      // so skip it here to avoid sending it twice; ensure that Accept and User-Agent are sent
      request.headers.forEach((name, value) -> {
        if(!(hasBody && name.equalsIgnoreCase(CONTENT_TYPE))) rb.header(name, value);
      });
      if(!request.headers.containsKey(ACCEPT)) rb.header(ACCEPT, MediaType.ALL_ALL.toString());
      if(!request.headers.containsKey(USER_AGENT)) rb.header(USER_AGENT, IOUrl.AGENT);
    } catch(final IllegalArgumentException ex) {
      throw new IOException(ex.getMessage(), ex);
    }

    final BodyHandler<InputStream> handler = IOUrl.handler(timeout);

    // send request (with optional authorization)
    final boolean follow = request.followRedirect;
    try {
      final UserInfo ui = new UserInfo(uri, request);
      if(request.sendAuthorization && request.authMethod == AuthMethod.BASIC) {
        ui.basic(rb);
        return send(client, rb.build(), handler, follow);
      }
      final HttpRequest sent = rb.build();
      final HttpResponse<InputStream> response = send(client, sent, handler, follow);
      final HttpRequest retry = ui.assign(sent, response);
      if(retry == null) return response;
      response.body().close();
      return send(client, retry, handler, follow);
    } catch(final InterruptedException | IllegalArgumentException ex) {
      // illegal argument exception may be caused by wrongly encoded redirect URL
      throw new IOException(ex.getMessage(), ex);
    }
  }

  /**
   * Sends a request and follows redirect responses.
   * @param client HTTP client
   * @param request request to be sent
   * @param handler response body handler
   * @param follow follow redirects
   * @return response
   * @throws IOException I/O exception
   * @throws InterruptedException interruption
   */
  private static HttpResponse<InputStream> send(final HttpClient client, final HttpRequest request,
      final BodyHandler<InputStream> handler, final boolean follow)
      throws IOException, InterruptedException {

    HttpRequest sent = request;
    for(int r = 0; ; r++) {
      final HttpRequest current = sent;
      final HttpResponse<InputStream> response = Job.run(() -> client.send(current, handler));
      // if redirects are not followed, the redirect response itself is returned
      final HttpRequest next = follow ? redirect(current, response) : null;
      if(next == null) return response;
      if(r >= MAX_REDIRECTS) throw new IOException("Too many redirects: " + next.uri());
      // the body of a redirect response is discarded, and the connection is released
      response.body().close();
      sent = next;
    }
  }

  /**
   * Returns the request that follows a redirect response.
   * @param sent request that was sent
   * @param response response
   * @return request, or {@code null} if the redirect is not followed
   */
  private static HttpRequest redirect(final HttpRequest sent,
      final HttpResponse<InputStream> response) {

    final int status = response.statusCode();
    if(status != 301 && status != 302 && status != 303 && status != 307 && status != 308)
      return null;
    final String location = response.headers().firstValue(LOCATION).orElse(null);
    if(location == null) return null;

    final URI uri = sent.uri();
    final URI target;
    try {
      target = uri.resolve(new URI(IOUrl.toAscii(location)));
    } catch(final URISyntaxException | IllegalArgumentException ex) {
      return null;
    }

    // the target must be an HTTP URI, and https must not be downgraded to http
    final String scheme = scheme(target);
    if(!scheme.equals("http") && !scheme.equals("https")) return null;
    if(scheme.equals("http") && scheme(uri).equals("https")) return null;

    // 303 turns every method but HEAD into GET, 301 and 302 do the same for POST (RFC 9110)
    final String method = sent.method();
    final boolean get = status == 303 ? !method.equals(Method.HEAD.name()) :
      (status == 301 || status == 302) && method.equals(Method.POST.name());

    // the body and its header fields are dropped; credentials are not sent to another origin
    final boolean origin = UserInfo.sameOrigin(uri, target);
    final HttpRequest.Builder rb = HttpRequest.newBuilder(sent, (name, value) ->
      !(get && bodyField(name)) && (origin || !credentialField(name))).uri(target);
    if(get) rb.GET();
    return rb.build();
  }

  /**
   * Returns the lower-case scheme of a URI.
   * @param uri URI
   * @return scheme (can be empty)
   */
  private static String scheme(final URI uri) {
    final String scheme = uri.getScheme();
    return scheme != null ? scheme.toLowerCase(Locale.ENGLISH) : "";
  }

  /**
   * Checks if a header field describes the request body.
   * @param name field name
   * @return result of check
   */
  private static boolean bodyField(final String name) {
    return name.equalsIgnoreCase(CONTENT_TYPE) || name.equalsIgnoreCase(CONTENT_ENCODING);
  }

  /**
   * Checks if a header field carries credentials.
   * @param name field name
   * @return result of check
   */
  private static boolean credentialField(final String name) {
    return name.equalsIgnoreCase(AUTHORIZATION) || name.equalsIgnoreCase(COOKIE);
  }

  /**
   * Returns a publisher for the request payload. The contents of file-based sources are streamed;
   * other payloads are materialized in advance. Live HTTP response streams are not attached
   * directly, as reading them while sending can deadlock the shared HTTP client.
   * @param src linked resource (can be {@code null})
   * @param request request data
   * @return publisher
   * @throws IOException I/O exception
   */
  private static BodyPublisher publisher(final String src, final Request request)
      throws IOException {
    IO io = null;
    if(src != null) {
      io = IO.get(src);
    } else if(request.payload.size() == 1 &&
        request.payload.get(0) instanceof final B64IOLazy bin && !bin.isCached() &&
        Checks.all(request.payloadAtts.entrySet(), att ->
          att.getKey().equals(SerializerOptions.MEDIA_TYPE.name()) &&
          Payload.binary(new MediaType(att.getValue())))) {
      io = bin.input();
    }
    return io instanceof final IOFile file ?
      BodyPublishers.ofFile(file.file().toPath()) :
      BodyPublishers.ofByteArray(payload(request));
  }

  /**
   * Sets the content type of the HTTP request.
   * @param rb HTTP request builder
   * @param request request data
   */
  private static void setContentType(final HttpRequest.Builder rb, final Request request) {
    String ct = request.headers.get(CONTENT_TYPE);
    if(ct == null) {
      // no header: @media-type of <http:body/> is considered
      ct = request.payloadAtts.get(SerializerOptions.MEDIA_TYPE.name());
      if(request.isMultipart) ct = Strings.concat(ct, "; ", BOUNDARY, "=", request.boundary());
    } else if(request.isMultipart && new MediaType(ct).parameter(BOUNDARY) == null) {
      // multipart header without boundary: append the generated boundary
      ct = Strings.concat(ct, "; ", BOUNDARY, "=", request.boundary());
    }
    rb.header(CONTENT_TYPE, ct);
  }

  /**
   * Returns the query exception for a failed HTTP exchange.
   * @param ex I/O exception
   * @param info input info (can be {@code null})
   * @return query exception
   */
  public static QueryException error(final IOException ex, final InputInfo info) {
    return ex instanceof HttpTimeoutException || ex instanceof SocketTimeoutException ?
      HC_TIMEOUT.get(info).cause(ex) :
      HC_ERROR_X.get(info, ex);
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
    final String uri = href.length == 0 ? request.href : Token.string(href);
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
   * @param resources query resources
   * @return client
   */
  private static HttpClient client(final Request request, final QueryResources resources) {
    // redirects are followed by this class, not by the JDK client
    return request.cookies ? resources.index(HttpClients.class).get(false) : IOUrl.client(false);
  }

  /**
   * Returns the authentication headers.
   * @param auth authorization string (can be {@code null})
   * @return values
   */
  public static EnumMap<AuthParam, String> authHeaders(final String auth) {
    final ArrayList<EnumMap<AuthParam, String>> list = challenges(auth);
    return list.isEmpty() ? new EnumMap<>(AuthParam.class) : list.get(0);
  }

  /**
   * Returns the authentication schemes and parameters of a header with one or more challenges.
   * @param header header value (can be {@code null})
   * @return values of the single challenges
   */
  public static ArrayList<EnumMap<AuthParam, String>> challenges(final String header) {
    final ArrayList<EnumMap<AuthParam, String>> list = new ArrayList<>();
    if(header == null) return list;

    EnumMap<AuthParam, String> values = null;
    for(final String field : splitFields(header)) {
      String param = field.trim();
      if(param.isEmpty()) continue;

      // a token that is not followed by "=" starts a new challenge
      final int pl = param.length();
      int t = 0;
      while(t < pl && param.charAt(t) != ' ' && param.charAt(t) != '=') t++;
      final String rest = param.substring(t).trim();
      if(!rest.startsWith("=")) {
        values = new EnumMap<>(AuthParam.class);
        values.put(SCHEME, param.substring(0, t));
        list.add(values);
        param = rest;
      }
      if(values == null) continue;

      final String[] kv = Strings.split(param, '=', 2);
      if(kv.length == 2) {
        final AuthParam r = Enums.get(AuthParam.class, kv[0].trim());
        if(r != null) values.put(r, unquote(kv[1].trim()));
      }
    }
    return list;
  }

  /**
   * Returns the value of a token or quoted string.
   * @param value value
   * @return unquoted value
   */
  private static String unquote(final String value) {
    final int vl = value.length();
    if(vl < 2 || value.charAt(0) != '"' || value.charAt(vl - 1) != '"') return value;
    final StringBuilder sb = new StringBuilder();
    for(int v = 1; v < vl - 1; v++) {
      final char ch = value.charAt(v);
      sb.append(ch == '\\' && v + 2 < vl ? value.charAt(++v) : ch);
    }
    return sb.toString();
  }

  /**
   * Returns a quoted string.
   * @param value value
   * @return quoted string
   */
  public static String quote(final String value) {
    return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
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
      if(ch == '\\' && quoted && s + 1 < sl) {
        // escaped character inside a quoted string
        sb.append(ch).append(string.charAt(++s));
      } else if(ch == ',' && !quoted) {
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
