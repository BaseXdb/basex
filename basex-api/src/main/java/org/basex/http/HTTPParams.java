package org.basex.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Bundles parameters of an HTTP request.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class HTTPParams {
  /** HTTP Context. */
  private final HTTPContext http;
  /** Parameter map. */
  private Map<String, String[]> map;
  /** Query parameters. */
  private Map<String, Value> query;
  /** Form parameters. */
  private Map<String, Value> form;
  /** Content body. */
  private IOContent content;

  /**
   * Returns an immutable map with all query parameters.
   * @param http HTTP context
   */
  HTTPParams(final HTTPContext http) {
    this.http = http;
  }

  /**
   * Returns the query parameters as map.
   * @return map
   * @throws IOException I/O exception
   */
  public Map<String, String[]> map() throws IOException {
    try {
      if(map == null) map = http.req.getParameterMap();
      return map;
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
      final MediaType mt = http.contentType();
      if(mt.is(MediaType.MULTIPART_FORM_DATA)) {
        // convert multipart parameters encoded in a form
        addMultipart(mt, options);
      } else if(mt.is(MediaType.APPLICATION_X_WWW_FORM_URLENCODED)) {
        // convert URL-encoded parameters
        addURLEncoded();
      }
    }
    return form;
  }

  /**
   * Returns query parameters.
   * @return query parameters
   * @throws IOException I/O exception
   */
  public Map<String, Value> query() throws IOException {
    if(query == null) {
      query = new HashMap<>();
      for(final Entry<String, String[]> entry : map().entrySet()) {
        final String key = entry.getKey();
        final String[] values = entry.getValue();
        final ValueBuilder vb = new ValueBuilder();
        for(final String v : values) vb.add(new Atm(v));
        query.put(key, vb.value());
      }
    }
    return query;
  }

  /**
   * Returns the cached body.
   * @return value
   * @throws IOException I/O exception
   */
  public IOContent body() throws IOException {
    if(content == null) {
      content = new IOContent(new BufferInput(http.req.getInputStream()).content());
      content.name(http.method + IO.XMLSUFFIX);
    }
    return content;
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Adds multipart form-data from the passed on request body.
   * @param type media type
   * @param options main options
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void addMultipart(final MediaType type, final MainOptions options)
      throws QueryException, IOException {

    try(final InputStream is = body().inputStream()) {
      final HttpPayload hp = new HttpPayload(is, true, null, options);
      final HashMap<String, Value> mp = hp.multiForm(type);
      for(final Entry<String, Value> entry : mp.entrySet()) {
        form.put(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Adds URL-encoded parameters from the passed on request body.
   * @throws IOException I/O exception
   */
  private void addURLEncoded() throws IOException {
    for(final String nv : Strings.split(body().toString(), '&')) {
      final String[] parts = Strings.split(nv, '=', 2);
      if(parts.length == 2) {
        final Atm i = new Atm(URLDecoder.decode(parts[1], Strings.UTF8));
        final String k = parts[0];
        final Value v = form.get(k);
        form.put(k, v == null ? i : new ValueBuilder().add(v).add(i).value());
      }
    }
  }
}
