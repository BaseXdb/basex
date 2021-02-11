package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InspectFunctions extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    // about to be updated in a future version
    final ArrayList<StaticFunc> old = new ArrayList<>();
    if(exprs.length > 0) {
      // cache existing functions
      Collections.addAll(old, qc.funcs.funcs());
      try {
        final IO io = checkPath(0, qc);
        qc.parse(Token.string(io.read()), io.path());
        qc.funcs.compileAll(new CompileContext(qc));
      } catch(final IOException ex) {
        throw IOERR_X.get(info, ex);
      }
    }

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final StaticFunc sf : qc.funcs.funcs()) {
      if(!old.contains(sf)) vb.add(Functions.getUser(sf, qc, sf.sc, info));
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    if(exprs.length == 0) cc.qc.funcs.compileAll(cc);
    return this;
  }

  @Override
  public boolean has(final Flag... flags) {
    // do not relocate function, as it introduces new code
    return Flag.NDT.in(flags) && exprs.length == 1 || super.has(flags);
  }
}
