package org.basex.query.func.admin;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.server.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class AdminSessions extends AdminFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final ValueBuilder vb = new ValueBuilder();
    synchronized(qc.context.sessions) {
      for(final ClientListener sp : qc.context.sessions) {
        final String user = sp.context().user.name;
        final String addr = sp.address();
        final Data data = sp.context().data();
        final FElem elem = new FElem(SESSION).add(USER, user).add(ADDRESS, addr);
        if(data != null) elem.add(DATABASE, data.meta.name);
        vb.add(elem);
      }
    }
    return vb;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) && super.accept(visitor);
  }
}
