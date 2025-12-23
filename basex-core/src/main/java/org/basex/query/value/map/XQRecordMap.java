package org.basex.query.value.map;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Compact map implementation for records with fixed entries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQRecordMap extends XQHashMap {
  /** Values. */
  private final Value[] values;

  /**
   * Constructor.
   * @param values values
   * @param type record type
   */
  public XQRecordMap(final Value[] values, final Type type) {
    super(type);
    this.values = values;
  }

  @Override
  public boolean refineType() throws QueryException {
    return true;
  }

  @Override
  public long structSize() {
    return values.length;
  }

  @Override
  public XQMap put(final Item key, final Value value) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = ((RecordType) type).fields().index(key.string(null));
      if(i != 0) return putAt(i - 1, value);
    }
    return super.put(key, value);
  }

  @Override
  public XQMap putAt(final int index, final Value value) throws QueryException {
    if(value.seqType().instanceOf(((RecordType) type).fields().value(index + 1).seqType())) {
      final Value[] copy = values.clone();
      copy[index] = value;
      return new XQRecordMap(copy, type);
    }
    return super.putAt(index, value);
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = ((RecordType) type).fields().index(key.string(null));
      if(i != 0) return valueAt(i - 1);
    }
    return null;
  }

  @Override
  public Value keys() {
    return StrSeq.get(((RecordType) type).fields().keys());
  }

  @Override
  public Item keyAt(final int index) {
    return Str.get(((RecordType) type).fields().key(index + 1));
  }

  @Override
  public Value valueAt(final int index) {
    return values[index];
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
  public Item shrink(final Job job) throws QueryException {
    shrinkValues(job);
    return this;
  }
}
