package org.basex.query.func.fn;

import java.nio.charset.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnDecodeFromUri extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Str.get(new String(XMLToken.decodeUri(toZeroToken(arg(0), qc), true), StandardCharsets.UTF_8));
  }
}
