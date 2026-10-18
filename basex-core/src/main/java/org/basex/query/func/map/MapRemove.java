package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapRemove extends MapFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    XQMap map = toMap(arg(0), qc);
    final Iter keys = arg(1).atomIter(qc, info);

    for(Item item; (item = qc.next(keys)) != null;) {
      map = map.remove(toAtomItem(item, qc));
    }
    return map.unannotated();
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    super.simplifyArgs(cc);
    // map:remove($map, replicate('a', 2)) → map:remove($map, 'a')
    arg(1, arg -> arg.simplifyFor(Simplify.SET, cc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1);
    // map:remove({}, $keys) → {}
    if(map == XQMap.empty()) return map;

    final MapTypeInfo mti = MapTypeInfo.get(map).key(key);
    if(mti.index != 0) {
      // remove the only field of a record: map:remove(RECORD, FIELD) → {}
      if(mti.shape.fields().size() == 1) return cc.voidAndReturn(map, XQMap.empty(), info);
      // narrow the shape: map:remove({ 'a': 1, 'b': 2 }, 'a') → map with field b
      exprType.assign(cc.qc.shared.shape(mti.shape.remove(mti.shape.fields().key(mti.index))));
      return this;
    }
    // return input map if nothing changes: map:remove({ 1: 2 }, 'a') → { 1: 2 }
    if(mti.validKey || mti.keyMismatch) {
      if(noRecords(map)) return map;
      // drop the record annotation, keep the shape
      if(mti.shape != null) {
        exprType.assign(cc.qc.shared.shape(mti.shape.shape()));
        return this;
      }
    }
    if(mti.mapType != null) exprType.assign(MapType.get(mti.mapType));
    return this;
  }
}
