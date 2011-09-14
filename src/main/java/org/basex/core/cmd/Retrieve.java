package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * Evaluates the 'retrieve' command and retrieves binary content.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Retrieve extends ACreate {
  /**
   * Default constructor.
   * @param path path
   */
  public Retrieve(final String path) {
    super(DATAREF, path);
  }

  @Override
  protected boolean run() throws IOException {
    final Data data = context.data();
    final IOFile bin = data.meta.binary(args[0]);
    if(!bin.exists() || bin.isDir()) return error(FILEWHICH, args[0]);

    out.write(bin.read());
    return info(QUERYEXEC, perf);
  }

  /**
   * Sends raw data to the specified output stream.
   * @param source source path
   * @param ctx database context
   * @param out output stream
   * @return info string
   * @throws BaseXException database exception
   */
  public static String retrieve(final String source, final OutputStream out,
      final Context ctx) throws BaseXException {

    final Performance perf = new Performance();
    final Data data = ctx.data();
    if(data == null) throw new BaseXException(PROCNODB);

    final IOFile bin = data.meta.binary(source);
    if(!bin.exists() || bin.isDir())
      throw new BaseXException(FILEWHICH, source);

    try {
      try {
        final BufferedInputStream bis =
            new BufferedInputStream(new FileInputStream(bin.file()));
        for(int b; (b = bis.read()) != -1;) out.write(b);
      } finally {
        out.close();
      }
    } catch(final IOException ex) {
      throw new BaseXException(ex.getMessage());
    }
    return Util.info(QUERYEXEC, perf);
  }
}
