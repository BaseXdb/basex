package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Rename expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Lukas Kircher
 */
public final class Rename extends Update {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param trg target expression
   * @param name new name expression
   */
  public Rename(final InputInfo info, final Expr trg, final Expr name) {
    super(info, trg, name);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = arg(0).iter(qc);
    final Item name = arg(1).atomItem(qc, info);

    for(Item item; (item = iter.next()) != null;) {
      final Type type = item.type;
      final boolean elemnt = type == NodeType.ELEMENT, attribute = type == NodeType.ATTRIBUTE;
      final CNode cname;
      if(elemnt) {
        cname = new CElem(info, false, name, new Atts());
      } else if(attribute) {
        cname = new CAttr(info, false, name, Empty.VALUE);
      } else if(type == NodeType.PROCESSING_INSTRUCTION) {
        cname = new CPI(info, false, name, Empty.VALUE);
      } else {
        throw UPWRTRGTYP_X.get(info, item);
      }
      final ANode target = (ANode) item;
      final QNm newName = ((ANode) cname.item(qc, info)).qname();

      // check for namespace conflicts
      if(elemnt || attribute) {
        final byte[] newPrefix = newName.prefix(), newUri = newName.uri();
        if(elemnt || newPrefix.length > 0) {
          final Atts nspaces = target.nsScope(sc());
          final int ns = nspaces.size();
          for(int n = 0; n < ns; n++) {
            final byte[] prefix = nspaces.name(n), uri = nspaces.value(n);
            if(eq(prefix, newPrefix) && !eq(uri, newUri))
              throw UPNSCONFL_X_X.get(info, newName, new QNm(prefix, uri));
          }
        }
      }

      final Updates updates = qc.updates();
      final DBNode dbn = updates.determineDataRef(target, qc);
      updates.add(new RenameNode(dbn.pre(), dbn.data(), info, newName), qc);
    }
    return Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Rename(info, arg(0).copy(cc, vm), arg(1).copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Rename && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(RENAME).token(NODE).token(arg(0)).token(AS).token(arg(1));
  }
}
