package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfVariables extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final List<Var> vars = cc.vs().vars;
    final QNmSet names = new QNmSet();
    final Expr[] varRefs = new Expr[vars.size()];
    for(int i = 0; i < vars.size(); ++i) {
      final Var var = vars.get(i);
      names.add(var.name);
      varRefs[i] = new VarRef(info, var);
    }
    return new VarHandler(info, names, varRefs);
  }

  /**
   * Function implementation for logging defined variables.
   */
  private static class VarHandler extends Arr {
    /** Names of variables. */
    final QNmSet names;

    /**
     * Constructor.
     * @param info input info
     * @param names variable names
     * @param varRefs variable references
     */
    protected VarHandler(final InputInfo info, final QNmSet names, final Expr... varRefs) {
      super(info, SeqType.EMPTY_SEQUENCE_Z, varRefs);
      this.names = names;
    }

    @Override
    public Value value(final QueryContext qc) throws QueryException {
      final TokenBuilder tb = new TokenBuilder().add(QueryText.DEBUG_LOCAL).add(':');
      int i = 0;
      for(final QNm name : names) {
        final Value value = exprs[i++].value(qc);
        if(value != null) tb.add(Prop.NL).add("  $").add(name).add(" := ").add(value);
      }
      FnTrace.trace(token(tb.toString()), EMPTY, qc);
      return Empty.VALUE;
    }

    @Override
    public boolean has(final Flag... flags) {
      for(final Flag flag : flags) if(flag == Flag.NDT) return true;
      return super.has(flags);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
      return copyType(new VarHandler(info, names, copyAll(cc, vm, args())));
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("prof:variables").params(exprs);
    }
  }
}
