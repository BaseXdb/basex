package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
import org.basex.io.out.PrintOutput;

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

  /**
   * Default constructor.
   * @param target target path
   */
  public Store(final String target) {
    super(DATAREF | User.WRITE, target);
  }

  @Override
  protected boolean run() {
    final boolean create = context.user.perm(User.CREATE);
    if(in == null) {
      final IO file = IO.get(args[1]);
      if(!file.exists() || file.isDir())
        return error(FILEWHICH, create ? file : args[1]);
      in = file.inputSource();
    }

    final String path = MetaData.normPath(args[0]);
    if(path == null) return error(NAMEINVALID, args[0]);

    final IOFile file = context.data().meta.binary(path);
    if(file == null || file.isDir())
      return error(NAMEINVALID, create ? path : args[0]);

    new IOFile(file.dir()).md();

    PrintOutput po = null;
    try {
      po = new PrintOutput(file.path());
      try {
        final Reader r = in.getCharacterStream();
        final InputStream is = in.getByteStream();
        final String  id = in.getSystemId();
        if(r != null) {
          for(int c; (c = r.read()) != -1;) po.utf8(c);
        } else if(is != null) {
          for(int b; (b = is.read()) != -1;) po.write(b);
        } else if(id != null) {
          final BufferInput bi = IO.get(id).buffer();
          try {
            for(int b; (b = bi.read()) != -1;) po.write(b);
          } finally {
            bi.close();
          }
        }
      } finally {
        po.close();
      }
    } catch(final IOException ex) {
      return error(DBNOTSTORED, ex.getMessage());
    }
    return info(QUERYEXEC, perf);
  }
}
