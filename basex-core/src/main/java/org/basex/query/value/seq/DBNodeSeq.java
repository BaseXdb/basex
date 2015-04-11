package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence, containing at least two ordered database nodes.
 *
 * @author BaseX Team 2005-15, BSD License
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
  public Seq insert(final long pos, final Item item) {
    // TODO check data instance?
    return copyInsert(pos, item);
  }

  @Override
  public Value remove(final long pos) {
    final int p = (int) pos, n = pres.length - 1;
    if(n == 1) return itemAt(1 - pos);
    final int[] out = new int[n];
    System.arraycopy(pres, 0, out, 0, p);
    System.arraycopy(pres, p + 1, out, p, n - p);
    return new DBNodeSeq(out, data, type, false);
  }

  @Override
  public Value reverse() {
    final int s = pres.length;
    final int[] tmp = new int[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = pres[r];
    return get(tmp, data, type, false);
  }

  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(int s = 0; s < size; s++) vb.add(itemAt(s).atomValue(ii));
    return vb.value();
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
