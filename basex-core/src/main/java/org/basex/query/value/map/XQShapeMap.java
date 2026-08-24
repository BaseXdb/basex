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
 * Map whose keys are supplied by its shape.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class XQShapeMap extends XQMap {
  /**
   * Constructor.
   * @param type shape
   */
  XQShapeMap(final ShapeType type) {
    super(type);
  }

  @Override
  public final long structSize() {
    return fields().size();
  }

  @Override
  public final Value getOrNull(final Item key) throws QueryException {
    final int i = field(key);
    return i != 0 ? valueAt(i - 1) : null;
  }

  @Override
  public final Value keys() {
    return StrSeq.get(fields().keys());
  }

  @Override
  public final Item keyAt(final long index) {
    return shape().key((int) index + 1);
  }

  @Override
  public final XQMap put(final Item key, final Value value) throws QueryException {
    final int i = field(key);
    if(i != 0) return putAt(i - 1, value);

    if(key.type == BasicType.STRING) {
      final ShapeType sh = shape().put(key.string(null), value.seqType());
      if(sh != null) return get(sh, Array.add(values(), value));
    }
    return trie().put(key, value);
  }

  @Override
  public final XQMap putAt(final int index, final Value value) {
    // the shape is preserved (without the record annotation) if the value matches the field type
    final ShapeType sh = shape();
    final SeqType st = value.seqType();
    final ShapeType tp = st.instanceOf(fields().value(index + 1).seqType()) ? sh.shape() :
      sh.put(fields().key(index + 1), st);
    if(value == valueAt(index) && tp == type) return this;

    final Value[] values = values();
    values[index] = value;
    return get(tp, values);
  }

  @Override
  public final XQMap remove(final Item key) throws QueryException {
    final int i = field(key);
    if(i == 0) return this;
    if(structSize() == 1) return empty();
    return get(shape().remove(fields().key(i)), Array.remove(values(), i - 1));
  }

  @Override
  public final void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    final int fs = (int) structSize();
    for(int f = 0; f < fs; f++) func.accept(keyAt(f), valueAt(f));
  }

  @Override
  public final boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    final int fs = (int) structSize();
    for(int f = 0; f < fs; f++) {
      if(!func.test(keyAt(f), valueAt(f))) return false;
    }
    return true;
  }

  @Override
  public final boolean refineType() {
    final ShapeType sh = shape();
    if(sh.name() == null) {
      final int fs = (int) structSize();
      final SeqType[] seqTypes = new SeqType[fs];
      for(int f = 0; f < fs; f++) seqTypes[f] = valueAt(f).seqType();
      type = sh.refine(seqTypes);
    }
    return true;
  }

  @Override
  public final XQMap materialize(final Predicate<Data> test, final boolean funcs,
      final InputInfo ii, final QueryContext qc) throws QueryException {

    if(materialized(test, funcs, ii)) return this;

    final Value[] values = values();
    final int vl = values.length;
    for(int v = 0; v < vl; v++) {
      qc.checkStop();
      values[v] = values[v].materialize(test, funcs, ii, qc);
    }
    return get(shape().detach(), values);
  }

  @Override
  public final boolean materialized(final Predicate<Data> test, final boolean funcs,
      final InputInfo ii) throws QueryException {
    return shape().detached() && super.materialized(test, funcs, ii);
  }

  @Override
  public final Item shrink(final QueryContext qc) throws QueryException {
    final Value[] values = values();
    final int vl = values.length;
    for(int v = 0; v < vl; v++) values[v] = values[v].shrink(qc);
    final XQMap map = get(shape(), values);
    map.refineType();
    return map;
  }

  /**
   * Returns the values of this map.
   * @return values
   */
  private Value[] values() {
    final int fs = (int) structSize();
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; f++) values[f] = valueAt(f);
    return values;
  }

  /**
   * Returns the index of the field that is addressed by the specified key.
   * @param key key to look for
   * @return index (starting with 1), or {@code 0} if the key is no field name
   * @throws QueryException query exception
   */
  private int field(final Item key) throws QueryException {
    return key.type.isStringOrUntyped() ? fields().index(key.string(null)) : 0;
  }

  /**
   * Returns the shape of this map.
   * @return shape
   */
  private ShapeType shape() {
    return (ShapeType) type;
  }

  /**
   * Returns the fields of the shape.
   * @return fields
   */
  private TokenObjectMap<ShapeField> fields() {
    return shape().fields();
  }
}
