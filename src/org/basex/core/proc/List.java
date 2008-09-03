package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;

import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'list' command. Shows all available databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class List extends Process {
  /**
   * Constructor.
   */
  public List() {
    super(PRINTING);
  }
  
  @Override
  protected void out(final PrintOutput o) throws IOException {
    final String[] list = list().finish();
    final int ml = maxLength(list) + 1;

    final TokenBuilder tb = new TokenBuilder();
    int c = 0;
    for(final String name : list) {
      tb.add(name, ml);
      try {
        final MetaData md = new MetaData(name);
        md.read();
        tb.add(md.file);
        c++;
      } catch(final IOException ex) {
        tb.add(INFODBERR);
      }
      tb.add(NL);
    }
    if(c == 0) {
      o.print(INFONODB + NL);
    } else {
      final TokenBuilder t = new TokenBuilder();
      t.add("Database", ml);
      t.add("XML Document" + NL);
      t.add("--------------------------------------------" + NL);
      o.print(t.finish());
      o.print(tb + NL + c + INFODBLIST + NL);
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
