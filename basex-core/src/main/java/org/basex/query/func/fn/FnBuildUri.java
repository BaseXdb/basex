package org.basex.query.func.fn;

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
public final class FnBuildUri extends FnParseUri {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap parts = toMap(arg(0), qc);
    final UriOptions options = toOptions(arg(1), new UriOptions(), qc);

    final TokenBuilder uri = new TokenBuilder();
    final String scheme = get(parts, SCHEME, qc);
    final Value hierarchical = parts.get(Str.get(HIERARCHICAL));
    final boolean hrrchcl = hierarchical.isEmpty() ? !NON_HIERARCHICAL.contains(scheme) :
      toBoolean(hierarchical, qc);

    if(!scheme.isEmpty()) {
      uri.add(scheme);
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
      int a = 0;
      for(final Item segment : segments) {
        if(a++ != 0) uri.add("/");
        final byte[] sgmnt = toToken(segment, qc);
        uri.add(hrrchcl ? encodeUri(sgmnt, UriEncoder.PATH) : sgmnt);
      }
    } else {
      uri.add(get(parts, PATH, qc));
    }

    final Value qp = parts.get(Str.get(QUERY_PARAMETERS)), query = parts.get(Str.get(QUERY));
    if(!qp.isEmpty()) {
      final TokenBuilder tmp = new TokenBuilder();
      toMap(qp, qc).apply((key, value) -> {
        final byte[] k = encodeUri(toToken(key), UriEncoder.QUERY);
        for(final Item item : value) {
          tmp.add(tmp.isEmpty() ? "?" : "&");
          if(k.length != 0) tmp.add(k).add('=');
          tmp.add(encodeUri(toToken(item), UriEncoder.QUERY));
        }
      });
      uri.add(tmp);
    } else if(!query.isEmpty()) {
      uri.add('?').add(encodeUri(toToken(query, qc), UriEncoder.QUERY));
    }

    final String fragment = get(parts, FRAGMENT, qc);
    if(!fragment.isEmpty()) uri.add('#').add(encodeUri(token(fragment), UriEncoder.FRAGMENT));

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
