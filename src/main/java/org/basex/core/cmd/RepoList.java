package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdRepo;
import org.basex.data.*;
import org.basex.query.util.pkg.Package;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

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
    t.description = PACKAGES_X;
    t.header.add(DataText.TABLEURI);
    t.header.add(VERSINFO);
    t.header.add(DIRECTORY);

    final TokenMap pkg = context.repo.pkgDict();
    for(final byte[] p : pkg) {
      if(p != null) {
        final TokenList tl = new TokenList();
        tl.add(Package.name(p));
        tl.add(Package.version(p));
        tl.add(pkg.get(p));
        t.contents.add(tl);
      }
    }
    t.sort();
    out.println(t.finish());
    return true;
  }

  /**
   * Returns a list of all packages.
   * @param ctx database context
   * @return packages
   */
  public static StringList list(final Context ctx) {
    final StringList sl = new StringList();
    for(final byte[] p : ctx.repo.pkgDict()) {
      if(p != null) sl.add(Token.string(p));
    }
    sl.sort(!Prop.WIN, true);
    return sl;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.REPO + " " + CmdRepo.LIST).args();
  }
}
