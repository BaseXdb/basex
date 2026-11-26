package org.basex.http;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * Request of an HTTP or WebSocket connection.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestContext implements RequestScope {
  /** Name of the attribute or parameter for the maximum form size (adopted from Jetty). */
  private static final String MAX_FORM = "org.eclipse.jetty.server.Request.maxFormContentSize";
  /** Default maximum form size (adopted from Jetty). */
  private static final long MAX_FORM_DEFAULT = 200_000;

  /** HTTP servlet request (can be {@code null} if the request is not accessible). */
  public final HttpServletRequest request;
  /** Request state. */
  private final RequestState state;
  /** Query parameters (can be {@code null}). */
  private XQMap values;
  /** Form parameters (can be {@code null}). */
  private XQMap form;
  /** Headers (can be {@code null}). */
  private XQMap headers;
  /** Content body (can be {@code null}). */
  private IO body;
  /** Error raised while reading the body (can be {@code null}). */
  private IOException failure;
  /** Temporary files of the spilled body and form parameters. */
  private final TempFiles temp = new TempFiles();

  /**
   * Constructor for HTTP connections.
   * @param request HTTP request
   */
  public RequestContext(final HttpServletRequest request) {
    this.request = request;
    state = new LiveRequest(request);
  }

  /**
   * Constructor for request contexts without live servlet request.
   * @param state request state
   */
  private RequestContext(final RequestState state) {
    request = null;
    this.state = state;
  }

  @Override
  public RequestContext detach() {
    if(request == null) return this;
    final RequestContext rc = new RequestContext(FrozenRequest.of(state));
    rc.values = values;
    rc.headers = headers;
    // temporary files are deleted with the live request: share in-memory data only
    if(temp.isEmpty()) {
      rc.form = form;
      rc.body = body;
    }
    return rc;
  }

  /**
   * Returns the request state.
   * @return state
   */
  public RequestState state() {
    return state;
  }

  /**
   * Returns the query headers.
   * @return headers
   * @throws QueryException query exception
   */
  public XQMap headers() throws QueryException {
    if(headers == null) {
      final MapBuilder map = new MapBuilder();
      for(final String name : state.headerNames()) {
        final TokenList list = new TokenList(1);
        for(final String value : state.headers(name)) list.add(value);
        map.put(name, StrSeq.get(list));
      }
      headers = map.map();
    }
    return headers;
  }

  /**
   * Returns the query parameters as strings.
   * @return parameters
   * @throws QueryException query exception
   */
  public Map<String, String[]> queryStrings() throws QueryException {
    final Map<String, String[]> strings = new HashMap<>();
    queryValues().forEach((key, value) -> {
      final StringList list = new StringList(value.size());
      for(final Item item : value) list.add(((Atm) item).toJava());
      strings.put(((Str) key).toJava(), list.finish());
    });
    return strings;
  }

  /**
   * Returns the query parameters.
   * @return parameters
   * @throws QueryException query exception
   */
  public XQMap queryValues() throws QueryException {
    if(values == null) {
      final String string = state.query();
      values = string != null ? params(Token.token(string)) : XQMap.empty();
    }
    return values;
  }

  /**
   * Returns the form parameters.
   * @param options main options
   * @return parameters
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public XQMap formValues(final MainOptions options) throws QueryException, IOException {
    if(form == null) {
      // no live request (WebSocket connection, detached job): no form body
      final MediaType mt = request != null ? state.mediaType() : null;
      if(mt != null && mt.is(MediaType.MULTIPART_FORM_DATA)) {
        // convert multipart parameters encoded in a form
        try(InputStream is = body().inputStream()) {
          form = new Payload(is, BodyMode.PARSE, null, options).multiForm(mt, temp);
        }
      } else if(mt != null && mt.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
        // convert URL-encoded parameters
        form = params(body().read());
      } else {
        form = XQMap.empty();
      }
    }
    return form;
  }

  /**
   * Returns the cached body.
   * @return body
   * @throws IOException I/O exception
   */
  public IO body() throws IOException {
    if(failure != null) throw failure;
    if(body == null) {
      // no live request (WebSocket connection, detached job): empty body
      if(request == null) return new IOContent(Token.EMPTY);
      final InputStream is = request.getInputStream();
      // binary and multipart bodies are consumed as streams: spill large ones to disk
      final MediaType mt = state.mediaType();
      try {
        if(Payload.binary(mt) || mt.isMultipart()) {
          body = SpillOutput.read(is, temp);
        } else if(mt.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
          body = new IOContent(readForm(is));
        } else {
          body = new IOContent(BufferInput.get(is).content());
        }
      } catch(final IOException ex) {
        // remember the error: the stream may have been partially consumed
        failure = ex;
        throw ex;
      }
    }
    return body;
  }

  /**
   * Reads a URL-encoded form body, rejecting bodies that exceed the maximum form size.
   * @param is input stream
   * @return body
   * @throws IOException I/O exception
   */
  private byte[] readForm(final InputStream is) throws IOException {
    final long max = maxForm();
    if(request.getContentLengthLong() <= max) {
      final byte[] bytes = is.readNBytes((int) Math.min(max, Array.MAX_SIZE - 1) + 1);
      if(bytes.length <= max) return bytes;
    }
    throw HTTPStatus.FORM_TOO_LARGE_X.get(max);
  }

  /**
   * Returns the maximum size of form bodies, as configured for the servlet container.
   * @return maximum size
   */
  private long maxForm() {
    final ServletContext sc = request.getServletContext();
    // init parameter first: Jetty 12 always answers the attribute with its own default
    Object value = sc.getInitParameter(MAX_FORM);
    if(value == null) value = sc.getAttribute(MAX_FORM);
    if(value == null) return MAX_FORM_DEFAULT;
    final long max = Strings.toLong(value.toString());
    if(max == Long.MIN_VALUE) return MAX_FORM_DEFAULT;
    // negative values disable the limit, as in Jetty
    return max < 0 ? Long.MAX_VALUE : max;
  }

  /**
   * Discards the temporary files of the body and the form parameters.
   */
  public void close() {
    temp.close();
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Returns a map with URL-decoded parameters.
   * @param params URL-encoded parameters
   * @return parameters
   * @throws QueryException query exception
   */
  private static XQMap params(final byte[] params) throws QueryException {
    final ItemObjectMap<ItemList> map = new ItemObjectMap<>();
    for(final byte[] param : Token.split(params, new byte[] { '&' }, false)) {
      final int eq = Token.indexOf(param, '='), end = eq == -1 ? param.length : eq;
      final Str name = Str.get(XMLToken.decodeUri(Token.substring(param, 0, end), true));
      final byte[] value = XMLToken.decodeUri(Token.substring(param, end + 1), true);
      map.computeIfAbsent(name, ItemList::new).add(Atm.get(value));
    }
    // create final map
    final MapBuilder mb = new MapBuilder();
    for(final Item key : map) mb.put(key, map.get(key).value());
    return mb.map();
  }
}
