package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Int;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Catch clause.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Catch extends Single {
  /** Error QName. */
  private static final QNm NS = new QNm(ERR, ERRORURI);
  /** Error QNames. */
  private static final QNm[] QNM = {
    create(ECODE), create(EDESC), create(EVALUE),
    create(EMODULE), create(ELINENUM), create(ECOLNUM)
  };
  /** Error types. */
  private static final SeqType[] TYPES = {
    SeqType.QNM, SeqType.STR_ZO, SeqType.ITEM_ZM,
    SeqType.STR_ZO, SeqType.ITR_ZO, SeqType.ITR_ZO
  };

  /** Supported codes. */
  private final QNm[] codes;
  /** Error variables. */
  private final Var[] vars = new Var[6];

  /**
   * Constructor.
   * @param ii input info
   * @param c supported error codes
   * @param ctx query context
   */
  public Catch(final InputInfo ii, final QNm[] c, final QueryContext ctx) {
    super(ii, null);
    codes = c;
    for(int i = 0; i < QNM.length; i++) vars[i] =
      Var.create(ctx, null, QNM[i], TYPES[i]);
  }

  @Override
  public Catch comp(final QueryContext ctx) throws QueryException {
    final int s = prepare(ctx);
    super.comp(ctx);
    finish(s, ctx);
    return this;
  }

  /**
   * Returns the value of the caught expression.
   * @param ctx query context
   * @param ex thrown exception
   * @return resulting item
   * @throws QueryException query exception
   */
  Value value(final QueryContext ctx, final QueryException ex)
      throws QueryException {

    final byte[] cd = token(ex.code());
    if(!find(cd)) return null;

    final int s = prepare(ctx);
    int i = 0;
    final byte[] io = ex.file() == null ? EMPTY : token(ex.file().path());
    final Value val = ex.value();
    for(final Value v : new Value[] { new QNm(cd, ERRORURI),
        Str.get(ex.getLocalizedMessage()), val == null ? Empty.SEQ : val,
        Str.get(io), Int.get(ex.col()), Int.get(ex.line()) }) {
      vars[i++].bind(v, ctx);
    }
    final Value ir = ctx.value(expr);
    finish(s, ctx);
    return ir;
  }

  /**
   * Prepares the catch construction.
   * @param ctx query context
   * @return original size of variable stack
   * @throws QueryException query exception
   */
  public int prepare(final QueryContext ctx) throws QueryException {
    ctx.ns.add(NS, null);
    final int s = ctx.vars.size();
    for(final Var v : vars) ctx.vars.add(v);
    return s;
  }

  /**
   * Finishes the catch construction.
   * @param s original size of variable stack
   * @param ctx query context
   */
  public void finish(final int s, final QueryContext ctx) {
    ctx.vars.reset(s);
    ctx.ns.delete(NS);
  }

  /**
   * Finds iterator.
   * @param err error code
   * @return result of check
   */
  private boolean find(final byte[] err) {
    for(final QNm c : codes) if(c == null || eq(c.ln(), err)) return true;
    return false;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || super.uses(u);
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
}
