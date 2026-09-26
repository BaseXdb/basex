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
  public void checkUp() throws QueryException {
    final Expr modify = arg(update());
    modify.checkUp();
    if(!modify.has(Flag.UPD) && !modify.vacuous()) throw UPMODIFY.get(info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    visitor.enterModify();
    final boolean more = arg(update()).accept(visitor);
    visitor.exitModify();
    return more && arg(target()).accept(visitor);
  }

  /**
   * Returns the type of copied nodes, without names, which may be changed by updates.
   * @param type type of the nodes to be copied
   * @return node type
   */
  static NodeType copyType(final Type type) {
    return type instanceof final NodeType nt && nt.kind().instanceOf(Kind.XNODE) ?
      NodeType.get(nt.kind()) : NodeType.XNODE;
  }

  /**
   * Returns the position of the updating expression.
   * @return result expression
   */
  static int update() {
    return 0;
  }

  /**
   * Returns the position of the target expression.
   * @return target expression
   */
  static int target() {
    return 1;
  }
}
