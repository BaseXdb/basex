package org.basex.query.func.repo;

import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RepoList extends RepoFn {
  @Override
  public Value value(final QueryContext qc) {
    final ValueBuilder vb  = new ValueBuilder(qc);
    final RepoManager repo = new RepoManager(qc.context);
    for(final Pkg pkg : repo.packages()) {
      vb.add(FElem.build(Q_PACKAGE).attr(Q_NAME, pkg.name()).
          attr(Q_VERSION, pkg.version()).attr(Q_TYPE, pkg.type()).
          attr(Q_PATH, repo.path(pkg)).finish());
    }
    return vb.value(this);
  }
}
