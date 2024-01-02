package org.basex.query.func.repo;

import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RepoList extends RepoFn {
  @Override
  public Value value(final QueryContext qc) {
    final ValueBuilder vb  = new ValueBuilder(qc);
    for(final Pkg pkg : new RepoManager(qc.context).packages()) {
      vb.add(FElem.build(Q_PACKAGE).add(Q_NAME, pkg.name()).
          add(Q_VERSION, pkg.version()).add(Q_TYPE, pkg.type()).finish());
    }
    return vb.value(this);
  }
}
