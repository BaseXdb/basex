package org.basex.query.func.web;

import java.net.*;
import java.nio.charset.StandardCharsets;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebEncodeUrl extends WebFn {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final String value = toString(arg(0), qc);
    return Str.get(URLEncoder.encode(value, StandardCharsets.UTF_8));
  }
}
