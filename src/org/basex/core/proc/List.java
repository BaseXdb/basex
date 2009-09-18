package org.basex.core.proc;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Context;
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
 * Evaluates the 'list' command and shows all available databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class List extends Process {
  /**
   * Default constructor.
   */
  public List() {
    super(PRINTING);
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    final String[] list = list(context).finish();
    final int ml = maxLength(list) + 2;

    if(list.length == 0) {
      o.println(INFONODB);
    } else {
      final TokenBuilder t = new TokenBuilder();
      o.print(Token.token(INFODBNAME), ml);
      o.println(INFODOC);
      o.println("--------------------------------------------");
      for(final String name : list) {
        o.print(Token.token(name), ml);
        DataInput in = null;
        try {
          in = new DataInput(prop.dbfile(name, DATAINFO));
          final MetaData md = new MetaData(name, in, prop);
          in.close();
          o.println(md.file.toString());
        } catch(final IOException ex) {
          o.println(INFODBERR);
        } finally {
          try { if(in != null) in.close(); } catch(final IOException ex) { }
        }
      }
      o.print(t.finish());
      o.print(NL + list.length + S + INFODBLIST + NL);
    }
  }

  /**
   * Returns a list of all databases.
   * @param ctx context reference
   * @return available databases.
   */
  public static StringList list(final Context ctx) {
    // create database list
    final StringList db = new StringList();

    final IO dir = IO.get(ctx.prop.get(Prop.DBPATH));
    if(!dir.exists()) return db;

    for(final IO f : dir.children()) {
      if(f.isDir() && !f.name().endsWith(".tmp")) db.add(f.name());
    }
    db.sort(false);
    return db;
  }
}
