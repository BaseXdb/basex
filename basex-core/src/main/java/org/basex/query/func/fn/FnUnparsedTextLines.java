package org.basex.query.func.fn;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author Christian Gruen
 * @author BaseX Team, BSD License
 */
public final class FnUnparsedTextLines extends FnUnparsedTextAvailable {
  @Override
  public boolean nl() {
    return true;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return doc(qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // overwrite implementation of superclass
    return value(qc).item(qc, ii);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr href = arg(0);
    return href.seqType().zero() ? href : super.opt(cc);
  }

  @Override
  Value parse(final TextInput ti, final Options options, final QueryContext qc) throws IOException {
    final NewlineInput ni = (NewlineInput) ti;
    final TokenList tl = new TokenList();
    final TokenBuilder tb = new TokenBuilder();
    while(ni.readLine(tb)) {
      qc.checkStop();
      tl.add(tb.next());
    }
    return StrSeq.get(tl);
  }
}
