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
  /** The size of a structure resulting from this constructor. */
  private final long structSize;
  /** The map builder function. */
  private final QueryBiFunction<QueryContext, InputInfo, XQMap> builder;
  /** Field names. */
  private final QNm[] names;

  /**
   * Constructor.
   * @param recordType record type
   */
  public RecordConstructor(final RecordType recordType) {
    this.recordType = recordType;
    final TokenObjectMap<RecordField> fields = recordType.fields();
    final int fs = fields.size();
    names = new QNm[fs];
    boolean complete = true;
    for(int i = 1; i <= fs; ++i) {
      names[i - 1] = new QNm(fields.key(i));
      complete = complete && fields.value(i).alwaysAdded();
    }
    if(complete) {
      builder = this::recordMap;
      structSize = fs;
    } else {
      builder = this::map;
      structSize = -1;
    }
  }

  /**
   * Returns a constructor function for a record type.
   * @param rt record type
   * @param ii input info
   * @param args constructor arguments
   * @return constructor function
   */
  public static RecordConstructor get(final InputInfo ii, final RecordType rt, final Expr[] args) {
    RecordConstructor constructor = new RecordConstructor(rt);
    constructor.init(ii, definition(rt), args);
    return constructor;
  }

  @Override
  public Expr opt(final CompileContext cc) throws QueryException {
    return values(true, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return builder.apply(qc, ii);
  }

  @Override
  public long structSize() {
    return structSize;
  }

  /**
   * Creates a compact record map. This is more efficient than the regular map implementation, but
   * only possible if all fields of the type are present.
   * @param qc query context
   * @param ii input info
   * @return record map
   * @throws QueryException query exception
   */
  private XQRecordMap recordMap(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenObjectMap<RecordField> fields = recordType.fields();
    final int fs = fields.size();
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; ++f) {
      final RecordField rf = fields.value(f + 1);
      final Value value = f < exprs.length ? exprs[f].value(qc) : rf.init().value(qc);
      values[f] = rf.seqType().coerce(value, names[f], qc, null, ii);
    }
    return new XQRecordMap(values, recordType);
  }

  /**
   * Creates a regular map. This is used if some fields of the record type may be absent.
   * @param qc query context
   * @param ii input info
   * @return map
   * @throws QueryException query exception
   */
  private XQMap map(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenObjectMap<RecordField> fields = recordType.fields();
    final int fs = fields.size();
    final MapBuilder mb = new MapBuilder(fs);
    for(int f = 0; f < fs; ++f) {
      final RecordField rf = fields.value(f + 1);
      final Value value = f < exprs.length ? exprs[f].value(qc) : rf.init().value(qc);
      if(!value.isEmpty() || rf.alwaysAdded()) {
        mb.put(fields.key(f + 1), rf.seqType().coerce(value, names[f], qc, null, ii));
      }
    }
    final XQMap map = mb.map();
    map.type = recordType;
    return map;
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

    Supplier<RecordConstructor> supplier = () -> new RecordConstructor(rt);
    return new FuncDefinition(supplier, description, params, rt.seqType(),
        EnumSet.noneOf(Flag.class), rt.name() == null ? Token.EMPTY : rt.name().uri(), Perm.NONE);
  }
}