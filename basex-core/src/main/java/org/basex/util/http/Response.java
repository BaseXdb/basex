package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.Version;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.Function;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * {@code <http:response/>} element.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class Response {
  /** Input information (can be {@code null}). */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;
  /** Query resources (can be {@code null}). */
  private final QueryResources resources;
  /** Function that requested the response. */
  private final FuncDefinition definition;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param options main options
   */
  public Response(final InputInfo info, final MainOptions options) {
    this(info, options, null);
  }

  /**
   * Constructor for lazy retrieval of response bodies.
   * @param info input info (can be {@code null})
   * @param options main options
   * @param resources query resources
   */
  public Response(final InputInfo info, final MainOptions options,
      final QueryResources resources) {
    this(info, options, resources, Function._HTTP_SEND_REQUEST.definition());
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param options main options
   * @param resources query resources (can be {@code null})
   * @param definition function that requests the response
   */
  public Response(final InputInfo info, final MainOptions options,
      final QueryResources resources, final FuncDefinition definition) {
    this.info = info;
    this.options = options;
    this.resources = resources;
    this.definition = definition;
  }

  /**
   * Constructs http:response element and reads HTTP response content.
   * @param response HTTP response
   * @param mode representation of the response body
   * @param mtype media type provided by the user (can be {@code null})
   * @param xml options for parsing XML bodies (can be {@code null})
   * @return result sequence of http:response and content items
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public Value getResponse(final HttpResponse<InputStream> response, final BodyMode mode,
      final String mtype, final MainOptions xml) throws IOException, QueryException {

    // construct <http:response/>
    final int status = response.statusCode();
    final FBuilder root = FElem.build(Q_HTTP_RESPONSE).ns();
    root.attr(Q_STATUS, status).attr(Q_MESSAGE, IOUrl.reason(status));
    final String href = href(response, null);
    if(href != null) root.attr(Q_HREF, href);
    if(response.version() != null) {
      root.attr(Q_VERSION, response.version() == Version.HTTP_2 ? "HTTP/2" : "HTTP/1.1");
    }

    headers(response, (name, values) -> {
      for(final String value : values) {
        root.node(FElem.build(Q_HTTP_HEADER).attr(Q_NAME, name).attr(Q_VALUE, value));
      }
    });

    // add payload elements and contents
    final ResponseBody parsed = body(response, mode, mtype, null, xml, href);
    root.node(element(parsed));
    final ItemList items = new ItemList().add((Item) null);
    if(mode != BodyMode.NONE) items.add(parsed.values());

    return items.set(0, root.finish()).value();
  }

  /**
   * Constructs a response record and reads HTTP response content.
   * @param response HTTP response
   * @param mode representation of the response body
   * @param charset character encoding of the body (can be {@code null})
   * @param xml options for parsing XML bodies (can be {@code null})
   * @return response record
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public XQMap getRecord(final HttpResponse<InputStream> response, final BodyMode mode,
      final String charset, final MainOptions xml) throws IOException, QueryException {

    final String href = href(response, "");

    final MapBuilder headers = new MapBuilder();
    headers(response, (name, values) -> {
      final TokenList list = new TokenList();
      for(final String value : values) list.add(value);
      headers.put(name, StrSeq.get(list));
    });

    final XQMap fields = headers.map();
    try {
      final ResponseBody parsed = body(response, mode, null, charset, xml, href);
      return record(response, href, fields, parsed.values());
    } catch(final QueryException ex) {
      if(ex.error() != HC_PARSE_X) throw ex;
      // the response is supplied as error value, with the body that 'binary' would return
      throw HTTP_PARSE_X.get(info, ex.getLocalizedMessage()).
        value(record(response, href, fields, ex.value()));
    }
  }

  /**
   * Returns the URI of a response.
   * @param response HTTP response
   * @param fallback value to be returned if the response has no URI (can be {@code null})
   * @return URI
   */
  private static String href(final HttpResponse<InputStream> response, final String fallback) {
    final URI uri = response.uri();
    return uri != null ? IOUrl.stripUserInfo(uri.toString()) : fallback;
  }

  /**
   * Passes the header fields of a response to a consumer. Field names are converted to lower
   * case, pseudo-headers are skipped.
   * @param response HTTP response
   * @param consumer consumer for field name and values
   * @throws QueryException query exception
   */
  private static void headers(final HttpResponse<InputStream> response,
      final QueryBiConsumer<String, List<String>> consumer) throws QueryException {
    for(final Entry<String, List<String>> entry : response.headers().map().entrySet()) {
      final String name = entry.getKey();
      // names are case-insensitive, and lower-case in HTTP/2
      if(name != null && !name.startsWith(":")) {
        consumer.accept(name.toLowerCase(Locale.ENGLISH), entry.getValue());
      }
    }
  }

  /**
   * Returns a response record.
   * @param response HTTP response
   * @param href URI of the response
   * @param headers response headers
   * @param body response body
   * @return record
   */
  private static XQMap record(final HttpResponse<InputStream> response, final String href,
      final XQMap headers, final Value body) {
    return XQMap.get(Records.HTTP_RESPONSE.get(), Itr.get(response.statusCode()), headers, body,
      Str.get(href), Str.get(response.version() == Version.HTTP_2 ? "2" : "1.1"));
  }

  /**
   * Reads and parses the response body.
   * @param response HTTP response
   * @param mode representation of the response body
   * @param mtype media type provided by the user (can be {@code null})
   * @param charset character encoding of the body (can be {@code null})
   * @param xml options for parsing XML bodies (can be {@code null})
   * @param href URI of the response (can be {@code null})
   * @return parsed body
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private ResponseBody body(final HttpResponse<InputStream> response, final BodyMode mode,
      final String mtype, final String charset, final MainOptions xml, final String href)
      throws IOException, QueryException {

    final HttpHeaders headers = response.headers();
    final MediaType type = mtype != null ? new MediaType(mtype) :
      headers.firstValue(CONTENT_TYPE).map(MediaType::new).orElse(MediaType.TEXT_PLAIN);
    final String encoding = headers.firstValue(CONTENT_ENCODING).orElse("");
    final TempFiles temp = resources != null ? resources.index(TempFiles.class) : null;

    final boolean binary = mode == BodyMode.BINARY ||
      mode == BodyMode.PARSE && Payload.binary(type);
    if(binary && resources != null &&
        !"0".equals(headers.firstValue(CONTENT_LENGTH).orElse(""))) {
      // binary result: skip retrieval of response body, return lazy item
      final InputStream is = response.body();
      resources.add(is);
      final ResponseBody parsed = new ResponseBody();
      parsed.type = type;
      parsed.value = new B64HttpLazy(href, is, encoding, temp, definition);
      return parsed;
    }
    try(InputStream is = response.body()) {
      return new Payload(is, mode, charset, info, options).xmlOptions(xml).
        parse(type, encoding, temp);
    }
  }

  /**
   * Returns the body element of a parsed response body.
   * @param body parsed body
   * @return body element
   */
  private static FNode element(final ResponseBody body) {
    final FBuilder elem;
    if(body.boundary != null) {
      elem = FElem.build(Q_HTTP_MULTIPART).attr(Q_BOUNDARY, body.boundary);
      for(final ResponseBody part : body.parts) {
        for(final Entry<String, String> header : part.headers) {
          elem.node(FElem.build(Q_HTTP_HEADER).attr(Q_NAME, header.getKey()).
            attr(Q_VALUE, header.getValue()));
        }
        elem.node(FElem.build(Q_HTTP_BODY).attr(Q_MEDIA_TYPE, part.type));
      }
    } else {
      elem = FElem.build(Q_HTTP_BODY);
    }
    return elem.attr(Q_MEDIA_TYPE, body.type.type()).finish();
  }
}
