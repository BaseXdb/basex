package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnDefaultLanguage extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) {
    return new Str(Formatter.EN, AtomType.LANGUAGE);
  }
}
