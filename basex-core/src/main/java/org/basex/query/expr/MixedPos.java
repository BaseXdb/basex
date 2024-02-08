package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Mixed position check.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class MixedPos extends Simple implements CmpPos {
  /** Positions. */
  final Value positions;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param positions positions
   */
  private MixedPos(final InputInfo info, final Value positions) {
    super(info, SeqType.BOOLEAN_O);
    this.positions = positions;
  }

  /**
   * Returns a position expression for the specified range, or an optimized boolean item.
   * @param value positions
   * @param info input info (can be {@code null})
   * @return expression
   */
  public static Expr get(final Value value, final InputInfo info) {
    return new MixedPos(info, value);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    return Bln.get(test(qc.focus.pos, qc) != 0);
  }

  @Override
  public int test(final long pos, final QueryContext qc) throws QueryException {
    final long qf = qc.focus.pos;
    qc.focus.pos = pos;
    try {
      return positions.test(qc, info, true) ? 1 : 0;
    } finally {
      qc.focus.pos = qf;
    }
  }

  @Override
  public boolean exact() {
    return false;
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    return null;
  }

  @Override
  public boolean has(final Flag... flags) {
    throw Util.notExpected();
  }

  @Override
  public IntPos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    throw Util.notExpected();
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj;
  }

  @Override
  public String description() {
    return "mixed positional access";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function.POSITION).token("=").paren(positions);
  }
}
