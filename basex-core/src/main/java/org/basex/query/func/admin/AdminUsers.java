package org.basex.query.func.admin;

import java.util.*;

import org.basex.core.*;
import org.basex.core.User.*;
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
      final String perm = u.perm().toString().toLowerCase(Locale.ENGLISH);
      final FElem elem = new FElem(USER).add(u.name()).add(PERMISSION, perm);
      final String salt = u.code(Code.SALT);
      final String salt256 = u.code(Code.SALT256);
      if(salt != null) elem.add(SALT, u.code(Code.SALT));
      if(salt256 != null) elem.add(PASSWORD, u.code(Code.SALT256));
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
