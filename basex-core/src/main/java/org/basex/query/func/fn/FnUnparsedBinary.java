package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnUnparsedBinary extends ParseFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String source = toStringOrNull(arg(0), qc);
    return source == null ? Empty.VALUE : new B64Lazy(toIO(source, false), RESWHICH_X);
  }

  @Override
  Str parse(final TextInput ti, final Options options, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  protected Options options(final QueryContext qc) throws QueryException {
    return new Options();
  }
}
