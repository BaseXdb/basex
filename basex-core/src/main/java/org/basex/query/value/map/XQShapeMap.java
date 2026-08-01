package org.basex.query.value.map;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Compact map implementation for maps with a known shape.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQShapeMap extends XQHashMap {
  /** Values. */
  private final Value[] values;

  /**
   * Constructor.
   * @param type shape
   * @param values values
   */
  public XQShapeMap(final Type type, final Value... values) {
    super(type);
    this.values = values;
  }

  @Override
  public boolean refineType() {
    // a named record type must retain its declared field types
    final ShapeType sh = (ShapeType) type;
    if(sh.name() == null) {
      final TokenObjectMap<ShapeField> fields = sh.fields();
      final int fs = fields.size();
      final TokenObjectMap<ShapeField> refined = new TokenObjectMap<>(fs);
      boolean narrowed = false;
      for(int f = 0; f < fs; f++) {
        final SeqType ost = fields.value(f + 1).seqType(), nst = values[f].seqType();
        final SeqType st = nst.instanceOf(ost) ? nst : ost;
        if(!st.eq(ost)) narrowed = true;
        refined.put(fields.key(f + 1), new ShapeField(st));
      }
      if(narrowed) type = sh.with(refined);
    }
    return true;
  }

  @Override
  public long structSize() {
    return values.length;
  }

  @Override
  public XQMap put(final Item key, final Value value) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = fields().index(key.string(null));
      if(i != 0) return putAt(i - 1, value);
    }
    return super.put(key, value);
  }

  @Override
  public XQMap putAt(final int index, final Value value) throws QueryException {
    if(value.seqType().instanceOf(fields().value(index + 1).seqType())) {
      final Type tp = type instanceof final ShapeType sh ? sh.shape() : type;
      if(value == values[index] && tp == type) return this;
      final Value[] copy = values.clone();
      copy[index] = value;
      return new XQShapeMap(tp, copy);
    }
    return super.putAt(index, value);
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = fields().index(key.string(null));
      if(i != 0) return valueAt(i - 1);
    }
    return null;
  }

  @Override
  public Value keys() {
    return StrSeq.get(fields().keys());
  }

  @Override
  public Item keyAt(final long index) {
    return Str.get(fields().key((int) index + 1));
  }

  /**
   * Returns the fields of the map type.
   * @return fields
   */
  private TokenObjectMap<ShapeField> fields() {
    return ((ShapeType) type).fields();
  }

  @Override
  public Value valueAt(final long index) {
    return values[(int) index];
  }

  @Override
  void valueAt(final int index, final Value value) {
    values[index] = value;
  }

  @Override
  XQHashMap build(final Item key, final Value value) {
    throw Util.notExpected();
  }

  @Override
  public XQMap materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {

    if(materialized(test, ii)) return this;

    final int vl = values.length;
    final Value[] vals = new Value[vl];
    for(int v = 0; v < vl; v++) {
      qc.checkStop();
      vals[v] = values[v].materialize(test, ii, qc);
    }
    return new XQShapeMap(((ShapeType) type).detach(), vals);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return ((ShapeType) type).detached() && super.materialized(test, ii);
  }

  @Override
  public Item shrink(final QueryContext qc) throws QueryException {
    shrinkValues(qc);
    refineType();
    return this;
  }
}
