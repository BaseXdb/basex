package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.io.IO;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'show backups' command and shows available backups.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ShowBackups extends Command {
  /**
   * Default constructor.
   */
  public ShowBackups() {
    super(User.CREATE);
  }

  @Override
  protected boolean run() throws IOException {
    final IO file = prop.dbpath();
    final IO[] files = file.children();
    int size = 0;
    final TokenBuilder tmp = new TokenBuilder();
    for(final IO f : files) {
      if(f.name().endsWith(IO.ZIPSUFFIX)) {
        tmp.add(NL + LI + f.name());
        ++size;
      }
    }
    final TokenBuilder tb = new TokenBuilder();
    if(size == 0) {
      tb.add(size + " Backup(s)" + DOT);
    } else {
      tb.add(size + " Backup(s)" + COL);
      tb.add(tmp.toString());
    }
    out.println(tb.toString());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.BACKUPS);
  }
}
