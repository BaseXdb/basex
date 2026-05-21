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
    final Str pat = Str.get(pattern);
    final Str flg = Str.get(flags);
    final XQMap map = new MapBuilder().
        put("pattern", pat).
        put("flags", flg).
        put("matches", unaryStringFunc(MATCHES, qc, pat, flg)).
        put("tokenize", unaryStringFunc(TOKENIZE, qc, pat, flg)).
        put("replace", replaceFunc(qc, pat, flg)).
        put("analyze-string", unaryStringFunc(ANALYZE_STRING, qc, pat, flg)).
        put("matching-segments", unaryStringFunc(MATCHING_SEGMENTS, qc, pat, flg)).
        map();
    map.type = Records.COMPILED_REGEX.get();
    return map;
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
  private FuncItem unaryStringFunc(final Function fn, final QueryContext qc,
      final Str pat, final Str flg) {
    final FuncDefinition def = fn.definition();
    final Var s = new Var(new QNm("s"), null, qc, info, 0, null);
    final Var[] params = { s };
    return new FuncItem(info,
        fn.get(info, new VarRef(info, s), pat, flg),
        params, AnnList.EMPTY,
        FuncType.get(def.seqType, Types.STRING_O),
        params.length, def.name);
  }

  /**
   * Creates a function item for {@code fn:replace}, taking string and replacement inputs, closing
   * over the compiled pattern and flags.
   * @param qc query context
   * @param pat pattern
   * @param flg flags
   * @return function item
   */
  private FuncItem replaceFunc(final QueryContext qc, final Str pat, final Str flg) {
    final FuncDefinition def = REPLACE.definition();
    final Var s = new Var(new QNm("s"), null, qc, info, 0, null);
    final Var rep = new Var(new QNm("replacement"), null, qc, info, 1, null);
    final Var[] params = { s, rep };
    return new FuncItem(info,
        REPLACE.get(info, new VarRef(info, s), pat, new VarRef(info, rep), flg),
        params, AnnList.EMPTY,
        FuncType.get(def.seqType, Types.STRING_O, FnReplace.REPLACEMENT_TYPE),
        params.length, def.name);
  }
}
