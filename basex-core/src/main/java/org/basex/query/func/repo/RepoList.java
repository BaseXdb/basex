package org.basex.query.func.repo;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
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
  public BasicNodeIter iter(final QueryContext qc) {
    final ANodeList list = new ANodeList();
    for(final Pkg pkg : new RepoManager(qc.context).all()) {
      final FElem elem = new FElem(PACKAGE);
      elem.add(NAME, pkg.name());
      final String version = pkg.version();
      if(version == null) {
        elem.add(TYPE, PkgText.INTERNAL);
      } else {
        elem.add(VERSION, version);
        elem.add(TYPE, PkgText.EXPATH);
      }
      list.add(elem);
    }
    return list.iter();
  }
}
