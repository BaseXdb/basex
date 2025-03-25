package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
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
   * @param args function arguments
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
    final boolean ext = rt.isExtensible();
    if(ext || ((Checks<byte[]>) rf -> rt.field(rf).isOptional()).any(rt.fields())) {
      final MapBuilder mb = new MapBuilder();
      int e = 0;
      for(final byte[] key : rt.fields()) {
        final Value value = exprs[e++].value(qc);
        boolean add = true;
        if(value.isEmpty()) {
          final RecordField rf = rt.field(key);
          if(rf.isOptional() && rf.expr() == null) add = false;
        }
        if(add) mb.put(key, value);
      }
      if(ext) {
        toMap(arg(e), qc).forEach((k, v) -> {
          if(!mb.contains(k)) mb.put(k, v);
        });
      }
      return mb.map();
    }

    // create compact record map
    final ValueList values = new ValueList(rt.fields().size());
    for(final Expr expr : exprs) values.add(expr.value(qc));
    return new XQRecordMap(values.finish(), rt);
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
