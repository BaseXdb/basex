package org.basex.query.func.file;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class FileWriteText extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    return write(false, qc);
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param qc query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized Item write(final boolean append, final QueryContext qc)
      throws QueryException, IOException {

    final Path path = checkParentDir(toPath(0, qc));
    final byte[] s = toToken(exprs[1], qc);
    final String enc = toEncoding(2, FILE_UNKNOWN_ENCODING_X, qc);
    final Charset cs = enc == null || enc == UTF8 ? null : Charset.forName(enc);

    try(final PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      out.write(cs == null ? s : string(s).getBytes(cs));
    }
    return null;
  }
}
