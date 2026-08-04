package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringSoundex extends StringFn {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    return str(Soundex.encode(toToken(arg(0), qc)));
  }
}
