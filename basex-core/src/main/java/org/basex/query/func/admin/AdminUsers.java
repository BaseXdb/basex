package org.basex.query.func.admin;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class AdminUsers extends AdminFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final ValueBuilder vb = new ValueBuilder();
    for(final User u : exprs.length == 0 ? qc.context.users.users(null) :
      checkData(qc).meta.users.users(qc.context.users)) {
      vb.add(new FElem(USER).add(u.name).add(PERMISSION,
          u.perm.toString().toLowerCase(Locale.ENGLISH)).add(PASSWORD, u.password));
    }
    return vb;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) &&
        (exprs.length == 0 || dataLock(visitor, 0)) && super.accept(visitor);
  }
}
