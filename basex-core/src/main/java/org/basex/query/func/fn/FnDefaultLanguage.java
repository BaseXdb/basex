package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDefaultLanguage extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) {
    return Str.get(Formatter.EN, BasicType.LANGUAGE);
  }
}
