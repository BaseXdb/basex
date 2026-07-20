package org.basex.query.func.string;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.similarity.Levenshtein.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;
import org.basex.util.options.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringClosest extends StringFn {
  /** Options. */
  public static final class ClosestOptions extends NgramOptions {
    /** Similarity measure. */
    public static final ValueOption MEASURE = new ValueOption("measure", Types.FUNCTION_ZO);
    /** Minimum similarity. */
    public static final ValueOption THRESHOLD =
        new ValueOption("threshold", Types.NUMERIC_O, Dbl.ZERO);
    /** Maximum number of results (0 or less: all results). */
    public static final NumberOption LIMIT = new NumberOption("limit", 1);
  }

  /** Result key: value. */
  private static final String VALUE = "value";
  /** Result key: similarity. */
  private static final String SIMILARITY = "similarity";

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toToken(arg(0), qc);
    final Iter candidates = arg(1).atomIter(qc, info);
    final ClosestOptions options = toOptions(arg(2), new ClosestOptions(), qc);

    final double threshold = ((ANum) options.get(ClosestOptions.THRESHOLD)).dbl();
    final int limit = options.get(ClosestOptions.LIMIT);
    final int n = n(options);
    final boolean padding = options.get(NgramOptions.PADDING);
    final FTOpt opt = ftOpt(options);

    final Value func = options.get(ClosestOptions.MEASURE);
    final FItem function = func.isEmpty() ? null : (FItem) func.itemAt(0);
    if(function != null && function.arity() != 2) throw typeError(function,
        FuncType.get(Types.DOUBLE_O, Types.STRING_O, Types.STRING_O), info);
    final Measure measure = measure(function);

    final boolean tokenize = Enums.oneOf(measure, Measure.TOKEN_SORT_RATIO,
        Measure.TOKEN_SET_RATIO);
    // measures with a quadratic runtime are limited in length
    final boolean bounded = Enums.oneOf(measure, Measure.LEVENSHTEIN, Measure.JARO_WINKLER,
        Measure.TOKEN_SORT_RATIO, Measure.TOKEN_SET_RATIO, Measure.PARTIAL_RATIO);
    final int[] cps = cps(value, opt);
    final String[] tokens = tokenize ? tokens(value, opt) : null;
    if(bounded) checkLength(cps.length);

    // a single result is requested: only the best candidate is cached
    final boolean single = limit == 1;
    final TokenList values = new TokenList();
    final DoubleList similarities = new DoubleList();
    for(Item item; (item = qc.next(candidates)) != null;) {
      // candidates can be untyped (e.g. index entries returned by ft:tokens)
      final byte[] cand = toToken(item);
      final int[] cps2 = cps(cand, opt);
      if(bounded) checkLength(cps2.length);

      final double similarity = measure == null ? invoke(function, cps, cps2, qc) :
        tokenize ? tokenRatio(tokens, tokens(cand, opt), measure) :
        similarity(cps, cps2, measure, threshold, n, padding);
      // NaN thresholds discard all candidates: the check must not be inverted
      if(similarity >= threshold) {
        if(single && !values.isEmpty()) {
          // equal similarities are ignored: the first candidate wins
          if(similarity <= similarities.get(0)) continue;
          values.reset();
          similarities.reset();
        }
        values.add(cand);
        similarities.add(similarity);
      }
    }

    // sort by descending similarity; equal values are returned in input order
    final int size = values.size();
    final Integer[] order = new Integer[size];
    for(int o = 0; o < size; o++) order[o] = o;
    Arrays.sort(order, (o1, o2) -> Double.compare(similarities.get(o2), similarities.get(o1)));

    final int max = limit > 0 ? Math.min(limit, size) : size;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(int o = 0; o < max; o++) {
      final int p = order[o];
      vb.add(new MapBuilder().put(VALUE, Str.get(values.get(p))).
          put(SIMILARITY, Dbl.get(similarities.get(p))).map());
    }
    return vb.value(this);
  }

  /**
   * Returns the measure that is computed internally for the specified function.
   * @param function function ({@code null}: default measure)
   * @return measure, or {@code null} if the function must be invoked
   */
  private static Measure measure(final FItem function) {
    if(function == null) return Measure.LEVENSHTEIN;
    final QNm name = function.funcName();
    return name != null && eq(name.uri(), STRING_URI) ?
      Enums.get(Measure.class, string(name.local())) : null;
  }

  /**
   * Invokes a user-defined similarity measure.
   * @param function function
   * @param cps1 first codepoints array
   * @param cps2 second codepoints array
   * @param qc query context
   * @return similarity
   * @throws QueryException query exception
   */
  private double invoke(final FItem function, final int[] cps1, final int[] cps2,
      final QueryContext qc) throws QueryException {
    final Item item = function.invoke(qc, info, str(cps1), str(cps2)).item(qc, info);
    if(item.isEmpty()) throw typeError(item, BasicType.DOUBLE, info);
    return toDouble(item);
  }

  /**
   * Computes a token-based similarity.
   * @param tokens1 first tokens
   * @param tokens2 second tokens
   * @param measure similarity measure
   * @return similarity (0.0 - 1.0)
   */
  private static double tokenRatio(final String[] tokens1, final String[] tokens2,
      final Measure measure) {
    return measure == Measure.TOKEN_SORT_RATIO ? TokenRatio.sort(tokens1, tokens2) :
      TokenRatio.set(tokens1, tokens2);
  }

  /**
   * Computes a character-based similarity.
   * @param cps1 first codepoints array
   * @param cps2 second codepoints array
   * @param measure similarity measure
   * @param threshold minimum similarity
   * @param n n-gram length
   * @param padding pad the input
   * @return similarity (0.0 - 1.0), or a negative value if the threshold cannot be reached
   */
  private static double similarity(final int[] cps1, final int[] cps2, final Measure measure,
      final double threshold, final int n, final boolean padding) {

    return switch(measure) {
      case JARO_WINKLER     -> JaroWinkler.distance(cps1, cps2);
      case NGRAM_SIMILARITY -> NGram.similarity(cps1, cps2, n, padding);
      case PARTIAL_RATIO    -> partial(cps1, cps2);
      default               -> {
        // a similarity of t allows at most (1 - t) * length errors
        final int mx = Math.max(cps1.length, cps2.length);
        if(mx == 0) yield 1;
        final int errors = threshold > 0 ? (int) Math.ceil((1 - threshold) * mx) : -1;
        final int dist = distance(cps1, cps2, errors);
        yield dist == -1 ? -1 : (double) (mx - dist) / mx;
      }
    };
  }

  /** Similarity measures that are computed internally. */
  public enum Measure {
    /** Levenshtein.       */ LEVENSHTEIN,
    /** Jaro-Winkler.      */ JARO_WINKLER,
    /** Token sort ratio.  */ TOKEN_SORT_RATIO,
    /** Token set ratio.   */ TOKEN_SET_RATIO,
    /** N-gram similarity. */ NGRAM_SIMILARITY,
    /** Partial ratio.     */ PARTIAL_RATIO;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }
}
