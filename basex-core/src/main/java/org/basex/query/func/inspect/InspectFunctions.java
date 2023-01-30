package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class InspectFunctions extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // returns all functions from the query context
    if(exprs.length < 1) {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final StaticFunc sf : qc.functions.funcs()) {
        vb.add(Functions.getUser(sf, qc, sf.sc, info));
      }
      return vb.value(this);
    }

    // URI specified: compile module and return all newly added functions
    final IOContent content = toContent(toString(exprs[0], qc), qc);
    Value funcs = qc.resources.functions(content.path());
    if(funcs != null) return funcs;

    // cache existing functions
    final HashSet<StaticFunc> old = new HashSet<>();
    Collections.addAll(old, qc.functions.funcs());

    try {
      qc.parse(content.toString(), content.path());
      qc.functions.compileAll(new CompileContext(qc, true));
    } catch(final QueryException ex) {
      throw INSPECT_PARSE_X.get(info, ex);
    }

    // collect new functions
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final StaticFunc sf : qc.functions.funcs()) {
      if(!old.contains(sf)) vb.add(Functions.getUser(sf, qc, sf.sc, info));
    }
    funcs = vb.value(this);
    qc.resources.addFunctions(content.path(), funcs);
    return funcs;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    if(exprs.length < 1) cc.qc.functions.compileAll(cc);
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock((String) null) && super.accept(visitor);
  }
}
