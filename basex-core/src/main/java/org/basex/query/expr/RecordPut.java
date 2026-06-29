package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Record put expression ({@code +:=}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RecordPut extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param record record expression (left operand)
   * @param update update expression (right operand)
   */
  public RecordPut(final InputInfo info, final Expr record, final Expr update) {
    super(info, Types.RECORD_O, record, update);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // RECORD +:= { ... } +:= { ... } → RECORD +:= { ..., ... }
    if(exprs[0] instanceof final RecordPut rp && rp.exprs[1] instanceof final XQMap update1 &&
        exprs[1] instanceof final XQMap update2 && disjoint(update1, update2)) {
      final MapBuilder mb = new MapBuilder();
      update1.forEach((key, value) -> mb.put(key, value));
      update2.forEach((key, value) -> mb.put(key, value));
      exprs = new Expr[] { rp.exprs[0], mb.map() };
    }
    // the result carries the record type of the left operand
    final SeqType st = exprs[0].seqType();
    if(st.type instanceof RecordType) exprType.assign(st.with(Occ.EXACTLY_ONE));
    return this;
  }

  /**
   * Checks if two maps have disjoint keys.
   * @param map1 first map
   * @param map2 second map
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean disjoint(final XQMap map1, final XQMap map2) throws QueryException {
    for(final Item key : map1.keys()) {
      if(map2.contains(key)) return false;
    }
    return true;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap record = toMap(exprs[0], qc);
    if(!(record.type instanceof final RecordType rt)) throw typeError(record, Types.RECORD, info);
    final XQMap update = toMap(exprs[1], qc);
    // merge the operands (right values take precedence: duplicates "use-last")
    final MapBuilder mb = new MapBuilder();
    record.forEach((key, value) -> mb.put(key, value));
    update.forEach((key, value) -> mb.put(key, value));
    // coerce the merged map to the record type of the left operand (rejects undeclared fields)
    return mb.map().coerceTo(rt, qc, info, null);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new RecordPut(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof RecordPut && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, " +:= ", true);
  }
}
