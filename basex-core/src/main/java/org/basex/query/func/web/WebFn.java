package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
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
  protected byte[] createUrl(final byte[] url, final XQMap map) throws QueryException {
    final TokenBuilder tb = new TokenBuilder().add(url);
    int c = 0;
    for(final Item key : map.keys()) {
      final byte[] name = key.string(info);
      for(final Item value : map.get(key, info)) {
        tb.add(c++ == 0 ? '?' : '&').add(Token.encodeUri(name, false));
        tb.add('=').add(Token.encodeUri(value.string(info), false));
      }
    }
    return tb.finish();
  }
}
