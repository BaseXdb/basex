package org.basex.query.func.web;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.net.*;
import java.nio.charset.StandardCharsets;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class WebDecodeUrl extends WebFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String value = toString(arg(0), qc);

    final byte[] decoded;
    try {
      decoded = token(URLDecoder.decode(value, StandardCharsets.UTF_8));
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      throw WEB_INVALID2_X.get(info, ex.getLocalizedMessage());
    }

    final int cp = XMLToken.invalid(decoded);
    if(cp != -1) throw WEB_INVALID1_X.get(info, cp);
    return Str.get(decoded);
  }
}
