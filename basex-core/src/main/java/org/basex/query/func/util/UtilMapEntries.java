package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilMapEntries extends StandardFunc {
  /** Key. */
  private static final Str KEY = Str.get("key");
  /** Value. */
  private static final Str VALUE = Str.get("value");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);

    return new Iter() {
      final Iter keys = map.keys().iter();

      @Override
      public XQMap next() throws QueryException {
        final Item key = keys.next();
        return key != null ? entry(map, key) : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item key : map.keys()) vb.add(entry(map, key));
    return vb.value(this);
  }

  /**
   * Creates a single map entry.
   * @param map map
   * @param key key
   * @return created map entry
   * @throws QueryException query exception
   */
  private XQMap entry(final XQMap map, final Item key) throws QueryException {
    return XQMap.EMPTY.put(KEY, key, info).put(VALUE, map.get(key, info), info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = exprs[0].funcType();
    if(ft instanceof MapType) {
      final MapType mt = (MapType) ft;
      final SeqType dt = ft.declType.union(mt.keyType().seqType()).with(Occ.ZERO_OR_MORE);
      exprType.assign(MapType.get(AtomType.STRING, dt));
    }
    return this;
  }
}
