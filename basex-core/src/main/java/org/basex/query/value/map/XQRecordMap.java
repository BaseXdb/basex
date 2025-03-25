package org.basex.query.value.map;

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
  public XQRecordMap(final Value[] values, final RecordType type) {
    super(values.length, type);
    this.values = values;
  }

  @Override
  public long structSize() {
    return capacity;
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = ((RecordType) type).fields().index(key.string(null));
      if(i != 0) return valueInternal(i);
    }
    return null;
  }

  @Override
  Value keysInternal() {
    return StrSeq.get(((RecordType) type).fields().toArray());
  }

  @Override
  Item keyInternal(final int pos) {
    return Str.get(((RecordType) type).fields().key(pos));
  }

  @Override
  Value valueInternal(final int pos) {
    return values[pos - 1];
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    throw Util.notExpected();
  }
}
