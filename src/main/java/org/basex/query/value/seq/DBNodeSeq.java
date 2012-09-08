package org.basex.query.value.seq;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence, containing at least two ordered database nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DBNodeSeq extends NativeSeq {
  /** Data reference. */
  private final Data data;
  /** Pre values. */
  public final int[] pres;
  /** Complete. */
  public final boolean complete;

  /**
   * Constructor.
   * @param p pre values
   * @param d data reference
   * @param t node type
   * @param c indicates if pre values represent all document nodes of a database
   */
  private DBNodeSeq(final int[] p, final Data d, final Type t, final boolean c) {
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
  public Item ebv(final QueryContext ctx, final InputInfo ii) {
    return itemAt(0);
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
  public DBNode itemAt(final long pos) {
    return new DBNode(data, pres[(int) pos]);
  }
}
