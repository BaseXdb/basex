package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnRegex extends RegExFn {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] pattern = toToken(arg(0), qc);
    final byte[] flags = toZeroToken(arg(1), qc);

    // validate pattern and pre-warm cache
    regExpr(pattern, flags, qc);
    final Str pttrn = Str.get(pattern);
    final Str flgs = Str.get(flags);
    return new XQRecordMap(Records.COMPILED_REGEX.get(), pttrn, flgs,
        valueFunc(MATCHES, qc, pttrn, flgs),
        valueFunc(TOKENIZE, qc, pttrn, flgs),
        valueReplacementFunc(qc, pttrn, flgs),
        valueFunc(ANALYZE_STRING, qc, pttrn, flgs),
        valueFunc(MATCHING_SEGMENTS, qc, pttrn, flgs));
  }

  /**
   * Creates a function item for a built-in, taking a single string input, closing over the
   * compiled pattern and flags.
   * @param fn built-in function
   * @param qc query context
   * @param pat pattern
   * @param flg flags
   * @return function item
   */
  private FuncItem valueFunc(final Function fn, final QueryContext qc,
      final Str pat, final Str flg) {
    final FuncDefinition def = fn.definition();
    final Var v = new Var(new QNm("value"), null, qc, info, 0, null);
    final Var[] params = { v };
    return new FuncItem(info,
        fn.get(info, new VarRef(info, v), pat, flg), params,
        AnnList.EMPTY, FuncType.get(def.seqType, Types.STRING_O),
        params.length, def.name);
  }

  /**
   * Creates a function item for {@code fn:replace}, taking string and replacement inputs, closing
   * over the compiled pattern and flags.
   * @param qc query context
   * @param pattern pattern
   * @param flg flags
   * @return function item
   */
  private FuncItem valueReplacementFunc(final QueryContext qc, final Str pattern, final Str flg) {
    final FuncDefinition def = REPLACE.definition();
    final Var v = new Var(new QNm("value"), null, qc, info, 0, null);
    final Var r = new Var(new QNm("replacement"), null, qc, info, 1, null);
    final Var[] params = { v, r };
    return new FuncItem(info,
        REPLACE.get(info, new VarRef(info, v), pattern, new VarRef(info, r), flg), params,
        AnnList.EMPTY, FuncType.get(def.seqType, Types.STRING_O, FnReplace.REPLACEMENT_TYPE),
        params.length, def.name);
  }
}
