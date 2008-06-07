package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.util.Token.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTSelect extends Single implements Cloneable {
  /** Units. */
  public enum Unit {
    /** Word unit. */      WORDS,
    /** Sentence unit. */  SENTENCES,
    /** Paragraph unit. */ PARAGRAPHS
  };

  /** Ordered flag. */
  public boolean ordered;
  /** Start flag. */
  public boolean start;
  /** End flag. */
  public boolean end;
  /** Entire content flag. */
  public boolean content;
  /** Window. */
  public Expr window;
  /** Window unit. */
  public Unit wunit;
  /** Distance occurrences. */
  public Expr[] dist;
  /** Distance unit. */
  public Unit dunit;
  /** Same flag. */
  public boolean same;
  /** Different flag. */
  public boolean different;
  /** Same/different unit. */
  public Unit sdunit;
  /** Weight. */
  public Expr weight;

  /** Term list. */
  private TokenList term = new TokenList();
  /** Position list. */
  private IntList[] pos = new IntList[0];
  /** Number of entries. */
  private int size;

  /**
   * Constructor.
   * @param e expression
   */
  public FTSelect(final Expr e) {
    super(e);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final FTSelect tmp = ctx.ftselect;
    ctx.ftselect = this;
    size = 0;
    term.reset();

    final Item it = ctx.iter(expr).next();
    ctx.ftselect = tmp;
    double s = it.dbl();
    
    // ...s == 0 correct?
    if(size == 0 || s == 0) return Dbl.iter(s);

    if(ordered) {
      int c = -1;
      int d = -1;
      for(int i = 0; i < size; i++) {
        for(int j = 0; j < pos[i].size; j++) {
          d = pos[i].get(j);
          if(c <= d) break;
        }
        if(c > d) return Dbl.iter(0);
        c = d;
      }
    }

    // ...to be revised...
    if(start || end || content) {
      final int c = words(ctx.ftitem);
      int l = 0;
      if(start || content) {
        for(int i = 0; i < size; i++) {
          boolean o = false;
          final int ts = pos[i].size;
          for(int j = 0; j < (ordered ? Math.min(1, ts) : ts); j++) {
            if(pos[i].get(j) == l) {
              l += words(term.list[i]);
              o = true;
            }
            if(ordered && !content) break;
          }
          if(!o) return Dbl.iter(0);
          if(o) break;
        }
      }
      if(content && l != c) return Dbl.iter(0);

      if(end) {
        for(int i = 0; i < size; i++) l += words(term.list[i]);
        for(int i = 0; i < size; i++) {
          boolean o = false;
          final int ts = pos[i].size;
          for(int j = ordered ? Math.max(0, ts - 1) : 0; j < ts; j++) {
            if(l + pos[i].get(j) == c) {
              o = true;
              break;
            }
            if(ordered) break;
          }
          if(!o) return Dbl.iter(0);
          if(o) break;
        }
      }
    }

    // ...to be revised...
    if(dunit != null) {
      final long mn = checkItr(ctx.iter(dist[0]));
      final long mx = checkItr(ctx.iter(dist[1]));
      
      int l = -1;
      for(int i = 0; i < size; i++) {
        boolean o = false;
        for(int j = 0; j < pos[i].size; j++) {
          final int p = calc(ctx, pos[i].get(j), dunit);
          int d = Math.abs(p - l) - 1;
          if(i == 0 || (d >= mn && d <= mx)) {
            o = true;
            l = p;
            break;
          }
        }
        if(!o) return Dbl.iter(0);
      }
    }

    // ...to be revised...
    if(wunit != null) {
      final long c = checkItr(ctx.iter(window));
      int l = -1;
      for(int i = 0; i < size; i++) {
        boolean o = false;
        for(int j = 0; j < pos[i].size; j++) {
          final int p = calc(ctx, pos[i].get(j), wunit);
          if(i == 0 || (Math.abs(p - l) - 1 < c)) {
            o = true;
            l = p;
            break;
          }
        }
        if(!o) return Dbl.iter(0);
      }
    }

    if(same) {
      final IntList il = pos[0];
      int p = -1, q = 0;
      for(int i = 0; i < il.size && p != q; i++) {
        p = calc(ctx, il.get(i), sdunit);
        q = p;
        for(int j = 1; j < size && p == q; j++) {
          for(int k = 0; k < pos[j].size; k++) {
            q = calc(ctx, pos[j].get(k), sdunit);
            if(p == q) break;
          }
        }
      }
      if(p != q) return Dbl.iter(0);
    }
    
    // ...to be revised...
    if(different) {
      int l = -1;
      for(int i = 0; i < size; i++) {
        boolean o = false;
        for(int j = 0; j < pos[i].size; j++) {
          final int p = calc(ctx, pos[i].get(j), sdunit);
          if(i != 0 && p != l) {
            o = true;
            break;
          }
          l = p;
        }
        if(i != 0 && !o) return Dbl.iter(0);
      }
    }

    final double d = checkDbl(ctx.iter(weight));
    if(d < 0 || d > 1000) Err.or(FTWEIGHT, d);
    return d != 1 ? Dbl.iter(it.dbl() * d) : it.iter();
  }

  /**
   * Adds a fulltext term.
   * @param t term to be added
   * @param il integer list to be added
   */
  void add(final byte[] t, final IntList il) {
    pos = Array.resize(pos, size, ++size);
    pos[size - 1] = il;
    term.add(t);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    weight = weight.comp(ctx);
    return super.comp(ctx);
  }

  /**
   * Calculates a new position value, dependent on the specified unit.
   * @param ctx query context
   * @param p word position
   * @param u unit
   * @return new position
   */
  private int calc(final XQContext ctx, final int p, final Unit u) {
    switch(u) {
      case SENTENCES : return sentence(ctx.ftitem, p);
      case PARAGRAPHS: return paragraph(ctx.ftitem, p);
      default:         return p;
    }
  }

  /**
   * Returns the number of tokens of the specified item.
   * @param tok token
   * @return word position
   */
  private static int words(final byte[] tok) {
    final int tl = tok.length;

    // compare tokens character wise
    int p = 0;
    boolean l = false;
    for(int t = 0; t < tl; t++) {
      final boolean lod = letterOrDigit(tok[t]);
      if(!l && lod) p++;
      l = lod;
    }
    return p;
  }

  /**
   * Returns the sentence number for the specified position.
   * @param tok token
   * @param pos token position
   * @return word position
   */
  private static int sentence(final byte[] tok, final int pos) {
    final int tl = tok.length;

    int p = 0;
    int s = 0;
    boolean l = false;
    for(int t = 0; t < tl && p <= pos; t++) {
      final byte c = tok[t];
      final boolean ld = letterOrDigit(c);
      if(!l && ld) p++;
      if(c == '.' || c == '!' || c == '?') s++;
      l = ld;
    }
    return s;
  }

  /**
   * Returns the paragraph number for the specified position.
   * @param tok token
   * @param pos token position
   * @return word position
   */
  private static int paragraph(final byte[] tok, final int pos) {
    final int tl = tok.length;

    int p = 0;
    int s = 0;
    boolean l = false;
    for(int t = 0; t < tl && p < pos; t++) {
      final byte c = tok[t];
      final boolean ld = letterOrDigit(c);
      if(!l && ld) p++;
      if(c == '\n') s++;
      l = ld;
    }
    return s;
  }

  @Override
  public FTOptions clone() {
    try {
      return (FTOptions) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr);
    if(ordered) sb.append(" ordered");
    if(start) sb.append(" at start");
    if(end) sb.append(" at end");
    if(content) sb.append(" entire content");
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
