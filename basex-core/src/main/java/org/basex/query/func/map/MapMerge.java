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
 * @author Christian Gruen
 */
public class MapMerge extends MapFn {
  /** Duplicate handling. */
  public enum Duplicates {
    /** Reject.    */ REJECT,
    /** Use first. */ USE_FIRST,
    /** Use last.  */ USE_LAST,
    /** Use any.   */ USE_ANY,
    /** Combine.   */ COMBINE;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Merge options. */
  public static final class MergeOptions extends Options {
    /** Duplicates handling. */
    public static final ValueOption DUPLICATES = new ValueOption("duplicates", Types.ITEM_ZM);
  }

  /** Cached value merger instance. */
  MapDuplicates md;

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter maps = arg(0).iter(qc);
    final MergeOptions options = toOptions(arg(1), new MergeOptions(), qc);
    final MapDuplicates dups = duplicates(options, qc, Duplicates.USE_FIRST);

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
    if(mb != null) mp.forEach(mb::put);

    while(current != null) {
      final XQMap map = toMap(current);
      for(final Item key : map.keys()) {
        final Value old = mb != null ? mb.get(key) : mp.getOrNull(key);
        final Value val = dups.merge(key, old, map.get(key));
        if(val != null) {
          if(mb != null) mb.put(key, val);
          else mp = mp.put(key, val);
        }
      }
      current = next;
      next = current != null ? qc.next(maps) : null;
    }
    return mb != null ? mb.map(this) : mp;
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
        if(_MAP_ENTRY.is(args[0]) && args[1].seqType().instanceOf(Types.MAP_O)) {
          return cc.function(_MAP_PUT, info, args[1], args[0].arg(0), args[0].arg(1));
        }
      }
      final MapType mt = (MapType) st.type;
      assignType(mt.keyType(), mt.valueType());
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
      md = duplicates(options, cc.qc, dflt);
    }
  }

  /**
   * Creates a merger for duplicate values.
   * @param options merge options
   * @param qc query context
   * @param dflt default duplicate operation
   * @return merger
   * @throws QueryException query exception
   */
  final MapDuplicates duplicates(final MergeOptions options, final QueryContext qc,
      final Duplicates dflt) throws QueryException {

    // return static options instance
    if(md != null) return md;
    // function
    final Value duplicates = options.get(MergeOptions.DUPLICATES);
    if(duplicates instanceof FItem) return new Invoke(toFunction(duplicates, 2, qc), qc);
    // fixed option
    final String string = duplicates.isEmpty() ? dflt.toString() : toString(duplicates, qc);
    final Duplicates value = Enums.get(Duplicates.class, string);
    if(value == null) throw typeError(duplicates,
        EnumType.get(Duplicates.values()), info);

    return switch(value) {
      case REJECT -> new Reject();
      case COMBINE -> new Combine(qc);
      case USE_FIRST -> new UseFirst();
      default -> new UseLast();
    };
  }

  /**
   * Assigns a map type for this expression.
   * @param kt key type (can be {@code null})
   * @param vt value type
   */
  final void assignType(final Type kt, final SeqType vt) {
    exprType.assign(MapType.get(kt != null ? kt : AtomType.ANY_ATOMIC_TYPE,
      md != null ? md.type(vt) : Types.ITEM_ZM));
  }

  @Override
  public int hofOffsets() {
    return functionOption(1) ? Integer.MAX_VALUE : 0;
  }

  /**
   * Return {@code null} to indicate that insertion can be skipped.
   */
  static final class UseFirst extends MapDuplicates {
    @Override
    Value get(final Item key, final Value old, final Value value) {
      return null;
    }
  }

  /**
   * Return new value.
   */
  static final class UseLast extends MapDuplicates {
    @Override
    Value get(final Item key, final Value old, final Value value) {
      return value;
    }
  }

  /**
   * Concatenate values.
   */
  static final class Combine extends MapDuplicates {
    /** Query context. */
    private final QueryContext qc;

    /**
     * Constructor.
     * @param qc query context
     */
    Combine(final QueryContext qc) {
      this.qc = qc;
    }

    @Override
    Value get(final Item key, final Value old, final Value value) {
      return old.append(value, qc);
    }

    @Override
    SeqType type(final SeqType st) {
      return st.union(st.occ.add(st.occ));
    }
  }

  /**
   * Reject merge.
   */
  final class Reject extends MapDuplicates {
    @Override
    Value get(final Item key, final Value old, final Value value) throws QueryException {
      throw MERGE_DUPLICATE_X.get(info, key);
    }
  }

  /**
   * Invoke function to combine values.
   */
  final class Invoke extends MapDuplicates {
    /** Combiner function. */
    private final FItem function;
    /** Query context. */
    private final QueryContext qc;
    /** HOF arguments. */
    private final HofArgs args;

    /**
     * Constructor.
     * @param function combiner function
     * @param qc query context
     */
    Invoke(final FItem function, final QueryContext qc) {
      this.function = function;
      this.qc = qc;
      args = new HofArgs(2);
    }

    @Override
    Value get(final Item key, final Value old, final Value value) throws QueryException {
      return function.invoke(qc, info, args.set(0, old).set(1, value).get());
    }

    @Override
    SeqType type(final SeqType st) {
      return st.union(function.funcType().declType);
    }
  }
}
