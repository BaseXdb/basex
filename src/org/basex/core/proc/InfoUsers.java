package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.User;
import org.basex.core.Users;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdInfo;
import org.basex.io.PrintOutput;
import org.basex.util.StringList;

/**
 * Evaluates the 'info users' command and returns user information.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class InfoUsers extends AInfo {
  /**
   * Default constructor.
   */
  public InfoUsers() {
    super(DATAREF | User.ADMIN);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    Users loc = context.data().meta.users;
    Users glob = context.users;
    StringList remove = new StringList();
    StringList tmp1 = new StringList();
    StringList tmp2 = new StringList();
    for(User u : loc.getUsers()) {
      tmp1.add(u.name);
    }
    for(User u : glob.getUsers()) {
      tmp2.add(u.name);
    }
    for(String s : tmp1) {
      if(!tmp2.contains(s)) {
        remove.add(s);
      }
    }
    for(String s : remove) {
      loc.remove(loc.get(s));
    }
    out.println(context.data().meta.users.info());
    return true;
  }

  @Override
  public String toString() {
    return Cmd.INFO + " " + CmdInfo.USERS;
  }
}
