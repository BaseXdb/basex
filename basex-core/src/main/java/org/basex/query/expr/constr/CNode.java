package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node constructor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class CNode extends Arr {
  /** Computed constructor. */
  final boolean computed;
  /** Results will only be serialized: skip copies of enclosed nodes. */
  boolean skipCopy;
  /** Nested direct constructor (results are adopted by the parent without copying). */
  boolean nested;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param computed computed constructor
   * @param exprs expressions
   */
  CNode(final InputInfo info, final SeqType seqType, final boolean computed,
      final Expr... exprs) {
    super(info, seqType, exprs);
    this.computed = computed;
  }

  @Override
  public abstract Value value(QueryContext qc) throws QueryException;

  /**
   * Marks this constructor as nested direct constructor.
   * @return self reference
   */
  public final CNode nested() {
    nested = true;
    return this;
  }

  /**
   * Assigns the type and the nested flag of this constructor to the specified copy.
   * @param <T> constructor type
   * @param copy copied constructor
   * @return specified copy
   */
  final <T extends CNode> T copyNode(final T copy) {
    copy.nested = nested;
    return copyType(copy);
  }

  /**
   * Optimizes the node value.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  final void optValue(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.STRING, cc);
    if(values(true, cc) && (exprs.length != 1 || !(exprs[0] instanceof Str))) {
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
      boolean more = false;
      final Iter iter = expr.atomIter(qc, info);
      for(Item item; (item = qc.next(iter)) != null;) {
        if(tb == null) tb = new TokenBuilder();
        else if(more) tb.add(' ');
        tb.add(item.string(info));
        more = true;
      }
    }
    return tb != null ? tb.finish() : empty ? Token.EMPTY : null;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // ignore PIs and attributes as values must be normalized
    // single item required: a node with empty content is atomized to an empty string
    SeqType st = null;
    if(exprs.length == 1 && !(this instanceof CPI || this instanceof CAttr)) {
      final SeqType st1 = exprs[0].seqType();
      if(st1.one() && st1.instanceOf(Types.ANY_ATOMIC_TYPE_O) && !has(Flag.NDT)) {
        if(mode.oneOf(Simplify.STRING, Simplify.STRING_VALUE)) {
          st = Types.STRING_O;
        } else if(mode.oneOf(Simplify.DATA, Simplify.NUMBER)) {
          st = this instanceof CComm || this instanceof CNSpace ? Types.STRING_O :
            Types.UNTYPED_ATOMIC_O;
        }
      }
    }
    return cc.simplify(this, st != null ? new Cast(info, exprs[0], st).optimize(cc) : this, mode);
  }

  @Override
  public void skipCopy() {
    // copy-namespaces modes are only applied to copies
    if(preserveNS(info) && inheritNS(info)) {
      skipCopy = true;
      for(final Expr expr : exprs) expr.skipCopy();
    }
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CNS.oneOf(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    // do not change context of constructed nodes:  <x/> ! <y xmlns='y'>{ <x/> }</y>
    return !ic.expr.has(Flag.CNS) && super.inlineable(ic);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final CNode cnode && computed == cnode.computed &&
        nested == cnode.nested && preserveNS(info) == preserveNS(cnode.info) &&
        inheritNS(info) == inheritNS(cnode.info) && super.equals(obj);
  }

  /**
   * Returns the copy-namespaces preserve mode of a static context.
   * @param info input info (can be {@code null})
   * @return result of check
   */
  static boolean preserveNS(final InputInfo info) {
    final StaticContext sc = info != null ? info.sc() : null;
    return sc == null || sc.preserveNS;
  }

  /**
   * Returns the copy-namespaces inherit mode of a static context.
   * @param info input info (can be {@code null})
   * @return result of check
   */
  static boolean inheritNS(final InputInfo info) {
    final StaticContext sc = info != null ? info.sc() : null;
    return sc == null || sc.inheritNS;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, SKIPCOPY, skipCopy ? true : null), exprs);
  }

  @Override
  public final String description() {
    return Strings.concat(seqType().type.kind().description(), " constructor");
  }

  /**
   * Adds the expression with the specified separator to the query string.
   * @param qs query string builder
   * @param kind node kind (can be {@code null})
   */
  protected void toString(final QueryString qs, final String kind) {
    if(kind != null) qs.token(kind);
    qs.token("{");
    if(exprs.length > 0) qs.tokens(exprs, SEP, false);
    qs.token(" }");
  }
}
