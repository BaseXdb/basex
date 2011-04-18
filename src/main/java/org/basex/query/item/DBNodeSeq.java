package org.basex.query.item;

import static org.basex.query.QueryTokens.*;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Sequence, containing at least two ordered database nodes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DBNodeSeq extends Seq {
  /** Data reference. */
  public final Data data;
  /** Pre values. */
  public final int[] pres;

  /**
   * Constructor.
   * @param p pre values
   * @param d data reference
   * @param t node type
   */
  private DBNodeSeq(final int[] p, final Data d, final Type t) {
    super(p.length, t);
    pres = p;
    data = d;
  }

  /**
   * Returns a value for the specified items.
   * @param v value
   * @param d data reference
   * @param r root flag
   * @return resulting item or sequence
   */
  public static Value get(final int[] v, final Data d, final boolean r) {
    final int s = v.length;
    return s == 0 ? Empty.SEQ : s == 1 ? new DBNode(d, v[0]) :
      new DBNodeSeq(v, d, r ? NodeType.DOC : NodeType.NOD);
  }

  @Override
  public Data data() {
    return data;
  }

  /***
   * Creates a new database node.
   * @param i index
   * @return node
   */
  DBNode node(final int i) {
    return new DBNode(data, pres[i]);
  }

  @Override
  public Object toJava() {
    final Object[] obj = new Object[(int) size];
    for(int s = 0; s != size; ++s) obj[s] = node(s).toJava();
    return obj;
  }

  @Override
  public ValueIter iter() {
    return new ValueIter() {
      int c = -1;
      @Override
      public Item next() { return ++c < size ? node(c) : null; }
      @Override
      public Item get(final long i) { return node((int) i); }
      @Override
      public long size() { return size; }
      @Override
      public boolean reset() { c = -1; return true; }
    };
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) {
    return node(0);
  }

  @Override
  public SeqType type() {
    return SeqType.NOD_OM;
  }

  @Override
  public boolean duplicates() {
    return false;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof DBNodeSeq)) return false;
    final DBNodeSeq seq = (DBNodeSeq) cmp;
    return pres == seq.pres && size == seq.size;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(Token.token(Util.name(this)), SIZE, Token.token(size));
    for(int v = 0; v != Math.min(size, 5); ++v) node(v).plan(ser);
    ser.closeElement();
  }

  @Override
  public int hash(final InputInfo ii) {
    int h = 0;
    for(int v = 0; v != Math.min(size, 5); ++v) h += pres[v];
    return h;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(PAR1);
    for(int v = 0; v != size; ++v) {
      if(v != 0) sb.append(SEP);
      sb.append(node(v));
      if(sb.length() > 32 && v + 1 != size) {
        sb.append(SEP).append(DOTS);
        break;
      }
    }
    return sb.append(PAR2).toString();
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    for(int i = 0; i < pres.length; i++) arr[i + start] = itemAt(i);
    return pres.length;
  }

  @Override
  public Item itemAt(final long pos) {
    return new DBNode(data, pres[(int) pos]);
  }

  @Override
  public boolean homogenous() {
    return false;
  }
}
