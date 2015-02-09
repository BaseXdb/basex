package org.basex.query.func.zip;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class ZipBinaryEntry extends ZipFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    return new B64(entry(qc));
  }

  /**
   * Returns an entry from a zip file.
   * @param qc query context
   * @return binary result
   * @throws QueryException query exception
   */
  final byte[] entry(final QueryContext qc) throws QueryException {
    final IOFile file = new IOFile(string(toToken(exprs[0], qc)));
    final String path = string(toToken(exprs[1], qc));
    if(!file.exists()) throw ZIP_NOTFOUND_X.get(info, file);

    try {
      return new Zip(file).read(path);
    } catch(final FileNotFoundException ex) {
      throw ZIP_NOTFOUND_X.get(info, file + "/" + path);
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }
}
