package org.basex.query.func.prof;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
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
    public static final BooleanOption MEMORY = new BooleanOption("memory", false);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TrackOptions options = toOptions(arg(1), new TrackOptions(), true, qc);

    // include memory consumption
    long min = -1;
    if(options.get(TrackOptions.MEMORY)) {
      Performance.gc(4);
      min = Performance.memory();
    }
    // include execution time (called after garbage collection)
    Performance perf = null;
    if(options.get(TrackOptions.TIME)) {
      perf = new Performance();
    }
    // include resulting value
    Value value = null;
    if(options.get(TrackOptions.VALUE)) {
      // retrieve and assign value
      value = arg(0).value(qc);
    } else {
      // iterate through results; skip iteration if iterator is based on a value
      final Iter iter = arg(0).iter(qc);
      if(!iter.valueIter()) {
        while(qc.next(iter) != null);
      }
    }

    final MapBuilder mb = new MapBuilder();
    // execution time (called before garbage collection)
    if(perf != null) {
      final BigDecimal ms = BigDecimal.valueOf(perf.ns()).divide(Dec.BD_1000000,
          MathContext.DECIMAL64);
      mb.put(TrackOptions.TIME.name(), Dec.get(ms));
    }
    // memory consumption
    if(min != -1) {
      Performance.gc(2);
      mb.put(TrackOptions.MEMORY.name(), Int.get(Math.max(0, Performance.memory() - min)));
    }
    // evaluated value
    if(value != null) mb.put(TrackOptions.VALUE.name(), value);
    return mb.map();
  }
}
