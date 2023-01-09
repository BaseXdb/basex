package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnEmpty extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(empty(qc));
  }

  @Override
  protected final void simplifyArgs(final CompileContext cc) throws QueryException {
    exprs[0] = exprs[0].simplifyFor(Simplify.COUNT, cc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final boolean exists = this instanceof FnExists;
    Expr input = exprs[0];
    final SeqType st = input.seqType();

    // ignore non-deterministic expressions (e.g.: empty(error()))
    if(!input.has(Flag.NDT)) {
      if(st.zero()) return Bln.get(!exists);
      if(st.oneOrMore()) return Bln.get(exists);
    }

    // static integer will always be greater than 1
    if(REPLICATE.is(input) && input.arg(1) instanceof Int) {
      input = input.arg(0);
    }
    // rewrite list to union expression:  exists((nodes1, nodes2))  ->  exists(nodes1 | nodes2)
    if(input instanceof List && input.seqType().type instanceof NodeType) {
      input = new Union(info, input.args()).optimize(cc);
    }
    if(input != exprs[0]) return cc.function(exists ? EXISTS : EMPTY, info, input);

    // replace optimized expression by boolean function
    if(input instanceof Filter) {
      // rewrite filter:  exists($a[text() = string])  ->  $a/text() = string
      final Filter filter = (Filter) input;
      input = filter.flattenEbv(filter.root, false, cc);
    } else if(INDEX_OF.is(input)) {
      // rewrite index-of:  exists(index-of($texts, string))  ->  $texts = string
      final Expr[] args = input.args();
      if(args.length == 2 && args[1].seqType().one() &&
          CmpG.compatible(args[0].seqType(), args[1].seqType(), true)) {
        input = new CmpG(info, args[0], args[1], OpG.EQ, null, sc).optimize(cc);
      }
    } else if(STRING_TO_CODEPOINTS.is(input) || CHARACTERS.is(input)) {
      // exists(string-to-codepoints(E))  ->  boolean(string(E))
      input = cc.function(STRING, info, input.args());
    }
    if(input != exprs[0]) return cc.function(exists ? BOOLEAN : NOT, info, input);

    // exists(map:keys(E))  ->  map:size(E) > 0
    // empty(util:array-members(E))  ->  array:size(E) = 0
    final boolean map = _MAP_KEYS.is(input), array = _UTIL_ARRAY_MEMBERS.is(input);
    if(map || array) {
      input = cc.function(map ? _MAP_SIZE : _ARRAY_SIZE, info, input.args());
      return new CmpG(info, input, Int.ZERO, exists ? OpG.NE : OpG.EQ, null, sc).optimize(cc);
    }

    return embed(cc, true);
  }

  @Override
  public final Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {

    final Function func = this instanceof FnExists ? EXISTS : EMPTY;
    if(!or && func.is(expr)) {
      return cc.function(func, info, List.get(cc, info, exprs[0], expr.arg(0)));
    }
    if(_UTIL_COUNT_WITHIN.is(expr)) {
      return expr.mergeEbv(this, or, cc);
    }
    return null;
  }

  /**
   * Evaluates the function.
   * @param qc query context
   * @return boolean result
   * @throws QueryException query exception
   */
  final boolean empty(final QueryContext qc) throws QueryException {
    final Expr input = exprs[0];
    return input.seqType().zeroOrOne() ?
      input.item(qc, info) == Empty.VALUE :
      input.iter(qc).next() == null;
  }
}
