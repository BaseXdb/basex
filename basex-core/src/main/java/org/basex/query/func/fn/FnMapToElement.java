package org.basex.query.func.fn;

import java.io.*;

import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnMapToElement extends PlanFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = arg(0).value(qc);
    final ElementsOptions options = options(1, ElementsOptions::new, qc);
    if(value.isEmpty()) return Empty.VALUE;

    final MapToElement converter = new MapToElement(options, uris(qc, sc()), qc.shared,
        qc.context.options, info);
    final NodeHandler handler = new NodeHandler("", false);
    try {
      converter.convert(value, handler);
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
    return handler.root();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(1, ElementsOptions::new, cc);
    return this;
  }
}
