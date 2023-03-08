package org.basex.query.func.fn;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnUnparsedTextAvailable extends Parse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return unparsedText(qc, true, true);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // pre-evaluate if target is empty
    final Expr href = arg(0);
    if(href == Empty.VALUE) return value(cc.qc);

    // pre-evaluate during dynamic compilation if target is not a remote URL
    if(cc.dynamic && href instanceof Value) {
      input = input(toToken(href.atomItem(cc.qc, info)));
      if(input == null || !(input instanceof IOUrl)) return value(cc.qc);
    }
    return this;
  }
}
