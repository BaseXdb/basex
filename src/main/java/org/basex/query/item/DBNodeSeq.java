package org.basex.query.item;

import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.IntList;

/**
 * Sequence, containing at least two ordered database nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DBNodeSeq extends Seq {
  /** Data reference. */
  public final Data data;
  /** Pre values. */
  public final int[] pres;
  /** Complete. */
  public boolean complete;

  /**
   * Constructor.
   * @param p pre values
   * @param d data reference
   * @param t node type
   * @param c indicates if pre values represent all document nodes of a database
   */
  private DBNodeSeq(final int[] p, final Data d, final Type t,
      final boolean c) {
    super(p.length, t);
    pres = p;
    data = d;
    complete = c;
  }

  /**
   * Creates a node sequence with the given data reference and pre values.
   * @param v pre values
   * @param d data reference
   * @param docs indicates if all values reference document nodes
   * @param c indicates if values include all document nodes of a database
   * @return resulting item or sequence
   */
  public static Value get(final IntList v, final Data d, final boolean docs,
      final boolean c) {

    final int s = v.size();
    return s == 0 ? Empty.SEQ : s == 1 ? new DBNode(d, v.get(0)) :
      new DBNodeSeq(v.toArray(), d, docs ? NodeType.DOC : NodeType.NOD, c);
  }

  @Override
  public Data data() {
    return data;
  }

  @Override
  public Object toJava() {
    final Object[] obj = new Object[(int) size];
    for(int s = 0; s != size; ++s) obj[s] = itemAt(s).toJava();
    return obj;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) {
    return itemAt(0);
  }

  @Override
  public SeqType type() {
    return SeqType.NOD_OM;
  }

  @Override
  public boolean iterable() {
    return true;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof DBNodeSeq)) return false;
    final DBNodeSeq seq = (DBNodeSeq) cmp;
    return pres == seq.pres && size == seq.size;
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    for(int i = 0; i < pres.length; i++) arr[i + start] = itemAt(i);
    return pres.length;
  }

  @Override
  public DBNode itemAt(final long pos) {
    return new DBNode(data, pres[(int) pos]);
  }

  @Override
  public boolean homogenous() {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(Token.token(Util.name(this)), SIZE, Token.token(size));
    for(int v = 0; v != Math.min(size, 5); ++v) itemAt(v).plan(ser);
    ser.closeElement();
  }
}
