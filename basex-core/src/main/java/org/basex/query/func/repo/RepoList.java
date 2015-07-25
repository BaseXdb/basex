package org.basex.query.func.repo;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.util.pkg.*;
import org.basex.query.util.pkg.Package;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
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
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final ANodeList cache = new ANodeList();
    final Repo repo = qc.context.repo.reset();
    for(final byte[] p : repo.pkgDict()) {
      if(p == null) continue;
      final FElem elem = new FElem(PACKAGE);
      elem.add(NAME, Package.name(p));
      elem.add(VERSION, Package.version(p));
      elem.add(TYPE, PkgText.EXPATH);
      cache.add(elem);
    }
    // traverse all directories, ignore root entries with dashes
    for(final IOFile dir : repo.path().children()) {
      if(dir.name().indexOf('-') != -1) continue;
      for(final String s : dir.descendants()) {
        final FElem elem = new FElem(PACKAGE);
        elem.add(NAME, dir.name() + '.' + s.replaceAll("\\..*", "").replace('/', '.'));
        elem.add(TYPE, PkgText.INTERNAL);
        cache.add(elem);
      }
    }
    return cache.iter();
  }
}
