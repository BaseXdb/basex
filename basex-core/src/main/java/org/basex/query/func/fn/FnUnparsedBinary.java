package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.io.*;
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
public final class FnUnparsedBinary extends ParseFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item source = arg(0).atomItem(qc, info);
    if(source.isEmpty()) return Empty.VALUE;

    final IO io = input(toToken(source));
    if(io == null) throw INVURL_X.get(info, source);
    if(Strings.contains(io.path(), '#')) throw FRAGID_X.get(info, io);

    return new B64Lazy(io, RESNF_X);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optFirst();
  }

  @Override
  Str parse(final TextInput ti, final Object options, final QueryContext qc) {
    throw Util.notExpected();
  }
}
