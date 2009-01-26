package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.iter.FTNodeIter;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.BoolList;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTPos extends FTExpr {
  /** Units. */
  public enum FTUnit {
    /** Word unit. */      WRD,
    /** Sentence unit. */  SEN,
    /** Paragraph unit. */ PAR
  }

  /** Ordered flag. */
  public boolean ordered;
  /** Start flag. */
  public boolean start;
  /** End flag. */
  public boolean end;
  /** Entire content flag. */
  public boolean content;
  /** Same flag. */
  public boolean same;
  /** Different flag. */
  public boolean different;

  /** Distance unit. */
  public FTUnit dunit;
  /** Window unit. */
  public FTUnit wunit;
  /** Same/different unit. */
  public FTUnit sdunit;
  /** Term list. */
  public TokenList term = new TokenList();
  /** Input token. */
  public FTTokenizer ft;
  /** Window. */
  public Expr window;
  /** Distance occurrences. */
  public Expr[] dist;
  /** Weight. */
  public Expr weight;

  /** Position list. */
  private IntList[] pos = new IntList[0];
  /** Number of position lists. */
  private int size;

  /**
   * Constructor.
   * @param e expression
   */
  public FTPos(final FTExpr e) {
    super(e);
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    if(window != null) window = window.comp(ctx);

    if(dist != null) {
      for(int d = 0; d < dist.length; d++) dist[d] = dist[d].comp(ctx);
    }

    if(weight != null) {
      weight = weight.comp(ctx);
      if(weight.i()) {
        final Item wg = (Item) weight;
        if(!wg.n()) Err.or(XPTYPENUM, WEIGHT, weight);
        if(wg.dbl() < 0 || wg.dbl() > 1000) Err.or(FTWEIGHT, wg);
      }
    }
    return super.comp(ctx);
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    final FTPos tmp = ctx.ftpos;
    ctx.ftpos = this;
    init(ctx.ftitem);
    final Item it = expr[0].iter(ctx).next();
    ctx.ftpos = tmp;

    if(tmp != null) {
      final int os = tmp.term.size;
      for(int i = 0; i < term.size; i++) tmp.term.add(term.list[i]);

      final IntList[] il = new IntList[term.size + os];
      System.arraycopy(pos, 0, il, 0, term.size);
      System.arraycopy(tmp.pos, 0, il, term.size, os);
      tmp.setPos(il, il.length);
      ctx.ftd = il;
    } else {
      ctx.ftd = pos;
    }

    final double s = it.score();
    if(s == 0 || !filter(ctx)) return score(0);

    // calculate weight
    final double d = weight != null ? checkDbl(ctx.iter(weight)) : 1;
    if(d < 0 || d > 1000) Err.or(FTWEIGHT, d);
    return score(s * d);
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  boolean filter(final QueryContext ctx) throws QueryException {
    if(!ordered() || !content() || !same() || !different()) return false;

    // ...distance?
    if(dunit != null) {
      final long mn = checkItr(ctx.iter(dist[0]));
      final long mx = checkItr(ctx.iter(dist[1]));
      if(!checkDist(mn, mx, true)) return false;
    }
    // ...window?
    if(wunit != null) {
      final long w = checkItr(ctx.iter(window));
      if(!checkDist(1, w, false)) return false;
    }
    return true;
  }

  /**
   * Initializes the select operator. Has to be called before any FTWords
   * are performed.
   * @param tok tokenizer for source term
   */
  void init(final FTTokenizer tok) {
    size = 0;
    term.reset();
    ft = tok;
  }

  /**
   * Adds the specified fulltext term and position list. This method is
   * called every time a test in {@link FTOpt#contains} was successful.
   * @param t term to be added
   * @param il positions to be added
   */
  void add(final byte[] t, final IntList il) {
    if(size == pos.length) pos = Array.resize(pos, size, size + 1);
    pos[size++] = il;
    term.add(t);
  }

  /**
   * Sets the position values and the number of tokens.
   * @param il IntList[] with position values
   * @param ilsize int number of tokens in query
   */
  void setPos(final IntList[] il, final int ilsize) {
    pos = il;
    size = ilsize;
  }

  /**
   * Evaluates the mild not expression.
   * @return boolean result
   */
  boolean mildNot() {
    for(int i = 1; i < pos.length; i++) {
      for(int j = 0; j < pos[i].size; j++) {
        if(pos[0].contains(pos[i].list[j])) return false;
      }
    }
    return true;
  }

  /**
   * Checks if the position values are ordered.
   * @return result of check
   */
  private boolean ordered() {
    if(!ordered || size == 1) return true;

    final IntList[] il = sortPositions();
    final IntList p = il[0];
    final IntList pp = il[1];
    int i = 0;
    int lp;
    while (i < p.size && pp.list[i] != 0) i++;
    lp = i;
    i++;
    while (i < p.size) {
      if (pp.list[i] < pp.list[lp] || pp.list[i] == pp.list[lp] + 1) lp = i;
      if (pp.list[lp] == size - 1) return true;
      i++;
    }
    return false;
  }

  /**
   * Checks if the start and end conditions are fulfilled.
   * @return result of check
   */
  private boolean content() {
    // ...to be revised...
    int l = 0;
    if(start || content) {
      for(int i = 0; i < size; i++) {
        boolean o = false;
        final int ts = pos[i].size;
        for(int j = 0; j < (ordered ? Math.min(1, ts) : ts); j++) {
          if(pos[i].list[j] == l) {
            l += new FTTokenizer(term.list[i]).count();
            o = true;
          }
          if(ordered && !content) break;
        }
        if(!o) return false;
        if(o) break;
      }
    }
    if(content && l != ft.count()) return false;

    if(end) {
      final int c = ft.count();
      for(int i = 0; i < size; i++) {
        l += new FTTokenizer(term.list[i]).count();
      }
      for(int i = 0; i < size; i++) {
        boolean o = false;
        final int ts = pos[i].size;
        for(int j = ordered ? Math.max(0, ts - 1) : 0; j < ts; j++) {
          if(l + pos[i].list[j] == c) {
            o = true;
            break;
          }
          if(ordered) break;
        }
        if(!o) return false;
        if(o) break;
      }
    }
    return true;
  }

  /**
   * Checks if all words are found in the same unit.
   * @return result of check
   */
  private boolean same() {
    if(!same) return true;
    for(int i = 0; i < pos[0].size; i++) {
      if(same(pos(pos[0].list[i], sdunit), 1)) return true;
    }
    return false;
  }

  /**
   * Recursively checks if all words are found in the same units.
   * @param v value to be compared
   * @param n current position
   * @return result of check
   */
  private boolean same(final int v, final int n) {
    if(n == size) return true;
    for(int i = 0; i < pos[n].size; i++) {
      if(pos(pos[n].list[i], sdunit) == v && same(v, n + 1)) return true;
    }
    return false;
  }

  /**
   * Checks if all words are found in different units.
   * @return result of check
   */
  private boolean different() {
    return !different || diff(new BoolList(), 0);
  }

  /**
   * Recursively checks if all words are found in different units.
   * @param bl boolean list
   * @param n current position
   * @return result of check
   */
  private boolean diff(final BoolList bl, final int n) {
    if(n == size) return true;
    for(int i = 0; i < pos[n].size; i++) {
      final int p = pos(pos[n].list[i], sdunit);
      if(p < bl.size && bl.list[p]) continue;
      bl.set(true, p);
      if(diff(bl, n + 1)) return true;
      bl.set(false, p);
    }
    return false;
  }

  /**
   * Checks if each token is reached by the ftdistance query.
   * @param mn minimum distance
   * @param mx maximum distance
   * @param dst flag for ftdistance
   * @return result of check
   */
  private boolean checkDist(final long mn, final long mx, final boolean dst) {
    final IntList[] il = sortPositions();
    for(int z = 0; z < il[1].size; z++) {
      if(checkDist(z, il[0], il[1], mn, mx, new BoolList(size), dst))
        return true;
    }
    return false;
  }

  /**
   * Checks if each token is reached by the ftdistance query.
   * @param x current position value
   * @param p pos list
   * @param pp pointer list
   * @param mn minimum number
   * @param mx maximum number
   * @param bl BollList for each token
   * @param dst flag for ftdistance
   * @return boolean result
   */
  private boolean checkDist(final int x, final IntList p,  final IntList pp,
      final long mn, final long mx, final BoolList bl, final boolean dst) {

    if(bl.all(true)) return true;
    int i = x + 1;

    final int p1 = pos(p.list[x], dst ? dunit : wunit);
    while(i < p.size) {
      final int p2 = pos(p.list[i], dst ? dunit : wunit);
      if(dst) {
        // ftdistance
        final int d = Math.abs(p1 - p2) - 1;
        if(d >= mn && d <= mx && !bl.list[pp.list[i]]) {
          bl.list[pp.list[x]] = true;
          bl.list[pp.list[i]] = true;
          if(checkDist(i, p, pp, mn, mx, bl, dst)) return true;
        }
      } else {
        // ftwindow
        final int d = Math.abs(p1 - p2);
        if(mn + d <= mx && !bl.list[pp.list[i]]) {
          bl.list[pp.list[x]] = true;
          bl.list[pp.list[i]] = true;
          if(checkDist(i, p, pp, mn + d, mx, bl, dst)) return true;
        }
      }
      i++;
    }
    return false;
  }

  /**
   * Sorts the position values in numeric order.
   * IntList[0] = position values sorted
   * IntList[1] = pointer to the position values.
   *
   * Each pos value has a pointer, showing which token
   * from the query cloud be found at that pos.
   *
   * @return IntList[] position values and pointer
   */
  private IntList[] sortPositions() {
    final IntList[] il = { new IntList(), new IntList()};
    final int[] k = new int[size];
    int min = 0;

    while(true) {
      min = 0;
      boolean q = true;
      for(int j = 0; j < size; j++) {
        if(k[j] > -1) {
          if(k[min] == -1) min = j;
          q = false;
          if(pos[min].list[k[min]] > pos[j].list[k[j]]) min = j;
        }
      }
      if(q) break;

      il[0].add(pos[min].list[k[min]]);
      il[1].add(min);
      k[min]++;
      if(k[min] == pos[min].size) k[min] = -1;
    }
    return il;
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param p word position
   * @param u unit
   * @return new position
   */
  private int pos(final int p, final FTUnit u) {
    if(u == FTUnit.WRD) return p;
    ft.init();
    while(ft.more() && ft.pos != p);
    return u == FTUnit.SEN ? ft.sent : ft.para;
  }

  /**
   * Returns true if no position filters are defined.
   * @return result of check
   */
  public boolean standard() {
    return !ordered && !start && !end && !content && !same && !different &&
      window == null && dist == null && weight == null;
  }

  @Override
  public void indexAccessible(final QueryContext ctx, final IndexContext ic)
      throws QueryException {

    expr[0].indexAccessible(ctx, ic);
    // index can only be used if there is no ftselection specified before ftnot
    ic.iu &= !ic.ftnot && standard();
  }

  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
    throws QueryException {

    return new FTPosIndex(expr[0].indexEquivalent(ctx, ic), this);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    if(ordered) ser.attribute(token(QueryTokens.ORDERED), TRUE);
    if(start) ser.attribute(token(QueryTokens.START), TRUE);
    if(end) ser.attribute(token(QueryTokens.END), TRUE);
    if(content) ser.attribute(token(QueryTokens.CONTENT), TRUE);
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr[0]);
    if(ordered) sb.append(" " + QueryTokens.ORDERED);
    if(start) sb.append(" " + QueryTokens.AT + " " + QueryTokens.START);
    if(end) sb.append(" " + QueryTokens.AT + " " + QueryTokens.END);
    if(content) sb.append(" " + QueryTokens.ENTIRE + " " + QueryTokens.CONTENT);
    if(dunit != null) {
      sb.append(" distance(");
      sb.append(dist[0]);
      sb.append(",");
      sb.append(dist[1]);
      sb.append(")");
    }
    return sb.toString();
  }
}
