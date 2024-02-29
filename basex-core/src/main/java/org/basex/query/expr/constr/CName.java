package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract fragment constructor with a QName argument.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class CName extends CNode {
  /** QName. */
  Expr name;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param computed computed constructor
   * @param name name
   * @param exprs contents
   */
  CName(final InputInfo info, final SeqType seqType, final boolean computed, final Expr name,
      final Expr... exprs) {
    super(info, seqType, computed, exprs);
    this.name = name;
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(name);
    super.checkUp();
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    name = name.compile(cc);
    return super.compile(cc);
  }

  /**
   * Evaluates the name expression as a QName.
   * @param elem element construction
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  final QNm qname(final boolean elem, final QueryContext qc) throws QueryException {
    final Item item = name.atomItem(qc, info);
    final Type type = item.type;
    if(type == AtomType.QNAME) return (QNm) item;

    if(!type.isStringOrUntyped() || type == AtomType.ANY_URI)
      throw STRQNM_X_X.get(info, item.seqType(), item);

    final QNm qnm = qc.shared.parseQName(item.string(info), elem, sc());
    if(qnm != null) return qnm;

    throw INVQNAME_X.get(info, item);
  }

  /**
   * Evaluates the name expression as an NCName.
   * @param empty allow empty name
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  final byte[] ncname(final boolean empty, final QueryContext qc) throws QueryException {
    final Item item = name.atomItem(qc, info);
    if(item.isEmpty()) {
      if(empty) return EMPTY;
    } else {
      final Type type = item.type;
      if(type.isStringOrUntyped() && type != AtomType.ANY_URI) return trim(item.string(info));
    }
    throw STRNCN_X_X.get(info, item.seqType(), item);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return name.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public final boolean has(final Flag... flags) {
    return name.has(flags) || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return name.accept(visitor) && visitAll(visitor, exprs);
  }

  @Override
  public final VarUsage count(final Var var) {
    return name.count(var).plus(super.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = ic.inline(exprs);
    final Expr inlined = name.inline(ic);
    if(inlined != null) {
      name = inlined;
      changed = true;
    }
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public final int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size + name.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CName && name.equals(((CName) obj).name) && super.equals(obj);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), name, exprs);
  }

  @Override
  public final void toString(final QueryString qs, final String kind) {
    qs.token(kind);
    if(name instanceof QNm) qs.token(((QNm) name).internal());
    else qs.brace(name);
    super.toString(qs, null);
  }
}
