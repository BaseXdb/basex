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
    final int vl = values.length;
    final SeqType[] seqTypes = new SeqType[vl];
    for(int v = 0; v < vl; v++) seqTypes[v] = values[v].seqType();
    type = shape().refine(seqTypes);
    return true;
  }

  @Override
  public long structSize() {
    return values.length;
  }

  @Override
  public XQMap put(final Item key, final Value value) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final byte[] name = key.string(null);
      final int i = fields().index(name);
      if(i != 0) return putAt(i - 1, value);
      if(key.type == BasicType.STRING) {
        final ShapeType sh = shape().put(name, value.seqType());
        if(sh != null) return new XQShapeMap(sh, Array.add(values, value));
      }
    }
    return super.put(key, value);
  }

  @Override
  public XQMap putAt(final int index, final Value value) throws QueryException {
    final ShapeType sh = shape();
    final SeqType vt = value.seqType();
    final ShapeType tp = vt.instanceOf(fields().value(index + 1).seqType()) ? sh.shape() :
      sh.put(fields().key(index + 1), vt);
    if(value == values[index] && tp == type) return this;
    final Value[] copy = values.clone();
    copy[index] = value;
    return new XQShapeMap(tp, copy);
  }

  @Override
  public XQMap remove(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final byte[] name = key.string(null);
      final int i = fields().index(name);
      if(i == 0) return this;
      if(values.length == 1) return empty();
      return new XQShapeMap(shape().remove(name), Array.remove(values, i - 1));
    }
    return super.remove(key);
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
   * Returns the shape of this map.
   * @return shape
   */
  private ShapeType shape() {
    return (ShapeType) type;
  }

  /**
   * Returns the fields of the map type.
   * @return fields
   */
  private TokenObjectMap<ShapeField> fields() {
    return shape().fields();
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
    return new XQShapeMap(shape().detach(), vals);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return shape().detached() && super.materialized(test, ii);
  }

  @Override
  public Item shrink(final QueryContext qc) throws QueryException {
    shrinkValues(qc);
    refineType();
    return this;
  }
}
