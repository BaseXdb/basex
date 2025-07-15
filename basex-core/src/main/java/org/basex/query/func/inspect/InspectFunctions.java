package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectFunctions extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // returns all functions from the query context
    if(!defined(0)) {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final StaticFunc sf : qc.functions.funcs()) {
        if(!NSGlobal.reserved(sf.name.uri())) addItems(vb, sf, qc);
      }
      return vb.value(this);
    }

    // URI specified: compile module and return all newly added functions
    final IOContent source = toContent(toString(arg(0), qc), qc);
    Value funcs = qc.resources.functions(source.path());
    if(funcs != null) return funcs;

    // cache existing functions
    final HashSet<StaticFunc> old = new HashSet<>();
    Collections.addAll(old, qc.functions.funcs());

    try {
      qc.parse(source.toString(), source.path());
      qc.functions.compileAll(new CompileContext(qc, true));
    } catch(final QueryException ex) {
      throw INSPECT_PARSE_X.get(info, ex);
    }

    // collect new functions
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final StaticFunc sf : qc.functions.funcs()) {
      if(!old.contains(sf)) addItems(vb, sf, qc);
    }
    funcs = vb.value(this);
    qc.resources.addFunctions(source.path(), funcs);
    return funcs;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    if(!defined(0)) cc.qc.functions.compileAll(cc);
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock((String) null) && super.accept(visitor);
  }

  /**
   * Build function items for all arities of a static function and add them to the given value
   * builder.
   * @param vb value builder
   * @param sf static function
   * @param qc query context
   * @throws QueryException query exception
   */
  private static void addItems(final ValueBuilder vb, final StaticFunc sf, final QueryContext qc)
      throws QueryException {
    for(int a = sf.minArity(); a <= sf.arity(); ++a) {
      final FuncBuilder fb = new FuncBuilder(sf.info, a, true);
      // safe cast (no context dependency, runtime evaluation)
      vb.add((FuncItem) Functions.item(sf, fb, qc, true));
    }
  }
}
