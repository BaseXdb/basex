package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.data.MetaData;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;

/**
 * Evaluates the 'retrieve' command and retrieves binary content.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Retrieve extends ACreate {
  /**
   * Default constructor.
   * @param path source path
   */
  public Retrieve(final String path) {
    super(DATAREF, path);
  }

  @Override
  protected boolean run() throws IOException {
    final String path = MetaData.normPath(args[0]);
    if(path == null) return error(NAMEINVALID, args[0]);

    final IOFile bin = context.data().meta.binary(path);
    if(bin == null || !bin.exists() || bin.isDir())
      return error(FILEWHICH, path);

    try {
      final BufferInput bi = bin.buffer();
      try {
        for(int b; (b = bi.read()) != -1;) out.write(b);
      } finally {
        bi.close();
      }
      return info(QUERYEXEC, perf);
    } catch(final IOException ex) {
      return error(DBNOTSTORED, ex.getMessage());
    }
  }
}
