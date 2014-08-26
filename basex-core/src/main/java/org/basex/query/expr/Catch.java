package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Catch clause.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Error QNames. */
  public static final QNm[] NAMES = {
    create(E_CODE), create(E_DESCRIPTION), create(E_VALUE), create(E_MODULE),
    create(E_LINE_NUMBER), create(E_COLUM_NUMBER), create(E_ADDITIONAL)
  };
  /** Error types. */
  public static final SeqType[] TYPES = {
    SeqType.QNM, SeqType.STR_ZO, SeqType.ITEM_ZM, SeqType.STR_ZO,
    SeqType.ITR_ZO, SeqType.ITR_ZO, SeqType.ITEM_ZM
  };

  /** Error variables. */
  private final Var[] vars;
  /** Supported codes. */
  private final NameTest[] codes;

  /**
   * Constructor.
   * @param info input info
   * @param codes supported error codes
   * @param vars variables to be bound
   */
  public Catch(final InputInfo info, final NameTest[] codes, final Var[] vars) {
    super(info, null);
    this.vars = vars;
    this.codes = codes;
  }

  @Override
  public Catch compile(final QueryContext qc, final VarScope scp) {
    try {
      expr = expr.compile(qc, scp);
      seqType = expr.seqType();
    } catch(final QueryException qe) {
      expr = FnError.get(qe, expr.seqType());
    }
    return this;
  }

  /**
   * Returns the value of the caught expression.
   * @param qc query context
   * @param ex thrown exception
   * @return resulting item
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc, final QueryException ex) throws QueryException {
    int i = 0;
    final byte[] io = ex.file() == null ? EMPTY : token(ex.file());
    final Value val = ex.value();
    for(final Value v : new Value[] { ex.qname(),
        Str.get(ex.getLocalizedMessage()), val == null ? Empty.SEQ : val,
        Str.get(io), Int.get(ex.line()), Int.get(ex.column()),
        Str.get(ex.getMessage().replaceAll("\r\n?", "\n")) }) {
      qc.set(vars[i++], v, info);
    }
    return qc.value(expr);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Var[] vrs = new Var[NAMES.length];
    for(int i = 0; i < vrs.length; i++)
      vrs[i] = scp.newLocal(qc, NAMES[i], TYPES[i], false);
    final Catch ctch = new Catch(info, codes.clone(), vrs);
    for(int i = 0; i < vars.length; i++) vs.put(vars[i].id, ctch.vars[i]);
    ctch.expr = expr.copy(qc, scp, vs);
    return ctch;
  }

  @Override
  public Catch inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex) {
    try {
      final Expr sub = expr.inline(qc, scp, var, ex);
      if(sub == null) return null;
      expr = sub;
    } catch(final QueryException qe) {
      expr = FnError.get(qe, seqType);
    }
    return this;
  }

  /**
   * Returns this clause as an inlineable expression.
   * @param ex caught exception
   * @param qc query context
   * @param scp variable scope
   * @return equivalent expression
   * @throws QueryException query exception during inlining
   */
  Expr asExpr(final QueryException ex, final QueryContext qc, final VarScope scp)
      throws QueryException {

    if(expr.isValue()) return expr;
    int i = 0;
    Expr e = expr;
    for(final Value v : values(ex)) {
      final Expr e2 = e.inline(qc, scp, vars[i++], v);
      if(e2 != null) e = e2;
      if(e.isValue()) break;
    }
    return e;
  }

  /**
   * Returns all error values.
   * @param ex exception
   * @return values
   */
  public static Value[] values(final QueryException ex) {
    final byte[] io = ex.file() == null ? EMPTY : token(ex.file());
    final Value val = ex.value();
    return new Value[] { ex.qname(),
        Str.get(ex.getLocalizedMessage()), val == null ? Empty.SEQ : val,
        Str.get(io), Int.get(ex.line()), Int.get(ex.column()),
        Str.get(ex.getMessage().replaceAll("\r\n?", "\n")) };
  }

  /**
   * Checks if one of the specified errors match the thrown error.
   * @param qe thrown error
   * @return result of check
   */
  boolean matches(final QueryException qe) {
    final QNm code = qe.qname();
    for(final NameTest c : codes) if(c.eq(code)) return true;
    return false;
  }

  @Override
  public String toString() {
    return "catch * { " + expr + " }";
  }

  /**
   * Creates an error QName with the specified name.
   * @param n name
   * @return QName
   */
  private static QNm create(final byte[] n) {
    return new QNm(concat(ERR, COLON, n), ERRORURI);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Var v : vars) if(!visitor.declared(v)) return false;
    return visitAll(visitor, expr);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }
}
