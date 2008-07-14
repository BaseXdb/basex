package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.StringList;

/**
 * Evaluates the 'list' command. Shows all available databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class List extends Proc {
  @Override
  protected boolean exec() {
    return true;
  }
  

  @Override
  protected void out(final PrintOutput o) throws IOException {
    final StringList list = list();

    final StringBuilder sb = new StringBuilder();
    int c = 0;
    for(final String name : list.finish()) {
      sb.append(name);
      int l = name.length();
      while(l++ < 14) sb.append(" ");
      try {
        final MetaData md = new MetaData(name);
        md.read();
        sb.append(md.file);
        c++;
      } catch(final IOException ex) {
        BaseX.debug(ex);
        sb.append(INFODBERR);
      }
      sb.append(NL);
    }
    if(c == 0) {
      o.print(INFONODB);
    } else {
      o.print("Database      XML Document" + NL);
      o.print("--------------------------------------------" + NL);
      o.print(sb + NL + c + INFODBLIST + NL);
    }
  }

  /**
   * Returns the list of available databases.
   * @return available databases.
   */
  public static StringList list() {
    // create database list
    final StringList db = new StringList();

    final IO dir = new IO(Prop.dbpath);
    // no database directory found...
    if(!dir.exists()) return db;

    for(final IO f : dir.children()) if(f.isDir()) db.add(f.name());
    db.sort();
    return db;
  }
}
