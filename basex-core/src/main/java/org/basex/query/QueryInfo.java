package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class remembers descriptive query information sent back to the client.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryInfo {
  /** Maximum byte size for compilation and evaluation output. */
  private static final int MAX = 1 << 20;
  /** Maximum byte size for compilation and evaluation output per line. */
  private static final int MAX_LINE = 1 << 14;

  /** Section key. */
  public static final String TIMING = "timing";
  /** Section key. */
  public static final String EVALUATION = "evaluation";
  /** Section key. */
  public static final String RESULT = "result";
  /** Section key. */
  public static final String OPTIMIZED_QUERY = "optimized-query";
  /** Section key. */
  public static final String OPTIMIZATION = "optimization";
  /** Section key. */
  public static final String COMPILATION = "compilation";
  /** Section key. */
  public static final String QUERY = "query";
  /** Section key: the error message of a failed query. */
  public static final String ERROR = "error";
  /** Section key. */
  public static final String PLAN = "plan";
  /** Entry key of the total time. */
  public static final String TOTAL = "total";

  /** Parsing time (nanoseconds). */
  public final AtomicLong parsing = new AtomicLong();
  /** Compilation time (nanoseconds). */
  public final AtomicLong compiling = new AtomicLong();
  /** Optimization time (nanoseconds). */
  public final AtomicLong optimizing = new AtomicLong();
  /** Evaluation time (nanoseconds). */
  public final AtomicLong evaluating = new AtomicLong();
  /** Serialization time (nanoseconds). */
  public final AtomicLong serializing = new AtomicLong();

  /** Compilation info. */
  private final Infos compile = new Infos();
  /** Optimization info. */
  private final Infos optimize = new Infos();
  /** Evaluation info. */
  private final Infos evaluate = new Infos();

  /** Verbose info. */
  private final boolean queryinfo;
  /** Number of runs. */
  private final int runs;

  /** Runtime flag. */
  boolean runtime;
  /** Query string (can be {@code null}). */
  String query;
  /** Serialized query plan (can be {@code null}). */
  private String queryPlan;

  /**
   * Constructor.
   * @param context database context
   */
  public QueryInfo(final Context context) {
    this(context, context.options.get(MainOptions.QUERYINFO));
  }

  /**
   * Constructor with a custom verbosity.
   * @param context database context
   * @param queryinfo collect verbose query information
   */
  public QueryInfo(final Context context, final boolean queryinfo) {
    this.queryinfo = queryinfo;
    runs = Math.max(1, context.options.get(MainOptions.RUNS));
  }

  /**
   * Resets info strings.
   */
  public void reset() {
    compile.reset();
    optimize.reset();
    evaluate.reset();
  }

  /**
   * Adds some compilation info.
   * @param dynamic dynamic compilation
   * @param string evaluation info
   * @param ext text text extensions
   */
  void compInfo(final boolean dynamic, final String string, final Object... ext) {
    final Infos infos = dynamic ? optimize : compile;
    if(queryinfo && !infos.full()) {
      final TokenList list = new TokenList(ext.length);
      for(final Object e : ext) list.add(QueryError.normalize(e, null));
      String info = Util.info(string, (Object[]) list.finish());
      if(!info.isEmpty()) {
        if(runtime) {
          info = "RUNTIME: " + info;
          if(Prop.debug) Util.stack(info);
        }
        infos.add(token(info));
      }
    }
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  void evalInfo(final String string) {
    if(queryinfo && !evaluate.full()) {
      evaluate.add(chop(token(string), MAX_LINE));
    }
  }

  /**
   * Returns detailed query information.
   * @param qp query processor
   * @param printed printed bytes
   * @param hits number of returned hits
   * @param locks read and write locks (can be {@code null})
   * @param success success flag
   * @return query string
   */
  public String toString(final QueryProcessor qp, final long printed, final long hits,
      final Locks locks, final boolean success) {

    final TokenBuilder tb = new TokenBuilder();
    if(queryinfo) {
      // the command line has an order of its own: it follows the phases of a query
      final Map<String, Section> sections = toSections(qp, printed, hits, locks).sections();
      for(final String key : new String[] { QUERY, COMPILATION, OPTIMIZATION,
          OPTIMIZED_QUERY, EVALUATION, TIMING, RESULT }) {
        final Section section = sections.get(key);
        if(section == null) continue;
        // an empty line precedes a section: the last one is not followed by one
        tb.add(NL).add(Strings.titleCase(key)).add(COL).add(NL);
        for(final String line : lines(section)) tb.add(line).add(NL);
      }
    }
    if(success) {
      final IO baseIO = qp.sc.baseIO();
      final String name = baseIO == null ? "" : " \"" + baseIO.name() + '"';
      tb.add(NL).addExt(QUERY_EXECUTED_X_X, name, Performance.formatNano(total(), runs));
    }
    return tb.toString();
  }

  /**
   * Returns structured query information.
   * @param qp query processor
   * @param hits number of returned hits
   * @param locks read and write locks (can be {@code null})
   * @return query information
   * @throws QueryException query exception
   */
  public XQMap toMap(final QueryProcessor qp, final long hits, final Locks locks)
      throws QueryException {

    // the query plan is part of the information
    plan(qp);
    final MapBuilder mb = new MapBuilder();
    for(final Map.Entry<String, Section> section :
        toSections(qp, -1, hits, locks).sections().entrySet()) {
      mb.put(section.getKey(), value(section.getValue(), qp.qc));
    }
    return mb.map();
  }

  /**
   * Returns the value of a section: a string for a block of text, a map for labeled entries,
   * and an array for a plain list.
   * @param section section
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  private static Value value(final Section section, final QueryContext qc) throws QueryException {
    final List<Entry> entries = section.entries();
    if(section.text()) return Str.get(entries.get(0).value());
    if(entries.get(0).key() != null) {
      final MapBuilder mb = new MapBuilder();
      for(final Entry entry : entries) mb.put(entry.key(), entry.value());
      return mb.map();
    }
    final ArrayBuilder ab = new ArrayBuilder(qc, entries.size());
    for(final Entry entry : entries) ab.add(Str.get(entry.value()));
    return ab.array();
  }

  /**
   * Returns the query information, section by section; every output chooses its own order.
   * @param qp query processor
   * @param printed printed bytes; if negative, the result was not serialized
   * @param hits number of returned hits
   * @param locks read and write locks (can be {@code null})
   * @return sections
   */
  public Sections toSections(final QueryProcessor qp, final long printed, final long hits,
      final Locks locks) {

    final Map<String, Section> map = new LinkedHashMap<>();
    final List<Entry> timing = new ArrayList<>(6);
    final LongList times = new LongList(6);
    time("parsing", parsing.get(), timing, times);
    time("compiling", compiling.get(), timing, times);
    time("optimizing", optimizing.get(), timing, times);
    time("evaluating", evaluating.get(), timing, times);
    // the result of a job is serialized by its caller: there is nothing to report
    if(printed >= 0) time("serializing", serializing.get(), timing, times);
    time(TOTAL, total(), timing, times);
    map.put(TIMING, new Section(false, timing));

    if(!evaluate.isEmpty()) map.put(EVALUATION, new Section(false, evaluate.entries()));

    final List<Entry> result = new ArrayList<>(5);
    result.add(new Entry("items", String.valueOf(hits)));
    result.add(new Entry("updates", String.valueOf(qp.updates())));
    if(printed >= 0) result.add(new Entry("bytes", Performance.formatHuman(printed)));
    if(locks != null) {
      result.add(new Entry("reads", locks.reads.toString()));
      result.add(new Entry("writes", locks.writes.toString()));
    }
    map.put(RESULT, new Section(false, result));

    map.put(OPTIMIZED_QUERY, text(optimized(qp)));
    if(!optimize.isEmpty()) map.put(OPTIMIZATION, new Section(false, optimize.entries()));
    if(!compile.isEmpty()) map.put(COMPILATION, new Section(false, compile.entries()));
    if(query != null) {
      map.put(QUERY, text(QueryParser.removeComments(query, Integer.MAX_VALUE)));
    }
    // the plan is included if it was serialized while the query was compiled
    if(queryPlan != null) map.put(PLAN, text(queryPlan));

    return new Sections(map, times);
  }

  /**
   * Returns the lines with which a section is displayed.
   * @param section section
   * @return lines
   */
  public static StringList lines(final Section section) {
    final StringList sl = new StringList();
    final List<Entry> entries = section.entries();
    if(section.text()) {
      for(final String line : entries.get(0).value().split("\r?\n", -1)) sl.add(line);
    } else {
      for(final Entry entry : entries) {
        sl.add(LI + (entry.key() != null ? Strings.titleCase(entry.key()) + COLS : "") +
            entry.value());
      }
    }
    return sl;
  }

  /**
   * Adds the time of a query phase.
   * @param key key
   * @param nano time (nanoseconds)
   * @param timing timing entries
   * @param times times (nanoseconds)
   */
  private void time(final String key, final long nano, final List<Entry> timing,
      final LongList times) {
    timing.add(new Entry(key, Performance.formatNano(nano, runs)));
    times.add(nano);
  }

  /**
   * Returns a section with a single block of text.
   * @param text text
   * @return section
   */
  private static Section text(final String text) {
    return new Section(true, List.of(new Entry(null, text)));
  }

  /**
   * Returns the optimized query.
   * @param qp query processor
   * @return optimized query
   */
  private static String optimized(final QueryProcessor qp) {
    return (qp.qc.main == null ? qp.qc.functions : qp.qc.main).toString();
  }

  /**
   * Returns the query plan, preceded by its heading.
   * @param qp query processor
   * @return heading and plan; empty if the plan cannot be serialized
   */
  public String planInfo(final QueryProcessor qp) {
    final String xml = plan(qp);
    return xml == null ? "" : NL + Strings.titleCase(PLAN) + COL + NL + xml;
  }

  /**
   * Returns the query plan, and serializes it if this has not happened yet.
   * @param qp query processor
   * @return query plan, or {@code null} if it cannot be serialized
   */
  public String plan(final QueryProcessor qp) {
    if(queryPlan == null) {
      try {
        // the serializer breaks lines as the platform does; a string value uses linefeeds
        queryPlan = qp.toXml().serialize(SerializerMode.INDENT.get()).toString().
            replace("\r\n", "\n");
      } catch(final QueryIOException ex) {
        Util.debug(ex);
      }
    }
    return queryPlan;
  }

  /**
   * Returns the total evaluation time.
   * @return time (nanoseconds)
   */
  private long total() {
    return parsing.get() + compiling.get() + optimizing.get() + evaluating.get() +
        serializing.get();
  }

  /**
   * Entry of a section.
   * @param key key ({@code null} if the entry has no label)
   * @param value value
   */
  public record Entry(String key, String value) { }

  /**
   * Section of the query information.
   * @param text the entries form a block of text, not a list
   * @param entries entries
   */
  public record Section(boolean text, List<Entry> entries) { }

  /**
   * Structured query information.
   * @param sections sections, keyed by their name
   * @param times times of the query phases (nanoseconds)
   */
  public record Sections(Map<String, Section> sections, LongList times) { }

  /**
   * Info strings of a single query phase, bounded by a maximum size.
   */
  private static final class Infos {
    /** Info strings. */
    private final TokenList list = new TokenList();
    /** Size of the collected strings. */
    private int size;

    /**
     * Adds an info string.
     * @param info info string
     */
    private synchronized void add(final byte[] info) {
      if(size >= MAX) return;
      list.add(info);
      size += info.length;
      if(size >= MAX) list.add(token(DOTS));
    }

    /**
     * Returns the info strings as the entries of a section.
     * @return entries
     */
    private synchronized List<Entry> entries() {
      final List<Entry> entries = new ArrayList<>(list.size());
      for(final byte[] info : list) entries.add(new Entry(null, string(info)));
      return entries;
    }

    /**
     * Indicates if no info has been collected.
     * @return result of check
     */
    private synchronized boolean isEmpty() {
      return list.isEmpty();
    }

    /**
     * Indicates if the maximum size has been reached.
     * @return result of check
     */
    private synchronized boolean full() {
      return size >= MAX;
    }

    /**
     * Discards all info strings.
     */
    private synchronized void reset() {
      list.reset();
      size = 0;
    }
  }
}
