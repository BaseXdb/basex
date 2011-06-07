package org.basex.core.cmd;

import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.User;
import org.basex.query.util.repo.Package;
import org.basex.util.Table;
import org.basex.util.TokenList;

/**
 * Evaluates the 'repo list' command.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public class RepoList extends Command {

  /**
   * Constructor.
   */
  public RepoList() {
    super(User.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    final Table t = new Table();
    t.header.add("Package");
    t.header.add("Version");

    for(final byte[] p : context.repo.pkgDict()) {
      if(p != null) {
        final TokenList tl = new TokenList();
        tl.add(string(Package.getName(p)));
        tl.add(string(Package.getVersion(p)));
        t.contents.add(tl);
      }
    }
    t.sort();
    out.println(t.finish());
    return true;
  }

}
