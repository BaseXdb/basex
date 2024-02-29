package org.basex.query.func.fn;

import static org.basex.query.func.fn.FnParseUri.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnBuildUri extends FnJsonDoc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap parts = toMap(arg(0), qc);
    final UriOptions options = toOptions(arg(1), new UriOptions(), false, qc);

    final TokenBuilder uri = new TokenBuilder();
    final String scheme = get(parts, SCHEME, qc);

    if(!scheme.isEmpty()) {
      uri.add(scheme);
      final Value hierarchical = parts.get(Str.get(HIERARCHICAL));
      final boolean hrrchcl = hierarchical.isEmpty() || toBoolean(hierarchical, qc);
      uri.add(hrrchcl ? "://" : ":");
      if(scheme.equals(FILE) && options.get(UriOptions.UNC_PATH)) uri.add("//");
    }

    String userinfo = get(parts, USERINFO, qc);
    if(userinfo.contains(":") && !options.get(UriOptions.ALLOW_DEPRECATED_FEATURES)) userinfo = "";

    String port = string(toZeroToken(parts.get(Str.get(PORT)), qc));
    if(omitPort(port, scheme, options)) port = "";

    final String host = get(parts, HOST, qc), authority = get(parts, AUTHORITY, qc);
    if(!(userinfo.isEmpty() && host.isEmpty() && port.isEmpty())) {
      if(!userinfo.isEmpty()) uri.add(userinfo).add('@');
      uri.add(host);
      if(!port.isEmpty()) uri.add(':').add(port);
    } else if(!authority.isEmpty()) {
      uri.add(authority);
    }

    final Value segments = parts.get(Str.get(PATH_SEGMENTS));
    if(!segments.isEmpty()) {
      final String sep = options.get(UriOptions.PATH_SEPARATOR);
      int a = 0;
      for(final Item segment : segments) {
        if(a++ != 0) uri.add(sep);
        uri.add(encodeUri(toToken(segment, qc), false));
      }
    } else {
      uri.add(get(parts, PATH, qc));
    }

    final Value qp = parts.get(Str.get(QUERY_PARAMETERS));
    if(!qp.isEmpty()) {
      final TokenBuilder query = new TokenBuilder();
      final String sep = options.get(UriOptions.QUERY_SEPARATOR);
      toMap(qp, qc).apply((key, value) -> {
        for(final Item item : value) {
          query.add(query.isEmpty() ? "?" : sep);
          query.add(encodeUri(toToken(key), false)).add('=');
          query.add(encodeUri(toToken(item), false));
        }
      });
      uri.add(query);
    }
    final String fragment = get(parts, FRAGMENT, qc);
    if(!fragment.isEmpty()) uri.add('#').add(fragment);

    return Str.get(uri.finish());
  }

  /**
   * Evaluates an expression to a string.
   * @param map map with URI parts
   * @param key key
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private String get(final XQMap map, final String key, final QueryContext qc)
      throws QueryException {
    final Value value = map.get(Str.get(key));
    return value.isEmpty() ? "" : string(toToken(value, qc));
  }
}
