package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
abstract class WebFn extends StandardFunc {
  /**
   * Returns a URL for the specified main URL and the query parameters.
   * @param url url
   * @param map query parameters
   * @return generated url
   * @throws QueryException query exception
   */
  protected byte[] createUrl(final byte[] url, final Map map) throws QueryException {
    final TokenBuilder tb = new TokenBuilder(url);
    int c = 0;
    for(final Item key : map.keys()) {
      final byte[] name = key.string(info);
      for(final Item value : map.get(key, info)) {
        tb.add(c++ == 0 ? '?' : '&');
        tb.add(Token.uri(name, false)).add('=').add(Token.uri(value.string(info), false));
      }
    }
    return tb.finish();
  }
}
