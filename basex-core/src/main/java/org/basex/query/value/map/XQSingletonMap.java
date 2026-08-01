package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Map with a single entry.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQSingletonMap extends XQMap {
  /** Key. */
  private final Item k;
  /** Value. */
  private Value v;

  /**
   * Constructor.
   * @param key key
   * @param value value
   */
  XQSingletonMap(final Item key, final Value value) {
    this(key, value, MapType.get(key.type, value.seqType()));
  }

  /**
   * Constructor with a predefined type.
   * @param key key
   * @param value value
   * @param type map type
   */
  private XQSingletonMap(final Item key, final Value value, final Type type) {
    super(type);
    k = key;
    v = value;
  }

  @Override
  public long structSize() {
    return 1;
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    return key.atomicEqual(k) ? v : null;
  }

  @Override
  public Value keys() {
    return k;
  }

  @Override
  public Item keyAt(final long index) {
    return k;
  }

  @Override
  public Value valueAt(final long index) {
    return v;
  }

  @Override
  public XQMap put(final Item key, final Value value) throws QueryException {
    return key.atomicEqual(k) ? putAt(0, value) : empty().put(k, v).put(key, value);
  }

  @Override
  public XQMap putAt(final int index, final Value value) {
    if(value == v) return this;
    // the shape is preserved (without the record annotation) if the value matches the field type
    if(type instanceof final ShapeType sh &&
        value.seqType().instanceOf(sh.fields().value(index + 1).seqType())) {
      return new XQSingletonMap(k, value, sh.shape());
    }
    return new XQSingletonMap(k, value);
  }

  @Override
  public XQMap remove(final Item key) throws QueryException {
    return key.atomicEqual(k) ? empty() : this;
  }

  @Override
  public void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    func.accept(k, v);
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    return func.test(k, v);
  }

  @Override
  public boolean refineType() throws QueryException {
    if(!(type instanceof final ShapeType sh)) return super.refineType();
    // a named record must retain its declared field types
    if(sh.name() == null) {
      final SeqType ost = sh.fields().value(1).seqType(), nst = v.seqType();
      final SeqType st = nst.instanceOf(ost) ? nst : ost;
      if(!st.eq(ost)) {
        final TokenObjectMap<ShapeField> refined = new TokenObjectMap<>(1);
        refined.put(sh.fields().key(1), new ShapeField(st));
        type = sh.with(refined);
      }
    }
    return true;
  }

  @Override
  public Item shrink(final QueryContext qc) throws QueryException {
    v = v.shrink(qc);
    refineType();
    return this;
  }
}
