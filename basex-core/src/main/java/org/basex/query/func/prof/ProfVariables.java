package org.basex.query.func.prof;

import static org.basex.query.func.Function.*;

import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
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
    final Item map = arg(0).item(qc, info);
    final String label = toStringOrNull(arg(1), qc);

    if(!map.isEmpty()) {
      final StringBuilder sb = new StringBuilder();
      toMap(map).forEach((key, value) -> {
        if(value != Empty.UNDEFINED) {
          sb.append(Prop.NL).append("  ").append(key.toJava()).append(" := ").append(value);
        }
      });
      qc.trace(sb.toString(), label.isEmpty() ? QueryText.DEBUG_ASSIGNMENTS : label);
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(!arg(0).seqType().zero()) return this;

    // create map constructor with context value and variable names/references
    final ExprList list = new ExprList();
    if(cc.qc.focus.value != null) {
      list.add(new ContextValue(info)).add(Str.get("."));
    }
    final List<Var> vars = cc.vs().vars;
    final QNmSet names = new QNmSet();
    for(int v = vars.size() - 1; v >= 0; --v) {
      final Var var = vars.get(v);
      if(names.add(var.name)) {
        list.add(new VarRef(info, var)).add(Str.get(Strings.concat('$', var.name.prefixId())));
      }
    }
    return cc.function(_PROF_VARIABLES, info, new CMap(info, list.reverse().finish()));
  }
}
