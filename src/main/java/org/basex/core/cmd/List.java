package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.DataInput;
import org.basex.io.IO;
import org.basex.util.StringList;
import org.basex.util.Table;
import org.basex.util.TokenList;
import org.basex.util.Util;

/**
 * Evaluates the 'list' command and shows all available databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class List extends Command {
  /**
   * Default constructor.
   */
  public List() {
    super(STANDARD);
  }

  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.desc = DATABASES;

    final boolean create = context.user.perm(User.CREATE);
    table.header.add(INFODBNAME);
    table.header.add(INFONDOCS);
    table.header.add(INFODBSIZE);
    if(create) table.header.add(INFODOC);

    for(final String name : list(context)) {
      DataInput in = null;
      String file = null;
      long size = 0;
      int ndocs = 0;
      final MetaData meta = new MetaData(name, prop);
      try {
        in = new DataInput(meta.file(DATAINFO));
        meta.read(in);
        size = meta.dbsize();
        ndocs = meta.ndocs;
        if(context.perm(User.READ, meta)) file = meta.file.toString();
      } catch(final IOException ex) {
        file = INFODBERR;
      } finally {
        if(in != null) try { in.close(); } catch(final IOException ex) { }
      }
      if(file != null) {
        final TokenList sl = new TokenList();
        sl.add(name);
        sl.add(ndocs);
        sl.add(size);
        if(create) sl.add(file);
        table.contents.add(sl);
      }
    }
    table.sort();
    out.println(table.finish());
    return true;
  }

  /**
   * Returns a list of all databases.
   * @param ctx context reference
   * @return available databases
   */
  public static StringList list(final Context ctx) {
    // create database list
    final StringList db = new StringList();

    final IO dir = IO.get(ctx.prop.get(Prop.DBPATH));
    if(!dir.exists()) return db;

    for(final IO f : dir.children()) {
      if(f.name().startsWith(".")) continue;
      if(f.isDir()) db.add(f.name());
    }
    db.sort(false, true);
    return db;
  }

  /**
   * Returns a list of all DeepFS databases.
   * @param ctx context reference
   * @return available databases
   */
  public static StringList listFS(final Context ctx) {
    final StringList dbs = list(ctx);
    final StringList dbl = new StringList();

    for(final String name : dbs) {
      DataInput in = null;
      final MetaData meta = new MetaData(name, ctx.prop);
      try {
        in = new DataInput(meta.file(DATAINFO));
        meta.read(in);
        if(meta.deepfs) dbl.add(name);
      } catch(final IOException ex) {
        Util.debug(ex.getMessage());
      } finally {
        if(in != null) try { in.close(); } catch(final IOException ex) { }
      }
    }
    return dbl;
  }
}
