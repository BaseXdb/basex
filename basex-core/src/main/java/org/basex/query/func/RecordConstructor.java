package org.basex.query.func;

import java.util.*;
import java.util.function.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Constructor function for some record type.
 */
public class RecordConstructor extends StandardFunc {
  /** Record type. */
  private final RecordType recordType;
  /** Field names. */
  private final QNm[] names;
  /** Created compact record map. */
  private final boolean compact;

  /**
   * Constructor.
   * @param recordType record type
   */
  public RecordConstructor(final RecordType recordType) {
    this.recordType = recordType;
    final TokenObjectMap<RecordField> fields = recordType.fields();
    final int fs = fields.size();
    names = new QNm[fs];
    boolean c = true;
    for(int f = 1; f <= fs; ++f) {
      names[f - 1] = new QNm(fields.key(f));
      c = c && fields.value(f).alwaysAdded();
    }
    compact = c;
  }

  /**
   * Returns a constructor function for a record type.
   * @param rt record type
   * @param ii input info
   * @param args constructor arguments
   * @return constructor function
   */
  public static RecordConstructor get(final InputInfo ii, final RecordType rt, final Expr[] args) {
    final RecordConstructor rc = new RecordConstructor(rt);
    rc.init(ii, definition(rt), args);
    return rc;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenObjectMap<RecordField> fields = recordType.fields();
    final int fs = fields.size(), el = exprs.length;
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; ++f) {
      final RecordField rf = fields.value(f + 1);
      final Value value = f < el ? exprs[f].value(qc) : rf.init().value(qc);
      if(!value.isEmpty() || rf.alwaysAdded()) {
        values[f] = rf.seqType().coerce(value, qc, ii, names[f], null);
      }
    }
    // create compact record map if all fields of the type are present
    if(compact) return new XQRecordMap(recordType, values);

    // create regular map otherwise
    final MapBuilder mb = new MapBuilder(fs);
    for(int f = 0; f < fs; ++f) {
      if(values[f] != null) mb.put(fields.key(f + 1), values[f]);
    }
    final XQMap map = mb.map();
    map.type = recordType;
    return map;
  }

  @Override
  public long structSize() {
    return compact ? recordType.fields().size() : -1;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final RecordConstructor rc && recordType.eq(rc.recordType);
  }

  @Override
  public void toString(final QueryString qs) {
    if(recordType.name() != null) {
      super.toString(qs);
    } else {
      qs.token("{ ");
      final TokenObjectMap<RecordField> fields = recordType.fields();
      int f = 0;
      for(final Expr expr : exprs) {
        if(++f > 1) qs.token(',');
        qs.quoted(fields.key(f)).token(':').token(expr);
      }
      qs.token(" }");
    }
  }

  /**
   * Returns a constructor function definition for this record type.
   * @param rt record type
   * @return constructor function definition
   */
  public static FuncDefinition definition(final RecordType rt) {
    final TokenBuilder tb = new TokenBuilder(
        rt.name() == null ? Token.token(QueryText.RECORD) : rt.name().local()).add('(');
    final TokenObjectMap<RecordField> fields = rt.fields();
    final int max = fields.size();
    final int min = rt.minFields();
    for(int i = 1; i <= max; ++i) {
      if(i == min + 1) tb.add('[');
      if(i > 1) tb.add(',');
      tb.add(fields.key(i));
    }
    if(max > min) tb.add(']');
    final String description = tb.add(')').toString();

    final SeqType[] params = new SeqType[max];
    for(int i = 0; i < max; ++i) {
      final RecordField f = fields.value(i + 1);
      final SeqType st = f.seqType();
      params[i] = f.isOptional() ? st.union(Occ.ZERO) : st;
    }

    final Supplier<RecordConstructor> supplier = () -> new RecordConstructor(rt);
    return new FuncDefinition(supplier, description, params, rt.seqType(),
        EnumSet.noneOf(Flag.class), rt.name() == null ? Token.EMPTY : rt.name().uri(), Perm.NONE);
  }
}
