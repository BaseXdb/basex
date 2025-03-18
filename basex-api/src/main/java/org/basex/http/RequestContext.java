package org.basex.http;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;

import jakarta.servlet.http.*;

/**
 * Request of an HTTP or WebSocket connection.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestContext {
  /** HTTP servlet request. */
  public final HttpServletRequest request;
  /** Query parameters. */
  private XQMap values;
  /** Form parameters. */
  private XQMap form;
  /** Headers. */
  private XQMap headers;
  /** Content body. */
  private IOContent body;
  /** Query string. */
  private final String query;

  /**
   * Returns an immutable map with all query parameters.
   * @param request HTTP request
   */
  public RequestContext(final HttpServletRequest request) {
    this.request = request;
    this.query = request.getQueryString();
  }

  /**
   * Returns the query headers.
   * @return headers
   * @throws QueryException query exception
   */
  public XQMap headers() throws QueryException {
    if(headers == null) {
      final MapBuilder map = new MapBuilder();
      for(final String name : Collections.list(request.getHeaderNames())) {
        final TokenList list = new TokenList(1);
        for(final String value : Collections.list(request.getHeaders(name))) list.add(value);
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
      // combine query parameters of previous and current request
      final MapBuilder mb = new MapBuilder();
      final RequestContext forward = (RequestContext) request.getAttribute(HTTPText.FORWARD);
      if(forward != null && forward.query != null) addParams(forward.query, mb);
      final String string = request.getQueryString();
      if(string != null) addParams(string, mb);
      values = mb.map();
    }
    return values;
  }

  /**
   * Returns the form parameters.
   * @param options main options
   * @return parameters
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public XQMap formValues(final MainOptions options) throws QueryException, IOException {
    if(form == null) {
      final MediaType mt = HTTPConnection.mediaType(request);
      if(mt.is(MediaType.MULTIPART_FORM_DATA)) {
        // convert multipart parameters encoded in a form
        try(InputStream is = body().inputStream()) {
          form = new Payload(is, true, null, options).multiForm(mt);
        }
      } else if(mt.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
        // convert URL-encoded parameters
        final MapBuilder mb = new MapBuilder();
        addParams(body().toString(), mb);
        form = mb.map();
      } else {
        form = XQMap.empty();
      }
    }
    return form;
  }

  /**
   * Returns the cached body.
   * @return value
   * @throws IOException I/O exception
   */
  public IOContent body() throws IOException {
    if(body == null) {
      final RequestContext forward = (RequestContext) request.getAttribute(HTTPText.FORWARD);
      if(forward != null && forward.body != null) {
        body = forward.body;
      } else {
        body = new IOContent(BufferInput.get(request.getInputStream()).content());
      }
    }
    return body;
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Adds URL-decoded parameters to the specified map.
   * @param params parameters
   * @param mb map builder
   * @throws QueryException query exception
   */
  private static void addParams(final String params, final MapBuilder mb) throws QueryException {
    final ItemObjectMap<ItemList> map = new ItemObjectMap<>();
    for(final String param : Strings.split(params, '&')) {
      final String[] parts = Strings.split(param, '=', 2);
      if(parts.length == 2) {
        final ItemList list = map.computeIfAbsent(Str.get(parts[0]), ItemList::new);
        list.add(Atm.get(XMLToken.decodeUri(parts[1])));
      }
    }
    // create final map
    for(final Item key : map) mb.put(key, map.get(key).value());
  }
}
