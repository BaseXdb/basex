package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Evaluates the 'store' command and stores binary content into the database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Store extends ACreate {
  /**
   * Constructor, specifying a target path.
   * The input needs to be set via {@link #setInput(InputStream)}.
   * @param path target path
   */
  public Store(final String path) {
    this(path, null);
  }

  /**
   * Constructor, specifying a target path and an input.
   * @param path target path
   * @param input input file
   */
  public Store(final String path, final String input) {
    super(Perm.WRITE, true, path == null ? "" : path, input);
  }

  @Override
  protected boolean run() {
    final boolean create = context.user().has(Perm.CREATE);
    String path = MetaData.normPath(args[0]);
    if(path == null) return error(PATH_INVALID_X, args[0]);

    if(in == null) {
      final IO io = IO.get(args[1]);
      if(!io.exists() || io.isDir()) return error(RES_NOT_FOUND_X, create ? io : args[1]);
      in = io.inputSource();
      // set/add name of document
      if((path.isEmpty() || Strings.endsWith(path, '/')) && !(io instanceof IOContent)) {
        path += io.name();
      }
    }

    // ensure that the name is not empty and contains no trailing dots
    final Data data = context.data();
    if(data.inMemory()) return error(NO_MAINMEM);

    final IOFile file = data.meta.binary(path);
    if(path.isEmpty() || Strings.endsWith(path, '.') || file == null)
      return error(PATH_INVALID_X, create ? path : args[0]);

    return update(data, new Code() {
      @Override
      boolean run() throws IOException {
        store(in, file);
        return info(QUERY_EXECUTED_X_X, "", jc().performance);
      }
    });
  }

  /**
   * Stores the specified source to the specified file.
   * @param in input source
   * @param file target file
   * @throws IOException I/O exception
   */
  public static void store(final InputSource in, final IOFile file) throws IOException {
    // add directory if it does not exist anyway
    if(file.isDir()) file.delete();
    file.parent().md();

    try(PrintOutput po = new PrintOutput(file)) {
      final Reader r = in.getCharacterStream();
      final InputStream is = in.getByteStream();
      final String id = in.getSystemId();
      if(r != null) {
        for(int c; (c = r.read()) != -1;) po.print(c);
      } else if(is != null) {
        for(int b; (b = is.read()) != -1;) po.write(b);
      } else if(id != null) {
        try(BufferInput bi = BufferInput.get(IO.get(id))) {
          for(int b; (b = bi.read()) != -1;) po.write(b);
        }
      }
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(S_TO, 0).add(1);
  }
}
