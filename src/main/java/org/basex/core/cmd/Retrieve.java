package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
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
   * @param path source path
   */
  public Retrieve(final String path) {
    super(DATAREF, path);
  }

  @Override
  protected boolean run() throws IOException {
    try {
      return info(retrieve(args[0]));
    } catch(final BaseXException ex) {
      return error(DBNOTSTORED, ex.getMessage());
    }
  }

  /**
   * Sends raw data to the specified output stream.
   * @param source source path
   * @return info string
   * @throws BaseXException database exception
   */
  public String retrieve(final String source) throws BaseXException {
    final Data data = context.data();
    if(data == null) throw new BaseXException(PROCNODB);

    final IOFile bin = data.meta.binary(source);
    if(bin == null || !bin.exists() || bin.isDir() || !bin.isValid())
      throw new BaseXException(FILEWHICH, source);

    try {
      final BufferInput bi = new BufferInput(bin.file());
      try {
        for(int b; (b = bi.read()) != -1;) out.write(b);
      } finally {
        bi.close();
      }
    } catch(final IOException ex) {
      throw new BaseXException(ex.getMessage());
    }
    return Util.info(QUERYEXEC, perf);
  }
}
