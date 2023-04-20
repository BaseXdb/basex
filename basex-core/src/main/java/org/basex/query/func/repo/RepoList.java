package org.basex.query.func.repo;

import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class RepoList extends RepoFn {
  /** Element name. */
  private static final String PACKAGE = "package";
  /** Header attribute: name. */
  private static final String NAME = "name";
  /** Header attribute: type. */
  private static final String TYPE = "type";
  /** Header attribute: version. */
  private static final String VERSION = "version";

  @Override
  public Value value(final QueryContext qc) {
    final ValueBuilder vb  = new ValueBuilder(qc);
    for(final Pkg pkg : new RepoManager(qc.context).packages()) {
      vb.add(FElem.build(PACKAGE).add(NAME, pkg.name()).add(VERSION, pkg.version()).
          add(TYPE, pkg.type()).finish());
    }
    return vb.value(this);
  }
}
