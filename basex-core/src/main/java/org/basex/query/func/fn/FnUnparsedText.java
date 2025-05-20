package org.basex.query.func.fn;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnUnparsedText extends FnUnparsedTextAvailable {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return (Str) doc(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    return expr != this ? expr : super.opt(cc);
  }

  @Override
  Str parse(final TextInput ti, final Options options, final QueryContext qc) throws IOException {
    return Str.get(ti.content());
  }
}
