package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ProfTrack extends StandardFunc {
  /** Profile Options. */
  public static final class TrackOptions extends Options {
    /** Value. */
    public static final BooleanOption VALUE = new BooleanOption("value", true);
    /** Time. */
    public static final BooleanOption TIME = new BooleanOption("time", true);
    /** Memory. */
    public static final BooleanOption MEMORY = new BooleanOption("memory", true);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TrackOptions opts = toOptions(1, new TrackOptions(), qc);

    final boolean value = opts.get(TrackOptions.VALUE);
    Performance perf = null;
    long min = -1;
    if(opts.get(TrackOptions.MEMORY)) {
      Performance.gc(4);
      min = Performance.memory();
    }
    if(opts.get(TrackOptions.TIME)) {
      perf = new Performance();
    }

    final Value v = qc.value(exprs[0]);
    Map map = Map.EMPTY;
    if(perf != null) {
      final long time = perf.ns();
      map = map.put(Str.get(TrackOptions.TIME.name()), Dbl.get(Performance.ms(time, 1)), info);
    }
    if(min != -1) {
      Performance.gc(2);
      final long mem = Math.max(0, Performance.memory() - min);
      map = map.put(Str.get(TrackOptions.MEMORY.name()), Int.get(mem), info);
    }
    if(value) {
      map = map.put(Str.get(TrackOptions.VALUE.name()), v, info);
    }
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return adoptType(exprs[0]);
  }
}
