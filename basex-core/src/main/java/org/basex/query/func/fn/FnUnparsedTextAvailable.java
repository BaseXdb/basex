package org.basex.query.func.fn;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.QueryError.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnUnparsedTextAvailable extends ParseFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item source = arg(0).atomItem(qc, info);
    try {
      return source.isEmpty() ? Bln.FALSE : (Bln) parse(source, false, arg(1), null, qc);
    } catch(final QueryException ex) {
      if(!ex.matches(ErrType.XPTY)) return Bln.FALSE;
      throw ex;
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // pre-evaluate if target is empty
    final Expr source = arg(0);
    if(source == Empty.VALUE) return value(cc.qc);

    // pre-evaluate during dynamic compilation if target is not a remote URL
    if(cc.dynamic && source instanceof Value) {
      input = input(toToken(source.atomItem(cc.qc, info)));
      if(input == null || !(input instanceof IOUrl)) return value(cc.qc);
    }
    return this;
  }

  @Override
  Value parse(final TextInput ti, final Object options, final QueryContext qc) throws IOException {
    try {
      while(ti.read() != -1);
      return Bln.TRUE;
    } catch(final IOException ex) {
      Util.debug(ex);
      return Bln.FALSE;
    }
  }
}
