package org.basex.query.func.fn;

import static org.basex.query.func.fn.FnParseUri.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
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
      final Value hierarchical = parts.get(Str.get(HIERARCHICAL), info);
      final boolean hrrchcl = hierarchical.isEmpty() || toBoolean(hierarchical, qc);
      uri.add(hrrchcl ? "://" : ":");
      if(scheme.equals(FILE) && options.get(UriOptions.UNC_PATH)) uri.add("//");
    }

    String userinfo = get(parts, USERINFO, qc);
    if(userinfo.contains(":") && !options.get(UriOptions.ALLOW_DEPRECATED_FEATURES)) userinfo = "";

    String port = string(toZeroToken(parts.get(Str.get(PORT), info), qc));
    if(omitPort(port, scheme, options)) port = "";

    final String host = get(parts, HOST, qc), authority = get(parts, AUTHORITY, qc);
    if(!(userinfo.isEmpty() && host.isEmpty() && port.isEmpty())) {
      if(!userinfo.isEmpty()) uri.add(userinfo).add('@');
      uri.add(host);
      if(!port.isEmpty()) uri.add(':').add(port);
    } else if(!authority.isEmpty()) {
      uri.add(authority);
    }

    final Value segments = parts.get(Str.get(PATH_SEGMENTS), info);
    final XQArray sgmnts = segments.isEmpty() ? XQArray.empty() : toArray(segments, qc);
    final long as = sgmnts.arraySize();
    if(as > 0) {
      final String sep = options.get(UriOptions.PATH_SEPARATOR);
      for(int a = 0; a < as; a++) {
        if(a != 0) uri.add(sep);
        uri.add(encodeUri(toToken(sgmnts.get(a), qc), false));
      }
    } else {
      uri.add(get(parts, PATH, qc));
    }

    final Value queries = parts.get(Str.get(QUERY_SEGMENTS), info);
    final XQArray qurs = queries.isEmpty() ? XQArray.empty() : toArray(queries, qc);
    final long qs = qurs.arraySize();
    if(qs > 0) {
      final TokenBuilder query = new TokenBuilder();
      final String sep = options.get(UriOptions.QUERY_SEPARATOR);
      for(int q = 0; q < qs; q++) {
        final XQMap map = toMap(qurs.get(q), qc);
        final byte[] key = encodeUri(token(get(map, KEY, qc)), false);
        final byte[] value = encodeUri(token(get(map, VALUE, qc)), false);
        final int kl = key.length, vl = value.length;
        if(kl != 0 || vl != 0) {
          query.add(query.isEmpty() ? "?" : sep);
          query.add(key).add(kl != 0 && vl != 0 ? "=" : "").add(value);
        }
      }
      uri.add(query.finish());
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
  protected final String get(final XQMap map, final String key, final QueryContext qc)
      throws QueryException {
    final Value value = map.get(Str.get(key), info);
    return value.isEmpty() ? "" : string(toToken(value, qc));
  }
}
