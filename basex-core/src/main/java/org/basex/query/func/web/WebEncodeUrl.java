package org.basex.query.func.web;

import java.io.*;
import java.net.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebEncodeUrl extends WebFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      return Str.get(URLEncoder.encode(Token.string(toToken(exprs[0], qc)), Strings.UTF8));
    } catch(final UnsupportedEncodingException ex) {
      // no error should be raised (UTF8 is always supported)
      throw Util.notExpected(ex);
    }
  }
}
