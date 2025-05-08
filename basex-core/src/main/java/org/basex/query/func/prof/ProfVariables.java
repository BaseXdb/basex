package org.basex.query.func.prof;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfVariables extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = arg(0).iter(qc);
    final String label = toStringOrNull(arg(1), qc);

    final StringBuilder sb = new StringBuilder();
    for(Item item; (item = iter.next()) != null;) {
      toMap(item).forEach((key, value) -> {
        if(value != Empty.UNDEFINED) {
          sb.append(Prop.NL).append("  ").append(key.toJava()).append(" := ").append(value);
        }
      });
    }
    qc.trace(label == null || label.isEmpty() ? QueryText.DEBUG_ASSIGNMENTS : label, sb::toString);
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(defined(0)) return this;

    // create single-entry maps with context value and variables
    final ExprList list = new ExprList();
    if(cc.qc.focus.value != null) {
      list.add(cc.function(_MAP_ENTRY, info, Str.get("."), new ContextValue(info)));
    }
    for(final Var var : cc.vs().vars) {
      final Str key = Str.get(Strings.concat('$', var.name.prefixId()));
      list.add(cc.function(_MAP_ENTRY, info, key, new VarRef(info, var)));
    }
    return cc.function(_PROF_VARIABLES, info, List.get(cc, info, list.reverse().finish()), arg(1));
  }
}
