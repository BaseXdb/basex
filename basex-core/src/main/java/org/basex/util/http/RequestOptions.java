package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.http.HTTPText.*;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.web.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Request options of the HTTP Client Module 2.0.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestOptions extends Options {
  /** Send the items of the body as the parts of a multipart body. */
  public static final BooleanOption MULTIPART = new BooleanOption("multipart", false);
  /** Query parameters. */
  public static final ValueOption QUERY = new ValueOption("query",
    MapType.get(BasicType.STRING, Types.ANY_ATOMIC_TYPE_ZM).seqType());
  /** HTTP header fields. */
  public static final ValueOption HEADERS = new ValueOption("headers",
    MapType.get(BasicType.STRING, Types.STRING_ZM).seqType());
  /** Authentication. */
  public static final ValueOption AUTH = new ValueOption("auth",
    Records.HTTP_AUTH.get().seqType(Occ.ZERO_OR_ONE));
  /** Options for the implicit parsing of response bodies. */
  public static final ValueOption PARSE_OPTIONS = new ValueOption("parse-options",
    Records.HTTP_PARSE_OPTIONS.get().seqType(Occ.ZERO_OR_ONE));
  /** Character encoding of the response body. */
  public static final StringOption ENCODING = new StringOption("encoding");
  /** Representation of the response body. */
  public static final EnumOption<BodyMode> RESPONSE_BODY =
    new EnumOption<>("response-body", BodyMode.PARSE);
  /** Follow redirects: boolean, or maximum number of redirects. */
  public static final ValueOption REDIRECTS = new ValueOption("redirects",
    ChoiceItemType.get(BasicType.BOOLEAN, BasicType.INTEGER).seqType(), Bln.TRUE);
  /** Use a cookie store. */
  public static final BooleanOption COOKIES = new BooleanOption("cookies", false);
  /** Proxy server. */
  public static final StringOption PROXY = new StringOption("proxy");
  /** Verify the certificate of an HTTPS server. */
  public static final BooleanOption VERIFY = new BooleanOption("verify", true);
  /** Certificates for HTTPS connections. */
  public static final ValueOption CERTIFICATES = new ValueOption("certificates",
    Types.MAP_ZO);
  /** Timeout in seconds. */
  public static final ValueOption TIMEOUT = new ValueOption("timeout",
    BasicType.DECIMAL.seqType(Occ.ZERO_OR_ONE));

  /**
   * Returns a request with the specified method and the assigned options.
   * @param method HTTP method
   * @param info input info (can be {@code null})
   * @return request
   * @throws QueryException query exception
   */
  public Request request(final String method, final InputInfo info) throws QueryException {
    final Request request = new Request();
    request.method = method;
    request.bodyMode = get(RESPONSE_BODY);
    final String encoding = get(ENCODING);
    if(encoding != null) {
      final String error = Strings.checkEncoding(encoding);
      if(error != null) throw HTTP_INVALID_OPTION_X.get(info, error);
      request.charset = Strings.normEncoding(encoding, false);
    }
    if(get(PARSE_OPTIONS) instanceof final XQMap map) request.parseOptions = map;
    request.cookies = get(COOKIES);
    request.verify = get(VERIFY);
    if(get(CERTIFICATES) instanceof final XQMap map)
      request.certificates = Certificates.parse(map, info);

    // a zero-length string requests a direct connection
    final String proxy = get(PROXY);
    if(proxy != null) request.proxy(proxy, info);

    final Item redirect = get(REDIRECTS).itemAt(0);
    if(redirect instanceof final Itr itr) request.redirects(itr.itr(), info);
    else if(!redirect.bool(info)) request.redirects = 0;

    final Value timeout = get(TIMEOUT);
    if(!timeout.isEmpty()) request.timeout(timeout.itemAt(0).dbl(info), info);

    if(get(QUERY) instanceof final XQMap map) {
      final byte[] query = query(map, info);
      if(query.length != 0) request.query = Token.string(query);
    }
    if(get(HEADERS) instanceof final XQMap map) headers(map, request.headers, info);
    if(get(AUTH) instanceof final XQMap map) auth(map, request, info);

    final String error = request.invalid();
    if(error != null) throw HTTP_INVALID_OPTION_X.get(info, error);
    return request;
  }

  /**
   * Assigns a body to a request.
   * @param value body
   * @param request request
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void body(final Value value, final Request request, final QueryContext qc,
      final InputInfo info) throws QueryException {

    if(get(MULTIPART)) {
      multipart(value, request, qc, info);
    } else if(value.size() > 1) {
      throw HTTP_INVALID_BODY_X.get(info, "Single item expected as request body");
    } else if(!value.isEmpty()) {
      body(value.itemAt(0), request.headers, request.payloadAtts, request.payload, info);
    }
  }

  /**
   * Assigns the parts of a multipart body to a request.
   * @param value parts
   * @param request request
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void multipart(final Value value, final Request request, final QueryContext qc,
      final InputInfo info) throws QueryException {

    if(value.isEmpty()) throw HTTP_INVALID_BODY_X.get(info, "No parts specified");

    // a supplied media type must be a multipart type; otherwise, form data is sent
    final String header = request.headers.get(CONTENT_TYPE);
    if(header != null) {
      if(!new MediaType(header).isMultipart())
        throw HTTP_INVALID_OPTION_X.get(info, "No multipart media type: " + header);
    } else {
      request.payloadAtts.put(SerializerOptions.MEDIA_TYPE.name(),
        MediaType.MULTIPART_FORM_DATA.toString());
    }
    request.isMultipart = true;

    final SeqType st = Records.HTTP_PART.get().seqType();
    for(final Item item : value) {
      final XQMap map = (XQMap) st.coerce(item, qc, info);
      final Part part = new Part();
      final Value headers = map.get(Str.get("headers"));
      if(headers instanceof final XQMap hdr) headers(hdr, part.headers, info);
      final Value body = map.get(Str.get("body"));
      if(!body.isEmpty()) body(body.itemAt(0), part.headers, part.attributes, part.contents, info);
      request.parts.add(part);
    }
  }

  /**
   * Assigns a single body. The media type is derived from the type of the item, unless a
   * Content-Type header was supplied.
   * @param item body
   * @param fields header fields
   * @param atts body attributes
   * @param contents body contents
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void body(final Item item, final Map<String, String> fields,
      final Map<String, String> atts, final ItemList contents, final InputInfo info)
      throws QueryException {

    final String header = fields.get(CONTENT_TYPE);
    final MediaType type = header != null ? new MediaType(header) : null;

    // a map with a form media type is form-encoded, all other maps and arrays are sent as JSON
    if(type != null && type.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED) &&
        item instanceof final XQMap map) {
      atts.put(SerializerOptions.MEDIA_TYPE.name(), header);
      atts.put(SerializerOptions.METHOD.name(), SerialMethod.TEXT.toString());
      contents.add(Str.get(query(map, info)));
      return;
    }

    // the type of the item, not the media type, determines how the body is serialized
    final String media, method;
    if(item instanceof Bin) {
      media = MediaType.APPLICATION_OCTET_STREAM.toString();
      method = null;
    } else if(item instanceof Str || item instanceof Atm || item instanceof Uri) {
      media = MediaType.TEXT_PLAIN + "; charset=" + Strings.UTF8;
      method = SerialMethod.TEXT.toString();
    } else if(item instanceof XNode) {
      media = MediaType.APPLICATION_XML.toString();
      method = SerialMethod.XML.toString();
    } else if(item instanceof XQMap || item instanceof XQArray) {
      media = MediaType.APPLICATION_JSON.toString();
      method = SerialMethod.JSON.toString();
    } else {
      throw HTTP_INVALID_BODY_X.get(info, "Invalid type of request body: " + item.type);
    }
    atts.put(SerializerOptions.MEDIA_TYPE.name(), header != null ? header : media);
    if(method != null) atts.put(SerializerOptions.METHOD.name(), method);
    contents.add(item);
  }

  /**
   * Assigns header fields.
   * @param map header fields
   * @param fields target map
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void headers(final XQMap map, final Map<String, String> fields,
      final InputInfo info) throws QueryException {

    map.forEach((key, value) -> {
      final String name = Token.string(key.string(info));
      if(value.isEmpty()) {
        // an empty sequence suppresses the field, including one added by the implementation
        fields.put(name, null);
      } else {
        // repeated values are merged (RFC 9110, RFC 6265)
        final String sep = name.equalsIgnoreCase(COOKIE) ? "; " : ", ";
        final StringBuilder sb = new StringBuilder();
        for(final Item item : value) {
          if(!sb.isEmpty()) sb.append(sep);
          sb.append(Token.string(item.string(info)));
        }
        fields.put(name, sb.toString());
      }
    });
  }

  /**
   * Assigns credentials to a request.
   * @param map credentials
   * @param request request
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void auth(final XQMap map, final Request request, final InputInfo info)
      throws QueryException {

    request.username = string(map, "username", info);
    request.password = string(map, "password", info);

    final String method = string(map, "method", info);
    if(method != null) request.authMethod(method, info);
    final Value preemptive = map.get(Str.get("preemptive"));
    request.sendAuthorization = !preemptive.isEmpty() && preemptive.itemAt(0).bool(info);
  }

  /**
   * Returns a query string without its leading separator.
   * @param map query parameters
   * @param info input info (can be {@code null})
   * @return query string
   * @throws QueryException query exception
   */
  private static byte[] query(final XQMap map, final InputInfo info) throws QueryException {
    // the helper starts the query string with a question mark
    final TokenBuilder tb = WebFn.createUrl(Token.EMPTY, map, '&', info);
    return tb.isEmpty() ? Token.EMPTY : tb.delete(0, 1).finish();
  }

  /**
   * Returns the string value of a record field.
   * @param map record (can be {@code null})
   * @param field field name
   * @param info input info (can be {@code null})
   * @return value, or {@code null} if the field is absent
   * @throws QueryException query exception
   */
  static String string(final XQMap map, final String field, final InputInfo info)
      throws QueryException {
    final Value value = map != null ? map.get(Str.get(field)) : null;
    return value == null || value.isEmpty() ? null :
      Token.string(value.itemAt(0).string(info));
  }
}
