package org.basex.query.func.string;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Common functionality of the string similarity functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class StringScore extends StringFn {
  /**
   * Computes a score for the normalized codepoints of the two input strings.
   * @param qc query context
   * @param score scoring function
   * @return score
   * @throws QueryException query exception
   */
  final Dbl cpsScore(final QueryContext qc, final ToDoubleBiFunction<int[], int[]> score)
      throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final FTOpt opt = ftOpt(options(2, StringOptions::new, qc));

    final int[] cps1 = cps(value1, opt), cps2 = cps(value2, opt);
    checkLength(cps1.length);
    checkLength(cps2.length);
    return Dbl.get(score.applyAsDouble(cps1, cps2));
  }

  /**
   * Computes a score for the tokens of the two input strings.
   * @param qc query context
   * @param score scoring function
   * @return score
   * @throws QueryException query exception
   */
  final Dbl tokenScore(final QueryContext qc, final ToDoubleBiFunction<String[], String[]> score)
      throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final FTOpt opt = ftOpt(options(2, StringOptions::new, qc));

    checkLength(Token.length(value1));
    checkLength(Token.length(value2));
    return Dbl.get(score.applyAsDouble(tokens(value1, opt), tokens(value2, opt)));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    optOptions(2, StringOptions::new, cc);
    return this;
  }
}
