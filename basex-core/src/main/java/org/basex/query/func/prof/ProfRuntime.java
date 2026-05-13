package org.basex.query.func.prof;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfRuntime extends StandardFunc {
  /** Runtime option. */
  public enum RuntimeOption {
    /** Used memory.          */ USED(rt -> rt.totalMemory() - rt.freeMemory()),
    /** Total memory.         */ TOTAL(Runtime::totalMemory),
    /** Maximum memory.       */ MAX(Runtime::maxMemory),
    /** Available processors. */ PROCESSORS(Runtime::availableProcessors);

    /** Value supplier. */
    private final ToLongFunction<Runtime> fn;

    /**
     * Constructor.
     * @param fn value supplier
     */
    RuntimeOption(final ToLongFunction<Runtime> fn) {
      this.fn = fn;
    }

    /**
     * Returns the current value of this option.
     * @param rt runtime
     * @return value
     */
    public long apply(final Runtime rt) {
      return fn.applyAsLong(rt);
    }

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item option = arg(0).atomItem(qc, info);
    final Runtime rt = Runtime.getRuntime();

    if(option.isEmpty()) {
      final MapBuilder map = new MapBuilder();
      for(final RuntimeOption opt : RuntimeOption.values()) {
        map.put(opt.toString(), Itr.get(opt.apply(rt)));
      }
      return map.map();
    }
    return Itr.get(toEnum(option, RuntimeOption.class).apply(rt));
  }
}
