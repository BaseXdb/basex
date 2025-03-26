package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Copy expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class Copy extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param exprs expressions
   */
  Copy(final InputInfo info, final SeqType seqType, final Expr... exprs) {
    super(info, seqType, exprs);
  }

  @Override
  public final Expr optimize(final CompileContext cc) {
    // do not assign original sequence type (name of node may change):
    // <a/> update { rename node . as 'x' }  ->  <x/>
    final SeqType st = arg(target()).seqType();
    exprType.assign(st.type, st.occ);
    return this;
  }

  @Override
  public void checkUp() throws QueryException {
    final Expr modify = arg(update());
    modify.checkUp();
    if(!modify.has(Flag.UPD) && !modify.vacuous()) throw UPMODIFY.get(info);
  }

  /**
   * Returns the position of the updating expression.
   * @return result expression
   */
  final int update() {
    return 0;
  }

  /**
   * Returns the position of the target expression.
   * @return target expression
   */
  final int target() {
    return 1;
  }
}
