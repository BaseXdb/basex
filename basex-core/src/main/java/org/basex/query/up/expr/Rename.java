package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.users.*;
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
 * @author BaseX Team, BSD License
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
    final IdentityHashMap<Type, QNm> names = new IdentityHashMap<>();

    for(Item item; (item = iter.next()) != null;) {
      final Type type = item.type;
      final boolean element = type == NodeType.ELEMENT, attribute = type == NodeType.ATTRIBUTE;
      final boolean pi = type == NodeType.PROCESSING_INSTRUCTION;
      if(!(element || attribute || pi)) throw UPWRTRGTYP_X.get(info, item);

      QNm newName = names.get(type);
      if(newName == null) {
        final CNode cname;
        if(element) {
          cname = new CElem(info, false, name, new Atts());
        } else if(attribute) {
          cname = new CAttr(info, false, name, Empty.VALUE);
        } else {
          cname = new CPI(info, false, name, Empty.VALUE);
        }
        newName = ((ANode) cname.item(qc, info)).qname();
        names.put(type, newName);
      }

      // check for namespace conflicts
      final ANode target = (ANode) item;
      if(element || attribute) {
        final byte[] newPrefix = newName.prefix(), newUri = newName.uri();
        if(element || newPrefix.length > 0) {
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
      final DBNode dbnode = updates.determineDataRef(target, qc);
      checkPerm(qc, Perm.WRITE, dbnode.data().meta.name);
      updates.add(new RenameNode(dbnode.pre(), dbnode.data(), info, newName), qc);
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
