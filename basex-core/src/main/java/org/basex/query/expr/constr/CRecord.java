package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Record type constructor function.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CRecord extends Arr {
  /**
   * Constructor.
   * @param type record type
   * @param info input info (can be {@code null})
   * @param args record values
   */
  public CRecord(final InputInfo info, final Type type, final Expr[] args) {
    super(info, type.seqType(), args);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return values(true, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final RecordType rt = (RecordType) seqType().type;
    if(rt.hasOptional()) {
      final MapBuilder mb = new MapBuilder(exprs.length);
      final TokenObjectMap<RecordField> fields = rt.fields();
      final int fs = fields.size();
      for(int f = 1; f <= fs; f++) {
        boolean add = true;
        final Value value = exprs[f - 1].value(qc);
        if(value == Empty.VALUE) {
          final RecordField rf = fields.value(f);
          if(rf.isOptional() && rf.expr() == null) add = false;
        }
        if(add) mb.put(fields.key(f), value);
      }
      return mb.map();
    }

    // create compact record map
    final int el = exprs.length;
    final Value[] values = new Value[el];
    for(int e = 0; e < el; e++) values[e] = exprs[e].value(qc);
    return new XQRecordMap(values, rt);
  }

  @Override
  public long structSize() {
    final RecordType rt = (RecordType) seqType().type;
    return rt.hasOptional() ? -1 : exprs.length;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CRecord(info, seqType().type, copyAll(cc, vm, args())));
  }

  @Override
  public String description() {
    return RECORD + " constructor";
  }

  @Override
  public void toString(final QueryString qs) {
    final RecordType rt = (RecordType) seqType().type;
    final QNm name = rt.name();
    if(name != null) {
      qs.token(name).params(exprs);
    } else {
      qs.token("{ ");
      int e = -1;
      for(final byte[] key : rt.fields()) {
        if(++e != 0) qs.token(',');
        qs.quoted(key).token(':').token(exprs[e]);
      }
      qs.token(" }");
    }
  }
}
