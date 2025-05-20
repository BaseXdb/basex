package org.basex.query.func.fn;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.QueryError.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnUnparsedTextAvailable extends ParseFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      return Bln.get(doc(qc) == Bln.TRUE);
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
      try {
        input = toIO(toString(source, cc.qc), false);
        if(!(input instanceof IOUrl)) return value(cc.qc);
      } catch(final QueryException ex) {
        Util.debug(ex);
      }
    }
    return this;
  }

  @Override
  Value parse(final TextInput ti, final Options options, final QueryContext qc) throws IOException {
    try {
      while(ti.read() != -1);
      return Bln.TRUE;
    } catch(final IOException ex) {
      Util.debug(ex);
      return Bln.FALSE;
    }
  }

  @Override
  public final QueryError error() {
    return QueryError.RESINPUT_X;
  }

  @Override
  protected final Options options(final QueryContext qc) throws QueryException {
    Expr options = arg(1);
    final ParseOptions po = new ParseOptions();
    if(options instanceof final XQMap map) {
      toOptions(map, po, qc);
    } else {
      po.set(ParseOptions.ENCODING, toStringOrNull(options, qc));
    }
    return po;
  }
}
