package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.basex.core.BaseXException;
import org.basex.core.User;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
import org.basex.io.out.PrintOutput;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.xml.sax.InputSource;

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
  protected boolean run() throws IOException {
    if(is == null) {
      final IO in = IO.get(args[1]);
      if(!in.exists() || in.isDir()) return error(FILEWHICH, in);
      is = in.inputSource();
    }

    final String target = args[0];
    final IOFile file = context.data().meta.binary(target);
    if(file == null || file.isDir() || !file.isValid())
      return error(NAMEINVALID, target);

    try {
      return info(store(file, is));
    } catch(final IOException ex) {
      return error(DBNOTSTORED, ex.getMessage());
    }
  }

  /**
   * Stores data from the specified input stream in the database.
   * @param target target file
   * @param input new content
   * @return info string
   * @throws BaseXException database exception
   */
  public static String store(final IOFile target, final InputSource input)
      throws BaseXException {

    final Performance perf = new Performance();
    new IOFile(target.dir()).md();

    PrintOutput out = null;
    try {
      out = new PrintOutput(target.path());
      final Reader r = input.getCharacterStream();
      final InputStream is = input.getByteStream();
      final String  id = input.getSystemId();
      if(r != null) {
        for(int c; (c = r.read()) != -1;) out.utf8(c);
      } else if(is != null) {
        for(int b; (b = is.read()) != -1;) out.write(b);
      } else if(id != null) {
        final BufferInput bi = IO.get(id).buffer();
        try {
          for(int b; (b = bi.read()) != -1;) out.write(b);
        } finally {
          bi.close();
        }
      }
    } catch(final IOException ex) {
      throw new BaseXException(DBNOTSTORED, target);
    } finally {
      if(out != null) try { out.close(); } catch(final IOException ex) { }
    }
    return Util.info(QUERYEXEC, perf);
  }
}
