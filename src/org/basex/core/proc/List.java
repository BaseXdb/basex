package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.MetaData;
import org.basex.io.DataInput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'list' command. Shows all available databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
    final int ml = maxLength(list) + 2;

    if(list.length == 0) {
      o.println(INFONODB);
    } else {
      final TokenBuilder t = new TokenBuilder();
      o.print(Token.token("Database"), ml);
      o.println("XML Document");
      o.println("--------------------------------------------");
      for(final String name : list) {
        o.print(Token.token(name), ml);
        DataInput in = null;
        try {
          in = new DataInput(name, DATAINFO);
          final MetaData md = new MetaData(name);
          md.read(in);
          in.close();
          o.println(md.file.toString());
        } catch(final IOException ex) {
          o.println(INFODBERR);
        } finally {
          try { if(in != null) in.close(); } catch(final IOException e) { }
        }
      }
      o.print(t.finish());
      o.print(NL + list.length + INFODBLIST + NL);
    }
  }

  /**
   * Returns the list of available databases.
   * @return available databases.
   */
  public static StringList list() {
    // create database list
    final StringList db = new StringList();

    final IO dir = IO.get(Prop.dbpath);
    // no database directory found...
    if(!dir.exists()) return db;

    for(final IO f : dir.children()) if(f.isDir()) db.add(f.name());
    db.sort(false);
    return db;
  }
}
