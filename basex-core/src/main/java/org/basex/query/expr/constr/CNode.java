package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node constructor.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * Returns the atomized node value.
   * @param qc query context
   * @return resulting value or {@code null}
   * @throws QueryException query exception
   */
  byte[] atomValue(final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    // empty sequence: empty string
    if(el == 0) return null;
    // single string argument
    if(el == 1 && exprs[0] instanceof Str) return ((Str) exprs[0]).string();

    boolean more = false;
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr expr : exprs) {
      more = false;
      final Iter iter = expr.atomIter(qc, info);
      for(Item item; (item = qc.next(iter)) != null;) {
        if(more) tb.add(' ');
        tb.add(item.string(info));
        more = true;
      }
    }
    return more ? tb.finish() : null;
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
  protected void plan(final QueryString qs, final String kind) {
    if(kind != null) qs.token(kind);
    qs.token("{");
    if(exprs.length > 0) qs.tokens(exprs, SEP, false);
    qs.token(" }");
  }
}
