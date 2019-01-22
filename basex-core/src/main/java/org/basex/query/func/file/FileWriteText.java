package org.basex.query.func.file;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @return {@code null}
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized Item write(final boolean append, final QueryContext qc)
      throws QueryException, IOException {

    final Path path = checkParentDir(toPath(0, qc));
    final AStr text = (AStr) checkType(exprs[1], qc, AtomType.STR);
    final String encoding = toEncoding(2, FILE_UNKNOWN_ENCODING_X, qc);
    final Charset cs = encoding == null || encoding == Strings.UTF8 ? null :
      Charset.forName(encoding);

    try(PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      if(cs == null) {
        try(BufferInput in = text.input(info)) {
          for(int b; (b = in.read()) != -1;) out.write(b);
        }
      } else {
        out.write(string(text.string(info)).getBytes(cs));
      }
    }
    return null;
  }
}
