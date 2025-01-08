package org.basex.http;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
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

  /** Query parameters with string values. */
  private Map<String, String[]> strings;
  /** Query parameters with XQuery values. */
  private Map<String, Value> values;
  /** Form parameters. */
  private Map<String, Value> form;
  /** Headers. */
  private XQMap headers;
  /** Content body. */
  private IOContent body;

  /**
   * Returns an immutable map with all query parameters.
   * @param request HTTP request
   */
  public RequestContext(final HttpServletRequest request) {
    this.request = request;
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
   * Returns the original query string.
   * @return query string
   */
  public String queryString() {
    return request.getQueryString();
  }

  /**
   * Returns the query parameters as strings.
   * @return map
   */
  public Map<String, String[]> queryStrings() {
    if(strings == null) {
      strings = new HashMap<>();
      queryValues().forEach((key, value) -> {
        final StringList list = new StringList(value.size());
        for(final Item item : value) list.add(((Atm) item).toJava());
        strings.put(key, list.finish());
      });
    }
    return strings;
  }

  /**
   * Returns query parameters as XQuery values.
   * @return map
   */
  public Map<String, Value> queryValues() {
    if(values == null) {
      values = new HashMap<>();
      final String string = request.getQueryString();
      if(string != null) addParams(string, values);
    }
    return values;
  }

  /**
   * Returns form parameters as XQuery Values.
   * @param options main options
   * @return parameters
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public Map<String, Value> formValues(final MainOptions options)
      throws QueryException, IOException {

    if(form == null) {
      final MediaType mt = HTTPConnection.mediaType(request);
      form = new HashMap<>();
      if(mt.is(MediaType.MULTIPART_FORM_DATA)) {
        // convert multipart parameters encoded in a form
        try(InputStream is = body().inputStream()) {
          form.putAll(new Payload(is, true, null, options).multiForm(mt));
        }
      } else if(mt.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
        // convert URL-encoded parameters
        addParams(body().toString(), form);
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
   * Populates a map with URL-decoded parameters.
   * @param params query parameters
   * @param map to populate
   */
  private static void addParams(final String params, final Map<String, Value> map) {
    for(final String param : Strings.split(params, '&')) {
      final String[] parts = Strings.split(param, '=', 2);
      if(parts.length == 2) {
        final Atm atm = Atm.get(XMLToken.decodeUri(parts[1]));
        map.merge(parts[0], atm, (value1, value2) -> {
          final ItemList items = new ItemList();
          for(final Item item : value1) items.add(item);
          for(final Item item : value2) items.add(item);
          return items.value();
        });
      }
    }
  }
}
