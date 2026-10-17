package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract update expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class Update extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expressions
   */
  Update(final InputInfo info, final Expr... expr) {
    super(info, Types.EMPTY_SEQUENCE_Z, expr);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.UPD.oneOf(flags) || Flag.NDT.oneOf(flags) || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    // target database is supplied by a node and cannot be resolved statically
    visitor.unresolvedTarget();
    return super.accept(visitor);
  }

  /**
   * Checks for namespace conflicts in attributes.
   * @param list node list
   * @param target target node
   * @param qc query context
   * @return specified node list, or a copy with renamed attributes
   * @throws QueryException query exception
   */
  final GNodeList checkNS(final GNodeList list, final XNode target, final QueryContext qc)
      throws QueryException {
    // the list is shared by all targets: copy it before renaming attributes
    GNodeList checked = list;
    Atts scope = null;
    final int ls = list.size();
    for(int l = 0; l < ls; l++) {
      final GNode attr = list.get(l);
      final QNm name = attr.qname();
      // attributes without prefix have no namespace
      if(!name.hasPrefix()) continue;
      if(scope == null) scope = target.nsScope(null);
      final QNm nm = checkNS(name, scope, qc);
      if(nm != name) {
        if(checked == list) checked = new GNodeList(ls).add(list);
        checked.set(l, new FAttr(nm, attr.string()));
      }
    }
    return checked;
  }

  /**
   * Checks if the name of a prefixed attribute conflicts with the namespaces of an element.
   * @param name attribute name
   * @param scope in-scope namespaces of the element
   * @param qc query context
   * @return name, or name with another prefix if the generated prefix was already bound
   * @throws QueryException query exception
   */
  final QNm checkNS(final QNm name, final Atts scope, final QueryContext qc) throws QueryException {
    final byte[] prefix = name.prefix(), uri = name.uri(), bound = scope.value(prefix);
    if(bound == null || eq(bound, uri)) return name;
    if(!qc.shared.generated(prefix, uri)) throw UPNSCONFL_X_X.get(info, uri, bound);
    // generated prefix: choose another one
    final byte[] npref = Constr.addNS(scope, prefix, uri, qc.shared);
    return qc.shared.qName(concat(npref, cpToken(':'), name.local()), uri);
  }

  /**
   * Creates a node builder.
   * @param expr node expression
   * @param qc query context
   * @return builder
   * @throws QueryException query exception
   */
  final FBuilder builder(final Expr expr, final QueryContext qc) throws QueryException {
    final FBuilder builder = new FBuilder();
    final Constr constr = new Constr(builder, false, info, qc).add(expr);
    if(constr.errAtt != null) throw UPNOATTRPER_X.get(info, constr.errAtt);
    if(constr.duplAtt != null) throw UPATTDUPL_X.get(info, constr.duplAtt);
    constr.checkNS();
    return builder;
  }

  /**
   * Checks update permissions.
   * @param dbnode database node
   * @param qc query context
   * @throws QueryException query exception
   */
  final void checkWrite(final DBNode dbnode, final QueryContext qc) throws QueryException {
    final String name = dbnode.data().meta.name;
    if(!name.isEmpty()) checkPerm(qc, Perm.WRITE, name);
  }
}
