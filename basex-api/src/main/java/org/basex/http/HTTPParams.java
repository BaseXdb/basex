package org.basex.http;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Bundles parameters of an HTTP request.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class HTTPParams {
  /** HTTP Connection. */
  private final HTTPConnection conn;
  /** Query parameters with string values. */
  private Map<String, String[]> strings;
  /** Query parameters with XQuery values. */
  private Map<String, Value> values;
  /** Form parameters. */
  private Map<String, Value> form;
  /** Content body. */
  private IOContent content;

  /**
   * Returns an immutable map with all query parameters.
   * @param conn HTTP connection
   */
  HTTPParams(final HTTPConnection conn) {
    this.conn = conn;
  }

  /**
   * Returns the query parameters as string map.
   * @return map
   * @throws IOException I/O exception
   */
  public Map<String, String[]> stringMap() throws IOException {
    try {
      if(strings == null) strings = conn.req.getParameterMap();
      return strings;
    } catch(final IllegalStateException ex) {
      // may be caused by too large input (#884)
      throw new IOException(ex);
    }
  }

  /**
   * Binds form parameters.
   * @param options main options
   * @return parameters
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public Map<String, Value> form(final MainOptions options) throws QueryException, IOException {
    if(form == null) {
      form = new HashMap<>();
      final MediaType mt = conn.contentType();
      if(mt.is(MediaType.MULTIPART_FORM_DATA)) {
        // convert multipart parameters encoded in a form
        addMultipart(mt, options, form);
      } else if(mt.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
        // convert URL-encoded parameters
        addURLEncoded(form);
      }
    }
    return form;
  }

  /**
   * Returns query parameters.
   * @return query parameters
   * @throws IOException I/O exception
   */
  public Map<String, Value> map() throws IOException {
    if(values == null) {
      values = new HashMap<>();
      stringMap().forEach((key, value) -> {
        final ItemList items = new ItemList();
        for(final String string : value) items.add(new Atm(string));
        values.put(key, items.value());
      });
    }
    return values;
  }

  /**
   * Returns the cached body.
   * @return value
   * @throws IOException I/O exception
   */
  public IOContent body() throws IOException {
    if(content == null) {
      content = new IOContent(new BufferInput(conn.req.getInputStream()).content());
    }
    return content;
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Adds multipart form-data from the passed on request body.
   * @param type media type
   * @param options main options
   * @param map form parameters
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void addMultipart(final MediaType type, final MainOptions options,
      final Map<String, Value> map) throws QueryException, IOException {

    try(InputStream is = body().inputStream()) {
      final HttpPayload hp = new HttpPayload(is, true, null, options);
      hp.multiForm(type).forEach(map::put);
    }
  }

  /**
   * Adds URL-encoded parameters from the passed on request body.
   * @param map form parameters
   * @throws IOException I/O exception
   */
  private void addURLEncoded(final Map<String, Value> map) throws IOException {
    for(final String param : Strings.split(body().toString(), '&')) {
      final String[] parts = Strings.split(param, '=', 2);
      if(parts.length == 2) {
        final Atm atm = new Atm(URLDecoder.decode(parts[1], Strings.UTF8));
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
