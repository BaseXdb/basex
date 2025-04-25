package org.basex.query.func.fn;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnUnparsedText extends FnUnparsedTextAvailable {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item source = arg(0).atomItem(qc, info);
    return source.isEmpty() ? Empty.VALUE :
      (Str) parse(source, false, arg(1), QueryError.INVCHARS_X, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    return expr != this ? expr : super.opt(cc);
  }

  @Override
  Str parse(final TextInput ti, final Object options, final QueryContext qc) throws IOException {
    return Str.get(ti.content());
  }
}
