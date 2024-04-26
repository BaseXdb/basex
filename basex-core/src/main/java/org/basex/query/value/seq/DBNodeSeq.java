package org.basex.query.value.seq;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence, containing at least two nodes in distinct document order (DDO).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class DBNodeSeq extends NativeSeq {
  /** Data reference. */
  protected final Data data;
  /** Pre values reference all documents of the database. */
  protected final boolean all;
  /** Pre values. */
  protected int[] pres;

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
  public boolean ddo() {
    return true;
  }

  @Override
  public DBNode itemAt(final long pos) {
    return new DBNode(data, pres[(int) pos]);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(int i = 0; i < size; i++) vb.add(itemAt(i).atomValue(qc, ii));
    return vb.value(AtomType.ANY_ATOMIC_TYPE);
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
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final int[] tmp = new int[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = pres[i];
    return new DBNodeSeq(tmp, data, type, false);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    if(mode.oneOf(Simplify.DATA, Simplify.NUMBER, Simplify.STRING)) {
      if(mode == Simplify.STRING) {
        final TokenList list = new TokenList(size);
        for(int i = 0; i < size; i++) list.add(data.atom(pres[i]));
        expr = StrSeq.get(list);
      } else {
        final int sz = (int) size;
        final Item[] items = new Item[sz];
        for(int i = 0; i < size; i++) items[i] = Atm.get(data.atom(pres[i]));
        expr = ItemSeq.get(items, sz, AtomType.UNTYPED_ATOMIC);
      }
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii) {
    return test.test(data);
  }

  @Override
  public void refineType() {
    refineType(this);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof DBNodeSeq)) return super.equals(obj);
    final DBNodeSeq ds = (DBNodeSeq) obj;
    return size == ds.size && Arrays.equals(pres, ds.pres);
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add('(');
    for(int p = 0; p < size; ++p) {
      if(p > 0) tb.add(SEP);
      tb.add(_DB_GET_PRE.args(data.meta.name, pres[p]).trim());
      if(tb.size() <= 16 || p + 1 == size) continue;
      // chop output to prevent too long error strings
      tb.add(SEP).add(DOTS);
      break;
    }
    qs.token(tb.add(')').finish());
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified values.
   * @param pres pre values
   * @param data data reference
   * @param type node type (can be {@code null})
   * @param all pre values reference all documents of the database
   * @return value
   */
  public static Value get(final int[] pres, final Data data, final Type type, final boolean all) {
    return pres.length == 0 ? Empty.VALUE : pres.length == 1 ? new DBNode(data, pres[0]) :
      new DBNodeSeq(pres, data, type == null ? NodeType.NODE : type, all);
  }

  /**
   * Creates a sequence with the specified values.
   * @param pres pre values
   * @param data data reference
   * @param expr expression (can be {@code null})
   * @return value
   */
  public static Value get(final int[] pres, final Data data, final Expr expr) {
    return get(pres, data, NodeType.NODE.refine(expr), false);
  }

  /**
   * Creates a node sequence with the given data reference and pre values.
   * @param pres pre values
   * @param data data reference
   * @param docs all values reference document nodes
   * @param all pre values reference all documents of the database
   * @return value
   */
  public static Value get(final IntList pres, final Data data, final boolean docs,
      final boolean all) {
    return get(pres.toArray(), data, docs ? NodeType.DOCUMENT_NODE : null, all);
  }
}
