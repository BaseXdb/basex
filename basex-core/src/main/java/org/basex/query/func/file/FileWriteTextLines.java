package org.basex.query.func.file;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class FileWriteTextLines extends FileFn {
  /** Line separator. */
  private static final byte[] NL = token(Prop.NL);

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
    final Value value = exprs[1].value(qc);
    final String encoding = toEncoding(2, FILE_UNKNOWN_ENCODING_X, qc);
    final Charset cs = encoding == null || encoding == Strings.UTF8 ? null :
      Charset.forName(encoding);

    try(PrintOutput out = PrintOutput.get(new FileOutputStream(path.toFile(), append))) {
      for(final Item item : value) {
        if(!item.type.isStringOrUntyped()) throw typeError(item, AtomType.STR, info);
        final byte[] s = item.string(info);
        out.write(cs == null ? s : string(s).getBytes(cs));
        out.write(cs == null ? NL : Prop.NL.getBytes(cs));
      }
    }
    return null;
  }
}
