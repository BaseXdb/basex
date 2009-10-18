package org.basex.core.proc;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.DataInput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.StringList;
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
    super(User.ADMIN);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    final String[] list = list(context).finish();
    int max = 0;
    for(final String s : list) max = Math.max(max, s.length());

    final TokenBuilder tb = new TokenBuilder();
    if(list.length == 0) {
      tb.add(INFONODB + NL);
    } else {
      tb.add(max + 2, INFODBNAME);
      tb.add(INFODOC + NL);
      tb.add("--------------------------------------------" + NL);
      for(final String name : list) {
        tb.add(max + 2, name);
        DataInput in = null;
        try {
          in = new DataInput(prop.dbfile(name, DATAINFO));
          final MetaData md = new MetaData(name, in, prop);
          in.close();
          tb.add(md.file + NL);
        } catch(final IOException ex) {
          tb.add(INFODBERR + NL);
        } finally {
          try { if(in != null) in.close(); } catch(final IOException ex) { }
        }
      }
      tb.add(NL + list.length + " " + INFODBLIST + NL);
    }
    out.print(tb.finish());
    return true;
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
  
  /**
   * Returns a list of all DeepFS databases.
   * @param ctx context reference
   * @return available databases.
   */
  public static StringList listFS(final Context ctx) {
    final StringList dbl = new StringList();

    final IO dir = IO.get(ctx.prop.get(Prop.DBPATH));
    if(!dir.exists()) return dbl;

    for(final IO f : dir.children()) {
      DataInput in = null;
      if(f.isDir() && !f.name().endsWith(".tmp")) {
        final String dbname = f.name();
        try {
          in = new DataInput(ctx.prop.dbfile(dbname, DATAINFO));
          final MetaData meta = new MetaData(dbname, in, ctx.prop);
          if (meta.deepfs)
            dbl.add(dbname);
        } catch(final IOException ex) {
          Main.debug(ex.getMessage()); 
        } finally {
          try { if(in != null) in.close(); } catch(final IOException ex) { }
        }
      }
    }
    dbl.sort(false);
    return dbl;
  }
}
