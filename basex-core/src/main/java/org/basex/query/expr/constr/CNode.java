package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node constructor.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class CNode extends Arr {
  /** Static context. */
  final StaticContext sc;
  /** Computed constructor. */
  final boolean computed;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param computed computed constructor
   * @param seqType sequence type
   * @param exprs expressions
   */
  CNode(final StaticContext sc, final InputInfo info, final SeqType seqType, final boolean computed,
      final Expr... exprs) {
    super(info, seqType, exprs);
    this.sc = sc;
    this.computed = computed;
  }

  @Override
  public abstract Item item(QueryContext qc, InputInfo ii) throws QueryException;

  /**
   * Optimizes the node value.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  final void optValue(final CompileContext cc) throws QueryException {
    simplifyAll(Simplify.STRING, cc);
    if(allAreValues(true) && (exprs.length != 1 || !(exprs[0] instanceof Str))) {
      exprs = new Expr[] { Str.get(atomValue(cc.qc, true)) };
    }
  }

  /**
   * Returns the atomized node value.
   * @param qc query context
   * @return resulting value or {@code null}
   * @param empty return empty string
   * @throws QueryException query exception
   */
  final byte[] atomValue(final QueryContext qc, final boolean empty) throws QueryException {
    TokenBuilder tb = null;
    for(final Expr expr : exprs) {
      boolean space = false;
      final Iter iter = expr.atomIter(qc, info);
      for(Item item; (item = qc.next(iter)) != null;) {
        if(tb == null) tb = new TokenBuilder();
        else if(space) tb.add(' ');
        tb.add(item.string(info));
        space = true;
      }
    }
    return tb != null ? tb.finish() : empty ? Token.EMPTY : null;
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    SeqType st = null;
    // ignore PIs and attributes as values must be normalized
    if(exprs.length == 1 && !(this instanceof CPI || this instanceof CAttr)) {
      final SeqType st1 = exprs[0].seqType();
      if(st1.zeroOrOne() && st1.instanceOf(SeqType.ANY_ATOMIC_TYPE_ZO) && !has(Flag.NDT)) {
        if(mode == Simplify.STRING) {
          st = SeqType.STRING_ZO;
        } else if(mode == Simplify.DATA || mode == Simplify.NUMBER) {
          st = this instanceof CComm ? SeqType.STRING_ZO : SeqType.UNTYPED_ATOMIC_ZO;
        }
      }
    }
    return st != null ? cc.simplify(this, new Cast(cc.sc(), info, exprs[0], st).optimize(cc)) :
      super.simplifyFor(mode, cc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CNS.in(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return !ic.expr.has(Flag.CNS) && super.inlineable(ic);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CNode && computed == ((CNode) obj).computed && super.equals(obj);
  }

  @Override
  public final String description() {
    return Strings.concat(((NodeType) seqType().type).qname().local(), " constructor");
  }

  /**
   * Adds the expression with the specified separator to the query string.
   * @param qs query string builder
   * @param kind node kind
   */
  protected void toString(final QueryString qs, final String kind) {
    if(kind != null) qs.token(kind);
    qs.token("{");
    if(exprs.length > 0) qs.tokens(exprs, SEP, false);
    qs.token(" }");
  }
}
