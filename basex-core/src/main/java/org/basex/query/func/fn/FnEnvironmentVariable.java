package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnEnvironmentVariable extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final String value = System.getenv(toString(arg(0), qc));
    return value != null ? Str.get(value) : Empty.VALUE;
  }
}
