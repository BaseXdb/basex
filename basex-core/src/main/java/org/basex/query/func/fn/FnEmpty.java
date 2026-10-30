package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.util.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnEmpty extends StandardFunc {
  @Override
  public final Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    final Expr input = arg(0);
    return input.seqType().zeroOrOne() ?
      input.item(qc, info) == Empty.VALUE :
      input.iter(qc).next() == null;
  }

  @Override
  protected final void simplifyArgs(final CompileContext cc) throws QueryException {
    // exists(distinct-values($seq)) → exists($seq)
    arg(0, arg -> arg.has(Flag.NDT) ? arg : arg.simplifyFor(Simplify.EXISTENCE, cc));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final boolean exists = this instanceof FnExists;
    Expr input = arg(0);
    final SeqType st = input.seqType();

    // ignore nondeterministic expressions (e.g.: empty(error()))
    if(!input.has(Flag.NDT)) {
      // empty(()) → true(), exists(1 to 10) → true()
      if(st.zero()) return Bln.get(!exists);
      if(st.oneOrMore()) return Bln.get(exists);
    }

    // rewrite to boolean function
    if(input instanceof final Filter filter) {
      // rewrite filter:  exists($a[text() = string]) → $a/text() = string
      input = filter.flattenEbv(filter.root, false, cc);
    } else if(input instanceof final Path path) {
      // rewrite path:  empty($a/b[@id = string]) → not($a/b/@id = string)
      input = path.flattenEbv(false, cc);
    } else if(INDEX_OF.is(input)) {
      // rewrite index-of:  exists(index-of($texts, string)) → $texts = string
      final Expr[] args = input.args();
      if(args.length == 2 && args[1].seqType().one() && args[0].seqType().type.isStringOrUntyped()
          && args[1].seqType().type.isStringOrUntyped()) {
        input = new CmpG(info, args[0], args[1], CmpOp.EQ).optimize(cc);
      }
    } else if(STRING_TO_CODEPOINTS.is(input) || CHARACTERS.is(input)) {
      // exists(string-to-codepoints(E)) → boolean(string(E))
      input = cc.function(STRING, info, input.args());
    }
    if(input != arg(0)) return cc.function(exists ? BOOLEAN : NOT, info, input);

    // exists(map:keys(E)) → map:size(E) > 0
    // empty(array:members(E)) → array:size(E) = 0
    final boolean map = _MAP_KEYS.is(input), array = _ARRAY_MEMBERS.is(input);
    if(map || array) {
      input = cc.function(map ? _MAP_SIZE : _ARRAY_SIZE, info, input.args());
      return new CmpG(info, input, Itr.ZERO, exists ? CmpOp.NE : CmpOp.EQ).optimize(cc);
    }
    // exists($a to $b) → $a <= $b, empty($a to $b) → $a > $b
    if(input instanceof final Range range && range.integers()) {
      return new CmpG(info, range.arg(0), range.arg(1), exists ? CmpOp.LE : CmpOp.GT).optimize(cc);
    }

    // exists(tail(E)) → util:count-within(E, 2), empty(tail(E)) → util:count-within(E, 0, 1)
    // exists(subsequence(E, pos)) → util:count-within(E, pos)
    // empty(items-at(E, pos)) → util:count-within(E, 0, pos - 1)
    final SeqRange range = SeqRange.get(input, cc);
    if(range != null && range.length > 0 && !input.has(Flag.NDT)) {
      final long[] mm = range.inputSize(exists ? 1 : 0, exists ? Long.MAX_VALUE : 0);
      return UtilCountWithin.function(input.arg(0), mm[0], mm[1], info, cc);
    }
    return embed(cc, true);
  }

  @Override
  public final Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {

    // exists(A) or exists(B) → exists((A, B))
    // empty(A) and empty(B) → empty((A, B))
    final Function func = this instanceof FnExists ? EXISTS : EMPTY;
    if(func == (or ? EXISTS : EMPTY) && func.is(expr)) {
      return cc.function(func, info, List.get(cc, info, arg(0), expr.arg(0)));
    }
    if(_UTIL_COUNT_WITHIN.is(expr)) {
      return expr.mergeEbv(this, or, cc);
    }
    return null;
  }
}
