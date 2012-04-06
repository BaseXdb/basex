package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'show backups' command and shows available backups.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ShowBackups extends Command {
  /**
   * Default constructor.
   */
  public ShowBackups() {
    super(Perm.CREATE);
  }

  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = BACKUPS;
    table.header.add(NAME);
    table.header.add(SIZE);

    for(final IO f : mprop.dbpath().children()) {
      final String name = f.name();
      if(!name.endsWith(IO.ZIPSUFFIX)) continue;
      final TokenList tl = new TokenList();
      tl.add(name);
      tl.add(f.length());
      table.contents.add(tl);
    }
    table.sort();
    out.println(table.finish());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.BACKUPS);
  }
}
