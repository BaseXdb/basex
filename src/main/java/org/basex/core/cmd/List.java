package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.File;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.IOFile;
import org.basex.io.in.DataInput;
import org.basex.util.Table;
import org.basex.util.list.StringList;
import org.basex.util.list.TokenList;

/**
 * Evaluates the 'list' command and shows all available databases.
 *
 * @author BaseX Team 2005-11, BSD License
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
    table.description = DATABASES;

    final boolean create = context.user.perm(User.CREATE);
    table.header.add(INFODBNAME);
    table.header.add(INFONRES);
    table.header.add(INFODBSIZE);
    if(create) table.header.add(INFOPATH);

    for(final String name : list(context)) {
      DataInput di = null;
      String file = null;
      long size = 0;
      int ndocs = 0;
      final MetaData meta = new MetaData(name, context);
      try {
        di = new DataInput(meta.dbfile(DATAINF));
        meta.read(di);
        size = meta.dbsize();
        ndocs = meta.ndocs;
        if(context.perm(User.READ, meta)) file = meta.original.toString();
      } catch(final IOException ex) {
        file = INFODBERR;
      } finally {
        if(di != null) try { di.close(); } catch(final IOException ex) { }
      }

      // count number of raw files
      final File bin = new File(mprop.dbpath(name), M_RAW);
      final int raw = new IOFile(bin).descendants().size();

      // create entry
      if(file != null) {
        final TokenList tl = new TokenList(4);
        tl.add(name);
        tl.add(ndocs + raw);
        tl.add(size);
        if(create) tl.add(file);
        table.contents.add(tl);
      }
    }
    table.sort();
    out.println(table.finish());
    return true;
  }

  /**
   * Returns a list of all databases.
   * @param ctx database context
   * @return available databases
   */
  public static StringList list(final Context ctx) {
    final StringList db = new StringList();
    for(final IOFile f : ctx.mprop.dbpath().children()) {
      if(f.name().startsWith(".")) continue;
      if(f.isDir()) db.add(f.name());
    }
    db.sort(false, true);
    return db;
  }
}
