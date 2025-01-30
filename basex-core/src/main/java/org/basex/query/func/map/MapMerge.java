package org.basex.query.func.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public class MapMerge extends StandardFunc {
  /** Duplicate handling. */
  public enum Duplicates {
    /** Reject.    */ REJECT,
    /** Use first. */ USE_FIRST,
    /** Use last.  */ USE_LAST,
    /** Use any.   */ USE_ANY,
    /** Combine.   */ COMBINE;

    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }

  /** Merge options. */
  public static final class MergeOptions extends Options {
    /** Handle duplicates. */
    public static final EnumOption<Duplicates> DUPLICATES =
        new EnumOption<>("duplicates", Duplicates.class);
    /** Combine function. */
    public static final ValueOption COMBINE =
        new ValueOption("combine", SeqType.FUNCTION_ZO);
  }

  /** Cached value merger instance. */
  ValueMerger vm;

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter maps = arg(0).iter(qc);
    final MergeOptions options = toOptions(arg(1), new MergeOptions(), qc);
    final ValueMerger merger = merger(options, qc, Duplicates.USE_FIRST);

    // empty input: return empty map
    final Item first = qc.next(maps);
    if(first == null) return XQMap.empty();

    // single input: return single map
    XQMap mp = toMap(first);
    Item current = qc.next(maps);
    if(current == null) return mp;

    // update first map if 2 maps are supplied, use map builder otherwise
    Item next = qc.next(maps);
    final MapBuilder mb = next == null ? null : new MapBuilder(arg(0).size());
    if(mb != null) mp.forEach((k, v) -> mb.put(k,  v));

    while(current != null) {
      final XQMap map = toMap(current);
      for(final Item key : map.keys()) {
        final Value old = mb != null ? mb.get(key) : mp.getInternal(key, false);
        final Value val = merger.merge(key, old, map.get(key));
        if(val != null) {
          if(mb != null) mb.put(key, val);
          else mp = mp.put(key, val);
        }
      }
      current = next;
      next = current != null ? qc.next(maps) : null;
    }
    return mb != null ? mb.map() : mp;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    prepareMerge(1, Duplicates.USE_FIRST, cc);

    if(arg(0).seqType().type instanceof MapType) {
      // remove empty entries
      if(arg(0) instanceof List &&
          ((Checks<Expr>) arg -> arg == XQMap.empty()).any(arg(0).args())) {
        final ExprList list = new ExprList();
        for(final Expr arg : arg(0).args()) {
          if(arg != XQMap.empty()) list.add(arg);
        }
        arg(0, arg -> List.get(cc, info, list.finish()));
      }
      // return simple arguments
      final SeqType st = arg(0).seqType();
      if(st.one()) return arg(0);

      // rewrite map:merge(map:entry, map) to map:put(map, key, value)
      if(!defined(1) && arg(0) instanceof List && arg(0).args().length == 2) {
        final Expr[] args = arg(0).args();
        if(_MAP_ENTRY.is(args[0]) && args[1].seqType().instanceOf(SeqType.MAP_O)) {
          return cc.function(_MAP_PUT, info, args[1], args[0].arg(0), args[0].arg(1));
        }
      }

      // consider duplicate handling for value type
      //   map:merge((1 to 2) ! map { 1: 1 }, map { 'duplicates': 'combine' })
      final MapType mt = (MapType) st.type;
      final Type kt = mt.keyType;
      final SeqType vt = mt.valueType;
      exprType.assign(MapType.get(kt, vm != null ? vm.type(vt) : SeqType.ITEM_ZM));
    }
    return this;
  }

  /**
   * Optimizes merge operations.
   * @param arg options argument
   * @param dflt default duplicate operation
   * @param cc compilation context
   * @throws QueryException query exception
   */
  final void prepareMerge(final int arg, final Duplicates dflt, final CompileContext cc)
      throws QueryException {
    if(arg(arg) instanceof Value) {
      MergeOptions options = new MergeOptions();
      if(defined(arg)) options = toOptions(arg(arg), options, cc.qc);
      vm = merger(options, cc.qc, dflt);
    }
  }

  /**
   * Creates a value merger.
   * @param options merge options
   * @param qc query context
   * @param dflt default duplicate operation
   * @return merger
   * @throws QueryException query exception
   */
  final ValueMerger merger(final MergeOptions options, final QueryContext qc, final Duplicates dflt)
      throws QueryException {

    if(vm != null) return vm;

    Duplicates duplicates = options.get(MergeOptions.DUPLICATES);
    final Value combine = options.get(MergeOptions.COMBINE);
    if(!combine.isEmpty()) {
      if(duplicates != null) throw OPTIONS_X_X.get(info, MergeOptions.DUPLICATES.name(),
          MergeOptions.COMBINE.name());
      return new Invoke(toFunction(combine, 2, qc), info, qc);
    }

    if(duplicates == null) duplicates = dflt;
    switch(duplicates) {
      case REJECT:    return new Reject(info);
      case COMBINE:   return new Combine(qc);
      case USE_FIRST: return new UseFirst();
      default:        return new UseLast();
    }
  }

  @Override
  public int hofIndex() {
    return defined(1) ? Integer.MAX_VALUE : -1;
  }
}
