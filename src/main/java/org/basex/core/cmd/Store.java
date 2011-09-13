package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.io.out.DataOutput;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * Evaluates the 'store' command and stores binary content into the database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Store extends ACreate {
  /**
   * Default constructor.
   * @param target target path
   * @param input input file
   */
  public Store(final String target, final String input) {
    super(DATAREF | User.WRITE, target, input);
  }

  @Override
  protected boolean run() throws IOException {
    final IOFile in = new IOFile(args[1]);
    if(!in.exists() || in.isDir()) return error(FILEWHICH, in);
    try {
      final BufferedInputStream bis =
          new BufferedInputStream(new FileInputStream(in.file()));
      return info(store(args[0], bis, context, false));
    } catch(final BaseXException ex) {
      return error(DBNOTSTORED, in);
    }
  }
  
  /**
   * Stores data from the specified input stream in the database.
   * @param target target path
   * @param input input stream
   * @param ctx database context
   * @param lock if {@code true}, register a write lock in context
   * @return info string
   * @throws BaseXException database exception
   */
  public static String store(final String target, final InputStream input,
      final Context ctx, final boolean lock) throws BaseXException {

    final Performance perf = new Performance();
    final Data data = ctx.data();
    if(data == null) throw new BaseXException(PROCNODB);

    final IOFile bin = data.meta.binary(target);
    if(target.isEmpty() || !bin.valid())
      throw new BaseXException(NAMEINVALID, target);


    new IOFile(bin.dir()).md();
    try {
      if(lock) ctx.register(true);
      final DataOutput out = new DataOutput(bin.file()); 
      try {
        for(int b; (b = input.read()) != -1;) out.write(b);
      } finally {
        out.close();
      }
    } catch(final IOException ex) {
      throw new BaseXException(DBNOTSTORED, target);
    } finally {
      if(lock) ctx.unregister(true);
    }
    return Util.info(QUERYEXEC, perf);
  }
}
