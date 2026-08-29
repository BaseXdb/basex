package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileLineSeparator extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) {
    return Str.get(Prop.NL);
  }
}
