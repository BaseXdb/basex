package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class DBNodeSeq extends NativeSeq {
  /** Data reference. */
  protected final Data data;
  /** Pre values. */
  protected int[] pres;
  /** Pre values reference all documents of the database. */
  protected boolean all;

  /**
   * Constructor.
   * @param pres pre values
   * @param data data reference
   * @param type node type
   * @param all pre values reference all documents of the database
   */
  protected DBNodeSeq(final int[] pres, final Data data, final Type type, final boolean all) {
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
  public Value atomValue(final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(int s = 0; s < size; s++) vb.add(itemAt(s).atomValue(ii));
    return vb.value();
  }

  /**
   * Returns the internal pre value array.
   * @return pre values
   */
  public int[] pres() {
    return pres;
  }

  /**
   * Returns the specified pre value.
   * @param index index of pre value
   * @return pre value
   */
  public int pre(final int index) {
    return pres[index];
  }

  /**
   * Indicates if pre values reference all documents of the database.
   * @return flag
   */
  public boolean all() {
    return all;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(PAREN1);
    for(int i = 0; i < size; ++i) {
      sb.append(i == 0 ? "" : SEP);
      sb.append(_DB_OPEN_PRE.args(data.meta.name, pres[i]));
      if(sb.length() <= 16 || i + 1 == size) continue;
      // output is chopped to prevent too long error strings
      sb.append(SEP).append(DOTS);
      break;
    }
    return sb.append(PAREN2).toString();
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a node sequence with the given data reference and pre values.
   * @param pres pre values
   * @param data data reference
   * @param docs all values reference document nodes
   * @param all pre values reference all documents of the database
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
   * @param all pre values reference all documents of the database
   * @return resulting item or sequence
   */
  private static Value get(final int[] pres, final Data data, final Type type, final boolean all) {
    return pres.length == 0 ? Empty.SEQ : pres.length == 1 ? new DBNode(data, pres[0]) :
      new DBNodeSeq(pres, data, type, all);
  }
}
