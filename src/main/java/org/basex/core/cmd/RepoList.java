package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdRepo;
import org.basex.data.DataText;
import org.basex.query.util.pkg.Package;
import org.basex.util.Table;
import org.basex.util.list.TokenList;

/**
 * Evaluates the 'repo list' command.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class RepoList extends Command {
  /**
   * Constructor.
   */
  public RepoList() {
    super(User.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    final Table t = new Table();
    t.description = PACKAGES;
    t.header.add(DataText.TABLEURI);
    t.header.add(VERSINFO);
    t.header.add(INFODIRECTORY);

    for(final byte[] p : context.repo.pkgDict()) {
      if(p != null) {
        final TokenList tl = new TokenList();
        tl.add(Package.name(p));
        tl.add(Package.version(p));
        tl.add(context.repo.pkgDict().get(p));
        t.contents.add(tl);
      }
    }
    t.sort();
    out.println(t.finish());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.LIST).args();
  }
}
