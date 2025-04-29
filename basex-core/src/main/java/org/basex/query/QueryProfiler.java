package org.basex.query;

import java.util.*;
import java.util.Map.*;
import java.util.function.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Query profiler.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryProfiler {
  /** Runtime. */
  public final Map<String, LongList> timing = Collections.synchronizedMap(new LinkedHashMap<>());
  /** Memory consumption. */
  public final Map<String, LongList> memory = Collections.synchronizedMap(new LinkedHashMap<>());

  /** Query context. */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param qc query context
   */
  public QueryProfiler(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Evaluates the result.
   * @param result profiled result
   * @param label label (can be {@code null})
   * @param time profile time/memory
   * @param aggregate aggregate result
   */
  public void evaluate(final long result, final String label, final boolean time,
      final boolean aggregate) {
    if(aggregate) {
      add(result, label, time);
    } else {
      qc.trace(label, () -> time ? Performance.formatNano(result) :
        Performance.formatHuman(result));
    }
  }

  /**
   * Adds a profiled result.
   * @param result measured result
   * @param label label (can be {@code null})
   * @param time profile time/memory
   */
  public void add(final long result, final String label, final boolean time) {
    final Map<String, LongList> map = time ? timing : memory;
    final String lbl = label != null ? label : "";

    LongList values = map.get(lbl);
    if(values == null) {
      if(map.size() >= 1000) return;
      values = new LongList();
      map.put(lbl, values);
    }
    values.add(result);
  }

  /**
   * Outputs the aggregated runtime results.
   */
  public void finish() {
    final Consumer<Map<String, LongList>> trace = map -> {
      for(final Entry<String, LongList> entry : map.entrySet()) {
        qc.trace(entry.getKey(), () -> {
          final LongList values = entry.getValue();
          final int runs = values.size();
          long min = Long.MAX_VALUE, max = Long.MIN_VALUE, sum = 0;
          for(final long l : values.toArray()) {
            if(l < min) min = l;
            if(l > max) max = l;
            sum += l;
          }
          return map == timing ?
              Performance.formatNano(sum) + " (" + runs + " runs, avg: " +
              Performance.nanoToMilli(sum, runs) + ", min: " +
              Performance.nanoToMilli(min) + ", max: " +
              Performance.nanoToMilli(max) + ")" :
            Performance.formatHuman(sum / runs) + " (avg, " + runs + " runs)";
        });
      }
    };
    trace.accept(timing);
    trace.accept(memory);
  }
}
