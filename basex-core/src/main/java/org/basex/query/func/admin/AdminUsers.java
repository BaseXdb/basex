package org.basex.query.func.admin;

import java.util.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
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
    final boolean global = exprs.length == 0;
    for(final User u : global ? qc.context.users.users(null) :
      checkData(qc).meta.users.users(qc.context.users)) {
      final String perm = u.perm().toString().toLowerCase(Locale.ENGLISH);
      final FElem elem = new FElem(USER).add(u.name()).add(PERMISSION, perm);
      if(global) {
        elem.add(SALT, u.code(Algorithm.SALTED_SHA256, Code.SALT));
        elem.add(HASH, u.code(Algorithm.SALTED_SHA256, Code.HASH));
        elem.add(DIGEST, u.code(Algorithm.DIGEST, Code.HASH));
      }
      vb.add(elem);
    }
    return vb;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.ADMIN) &&
        (exprs.length == 0 || dataLock(visitor, 0)) && super.accept(visitor);
  }
}
