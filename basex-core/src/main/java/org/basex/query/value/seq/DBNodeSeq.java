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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBNodeSeq extends NativeSeq {
  /** Data reference. */
  private final Data data;
  /** Pre values. */
  public final int[] pres;
  /** Pre values comprise all documents of the database. */
  public final boolean all;

  /**
   * Constructor.
   * @param pres pre values
   * @param data data reference
   * @param type node type
   * @param all pre values comprise all documents of the database
   */
  private DBNodeSeq(final int[] pres, final Data data, final Type type, final boolean all) {
    super(pres.length, type);
    this.pres = pres;
    this.data = data;
    this.all = all;
  }

  @Override
  public Data data() {
    return data;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) {
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

  @Override
  public Value reverse() {
    final int s = pres.length;
    final int[] tmp = new int[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = pres[r];
    return get(tmp, data, type, false);
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a node sequence with the given data reference and pre values.
   * @param pres pre values
   * @param data data reference
   * @param docs all values reference document nodes
   * @param all pre values comprise all documents of the database
   * @return resulting item or sequence
   */
  public static Value get(final IntList pres, final Data data, final boolean docs,
      final boolean all) {
    return get(pres.toArray(), data, docs ? NodeType.DOC : NodeType.NOD, all);
  }

  /**
   * Creates a node sequence with the given data reference and pre values.
   * @param pres pre values
   * @param data data reference
   * @param type node type
   * @param all pre values comprise all documents of the database
   * @return resulting item or sequence
   */
  private static Value get(final int[] pres, final Data data, final Type type, final boolean all) {
    return pres.length == 0 ? Empty.SEQ : pres.length == 1 ? new DBNode(data, pres[0]) :
      new DBNodeSeq(pres, data, type, all);
  }
}
