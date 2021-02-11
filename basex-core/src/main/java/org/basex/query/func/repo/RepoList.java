package org.basex.query.func.repo;

import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
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
      final FElem elem = new FElem(PACKAGE);
      elem.add(NAME, pkg.name());
      elem.add(VERSION, pkg.version());
      elem.add(TYPE, pkg.type().toString());
      vb.add(elem);
    }
    return vb.value(this);
  }
}
