package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
    if(st.type instanceof final RecordType rt) {
      // the update supplies every field: drop the left operand
      if(covered(exprs[1], rt)) {
        // RECORD +:= local:rec(1, 2) → local:rec(1, 2): build the record type directly …
        final Expr direct = retarget(exprs[1], rt, cc);
        // … otherwise RECORD +:= { 'a': 1, 'b': 2 } → { 'a': 1, 'b': 2 } coerce to RECORD
        return cc.replaceWith(this, direct != null ? direct :
          new TypeCheck(info, exprs[1], rt.seqType()).optimize(cc));
      }
      exprType.assign(st.with(Occ.EXACTLY_ONE));
    }
    return this;
  }

  /**
   * Builds the record type directly if the update is a constructor supplying exactly its fields.
   * @param update update expression (right operand)
   * @param rt record type of the left operand
   * @param cc compilation context
   * @return direct record constructor, or {@code null} if not applicable
   * @throws QueryException query exception
   */
  private Expr retarget(final Expr update, final RecordType rt, final CompileContext cc)
      throws QueryException {
    if(!(update instanceof final RecordConstructor rc) ||
        !(update.seqType().type instanceof final RecordType urt) ||
        urt.fields().size() != rt.fields().size()) return null;
    final TokenObjectMap<RecordField> ufields = urt.fields(), fields = rt.fields();
    final int fs = fields.size();
    final Expr[] args = new Expr[fs];
    for(int f = 1; f <= fs; f++) args[f - 1] = rc.arg(ufields.index(fields.key(f)) - 1);
    return RecordConstructor.get(info, rt, args).optimize(cc);
  }

  /**
   * Checks whether the update supplies every field of the record type.
   * @param update update expression (right operand)
   * @param rt record type of the left operand
   * @return result of check
   */
  private static boolean covered(final Expr update, final RecordType rt) {
    if(!(update instanceof RecordConstructor || update instanceof XQMap) ||
        !(update.seqType().type instanceof final RecordType urt)) return false;
    final TokenObjectMap<RecordField> ufields = urt.fields(), fields = rt.fields();
    final int fs = fields.size();
    for(int f = 1; f <= fs; f++) {
      if(!ufields.contains(fields.key(f))) return false;
    }
    return true;
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

    // compact record layout
    if(record instanceof final XQRecordMap rec) {
      final TokenObjectMap<RecordField> fields = rt.fields();
      final int fs = fields.size();
      final Value[] values = new Value[fs];
      for(int f = 0; f < fs; f++) values[f] = rec.valueAt(f);
      update.forEach((key, value) -> {
        final int i = key.type.isStringOrUntyped() ? fields.index(key.string(null)) : 0;
        if(i == 0) throw typeError(update, rt, info);
        values[i - 1] = fields.value(i).seqType().coerce(value, qc, info, null, null);
      });
      return new XQRecordMap(rt, values);
    }

    // fallback
    final MapBuilder mb = new MapBuilder();
    record.forEach((key, value) -> mb.put(key, value));
    update.forEach((key, value) -> mb.put(key, value));
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
