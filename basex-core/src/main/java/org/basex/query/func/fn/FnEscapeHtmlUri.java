package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnEscapeHtmlUri extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    return Str.get(encodeUri(toZeroToken(arg(0), qc), UriEncoder.ESCAPE));
  }
}
