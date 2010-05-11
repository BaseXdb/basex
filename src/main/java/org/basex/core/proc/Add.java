package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;

/**
 * Evaluates the 'add' command and adds a single document to a collection.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class Add extends ACreate {

  /** Target path for collections. */
  private String target;

  /**
   * Default constructor.
   * @param input input XML file or XML string
   */
  public Add(final String input) {
    super(DATAREF | User.WRITE, input);
    this.target = "";
  }

  /**
   * Default constructor.
   * @param name name of database
   * @param input input XML file or XML string
   */
  public Add(final String name, final String input) {
    super(DATAREF | User.WRITE, name, input);
    this.target = "";
  }

  /**
   * ADD <code>doc</code> INTO <code>targetPath</code>.
   * @param input doc
   * @param ta target
   * @param a add to collection: allow coexsitence with
   *          {@link #Add(String, String)}.
   */
  public Add(final String input, final String ta,
      @SuppressWarnings("unused") final boolean a) {
    super(DATAREF | User.WRITE, input);
    String tr = ta + "/";
    tr = tr.replaceAll("//+", "/");
    this.target = tr.startsWith("/") ? tr.substring(1) : tr;
  }

  @Override
  protected boolean run() {
    IO io;
    String name;
    String dbname;
    if(args.length == 2) {
      io = IO.get(args[1]);
      name = args[0];
      dbname = name;
    } else {
      io = IO.get(args[0]);
      name = io.name();
      dbname = io.dbname();
    }
    if(!io.exists()) return error(FILEWHICH, io);
    
//    final int pre = findDoc(Token.token(target + io.name()));
//    if(pre != -1) return error(DBDOC, args[0]); // checks only
    
    final DirParser p = new DirParser(io, context.prop, target);

    MemData d = null;
    try {
      d = new MemBuilder(p).build(dbname);
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : name);
    }
    final Data data = context.data;
    data.insert(data.meta.size, -1, d);
    data.flush();
    context.update();
    return info(DOCADDED, name, perf);
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }
}
