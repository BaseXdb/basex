package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.FTNodeIter;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTSelect extends FTExpr {
  /** Term list. */
  TokenList term = new TokenList();
  /** Input token. */
  Tokenizer ft;
  /** Position lists. */
  IntList[] pos = new IntList[1];
  /** Number of position lists. */
  int size;
  /** Filter array. */
  private final FTFilter[] filter;

  /**
   * Constructor.
   * @param e expression
   * @param f filters
   */
  public FTSelect(final FTExpr e, final FTFilter... f) {
    super(e);
    filter = f;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    for(final FTFilter f : filter) {
      f.comp(ctx);
      f.sel = this;
    }
    return super.comp(ctx);
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    final FTSelect tmp = ctx.ftselect;
    ctx.ftselect = this;
    init(ctx.ftitem);
    final Item it = expr[0].iter(ctx).next();
    ctx.ftselect = tmp;

    final double s = it.score();
    if(s == 0 || !filter(ctx)) return score(0);

    if(tmp.expr[0] != null) tmp.addPos(pos, size, term);
    ctx.ftd = pos;
    return score(s);
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  boolean filter(final QueryContext ctx) throws QueryException {
    for(final FTFilter f : filter) if(!f.filter(ctx)) return false;
    return true;
  }

  /**
   * Adds position values to existing values.
   * @param il IntList[] with position values
   * @param ils int number of tokens in query
   * @param tl TokenList with tokens
   */
  private void addPos(final IntList[] il, final int ils, final TokenList tl) {
    final IntList[] iln = new IntList[size + ils];
    System.arraycopy(pos, 0, iln, 0, size);
    System.arraycopy(il, 0, iln, size, ils);
    pos = iln;
    size += ils;
    for (int i = 0; i < tl.size; i++) term.add(tl.list[i]);
  }

  /**
   * Sets the position values and the number of tokens.
   * @param il IntList[] with position values
   * @param ils int number of tokens in query
   */
  void setPos(final IntList[] il, final int ils) {
    pos = il;
    size = ils;
  }

  /**
   * Adds the specified full-text term and position list. This method is
   * called every time a test in {@link FTOpt#contains} was successful.
   * @param t term to be added
   * @param il positions to be added
   */
  void add(final byte[] t, final IntList il) {
    if(size == pos.length) pos = Array.extend(pos);
    pos[size++] = il;
    term.add(t);
  }

  /**
   * Initializes the select operator. Has to be called before any
   * {@link FTWords} are performed.
   * @param tok tokenizer for source term
   */
  void init(final Tokenizer tok) {
    term.reset();
    size = 0;
    ft = tok;
  }

  /**
   * Evaluates the mild not expression.
   * @return boolean result
   */
  boolean mildNot() {
    for(int i = 1; i < size; i++) {
      for(int j = 0; j < pos[i].size; j++) {
        if(pos[0].contains(pos[i].list[j]))
          return pos[0].list[pos[0].size - 1] > pos[i].list[j]; //false
      }
    }
    return true;
  }

  @Override
  public boolean indexAccessible(final QueryContext ctx,
      final IndexContext ic) throws QueryException {

    // [SG] are all position filters supported by the index?
    return expr[0].indexAccessible(ctx, ic);
  }

  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
      throws QueryException {

    expr[0] = expr[0].indexEquivalent(ctx, ic);
    return new FTSelectIndex(this);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final FTFilter f : filter) f.plan(ser);
    if(expr[0] != null) expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(expr[0] != null) sb.append(expr[0]);
    for(final FTFilter f : filter) sb.append(" " + f);
    return sb.toString();
  }
}
