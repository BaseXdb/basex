package org.basex.query.expr.constr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Named record type constructor function.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class CRecord extends Arr {
  /** Record type. */
  private final RecordType recordType;

  /**
   * Constructor.
   * @param recordType record type
   * @param info input info (can be {@code null})
   * @param args function arguments
   */
  public CRecord(final InputInfo info, final RecordType recordType, final Expr[] args) {
    super(info, recordType.seqType(), args);
    this.recordType = recordType;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return values(true, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder mb = new MapBuilder();
    final Expr[] args = args();
    int i = 0;
    for(final Iterator<byte[]> iter = recordType.iterator(); iter.hasNext();) {
      final byte[] key = iter.next();
      final Value value = args[i++].value(qc);
      final boolean omit;
      if(value.isEmpty()) {
        final RecordField rf = recordType.field(key);
        omit = rf.isOptional() && rf.expr() == null;
      } else {
        omit = false;
      }
      if(!omit) mb.put(key, value);
    }
    if(recordType.isExtensible()) {
      toMap(arg(i), qc).forEach((k, v) -> {
        if(!mb.contains(k)) mb.put(k, v);
      });
    }
    return mb.map();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CRecord(info, recordType, copyAll(cc, vm, args())));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(recordType.name()).params(exprs);
  }
}
