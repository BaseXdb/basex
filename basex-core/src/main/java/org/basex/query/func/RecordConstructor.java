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
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Constructor function for some record type.
 */
public class RecordConstructor extends StandardFunc {
  /** Record type. */
  private RecordType recordType;
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
    for(int f = 1; f <= fs; ++f) {
      names[f - 1] = new QNm(fields.key(f));
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
      final Value value = f < el ? exprs[f].value(qc) :
          rf.init() != null ? rf.init().value(qc) : Empty.VALUE;
      values[f] = rf.seqType().coerce(value, qc, ii, names[f], null);
    }
    return new XQRecordMap(recordType, values);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // refine the field types of an inferred (anonymous) record from the argument types
    if(recordType.name() == null) {
      final TokenObjectMap<RecordField> fields = recordType.fields();
      final int fs = fields.size();
      if(fs == exprs.length) {
        final TokenObjectMap<RecordField> refined = new TokenObjectMap<>(fs);
        boolean narrowed = false;
        for(int f = 0; f < fs; f++) {
          final SeqType ost = fields.value(f + 1).seqType(), nst = exprs[f].seqType();
          final SeqType st = nst.instanceOf(ost) ? nst : ost;
          if(!st.eq(ost)) narrowed = true;
          refined.put(fields.key(f + 1), new RecordField(st));
        }
        if(narrowed) {
          recordType = cc.qc.shared.record(new RecordType(refined));
          exprType.assign(recordType.seqType());
        }
      }
    }
    return this;
  }

  @Override
  public long structSize() {
    return recordType.fields().size();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final RecordConstructor rc &&
        recordType.eq(rc.recordType) && Array.equals(exprs, rc.exprs);
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
      params[i] = f.seqType();
    }

    final Supplier<RecordConstructor> supplier = () -> new RecordConstructor(rt);
    return new FuncDefinition(supplier, description, params, rt.seqType(),
        EnumSet.noneOf(Flag.class), rt.name() == null ? Token.EMPTY : rt.name().uri(), Perm.NONE);
  }
}
