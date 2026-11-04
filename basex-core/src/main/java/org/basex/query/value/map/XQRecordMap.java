package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Record whose static field values are shared by all records of the same type.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQRecordMap extends XQShapeMap {
  /** Values of the non-static fields. */
  private final Value[] values;
  /** Layout. */
  private final Layout layout;

  /**
   * Constructor.
   * @param type shape
   * @param values values of the non-static fields
   * @param layout layout
   */
  private XQRecordMap(final ShapeType type, final Value[] values, final Layout layout) {
    super(type);
    this.values = values;
    this.layout = layout;
  }

  /**
   * Creates a record that shares its static field values with other records of the same type.
   * @param rt record type
   * @param values values, one per field
   * @param qc query context
   * @return record
   */
  public static XQMap get(final RecordType rt, final Value[] values, final QueryContext qc) {
    final int[] slots = rt.slots();
    if(slots == null) return XQMap.get(rt, values);
    final Layout layout = qc.caches.get(rt, () -> new Layout(slots, values));
    final Value[] vals = new Value[layout.size];
    final int vl = values.length;
    for(int v = 0; v < vl; v++) {
      final int s = slots[v];
      if(s >= 0) vals[s] = values[v];
      else if(values[v] != layout.statics[v]) return XQMap.get(rt, values);
    }
    return new XQRecordMap(rt, vals, layout);
  }

  @Override
  public Value valueAt(final long index) {
    final int s = layout.slots[(int) index];
    return s >= 0 ? values[s] : layout.statics[(int) index];
  }

  @Override
  XQMap putAt(final ShapeType tp, final int index, final Value value) {
    final int s = layout.slots[index];
    if(s < 0) return super.putAt(tp, index, value);
    final Value[] vals = values.clone();
    vals[s] = value;
    return new XQRecordMap(tp, vals, layout);
  }

  /** Storage slots of the fields and shared values of the static fields. */
  private static final class Layout {
    /** Storage slots: an instance slot, or {@code -1} for static fields. */
    private final int[] slots;
    /** Values of the static fields, indexed by field ({@code null} for non-static fields). */
    private final Value[] statics;
    /** Number of non-static fields. */
    private final int size;

    /**
     * Constructor.
     * @param slots storage slots of the fields
     * @param values values, one per field
     */
    private Layout(final int[] slots, final Value[] values) {
      this.slots = slots;
      statics = values.clone();
      final int sl = slots.length;
      int s = 0;
      for(int v = 0; v < sl; v++) {
        if(slots[v] >= 0) {
          statics[v] = null;
          s++;
        }
      }
      size = s;
    }
  }
}
