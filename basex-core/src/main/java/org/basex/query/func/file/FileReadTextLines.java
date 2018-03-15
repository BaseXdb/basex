package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FileReadTextLines extends FileRead {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkCreate(qc);

    final Path path = toPath(0, qc);
    final String encoding = toEncoding(1, FILE_UNKNOWN_ENCODING_X, qc);
    final boolean validate = exprs.length < 3 || !toBoolean(exprs[2], qc);
    final long start = exprs.length < 4 ? 1 : toLong(exprs[3], qc);
    final long end = exprs.length < 5 ? Long.MAX_VALUE : start + toLong(exprs[4], qc) - 1;

    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    final TokenList tl = new TokenList();
    try(NewlineInput ni = new NewlineInput(new IOFile(path.toFile()))) {
      ni.encoding(encoding).validate(validate);
      int c = 0;
      for(String line; (line = ni.readLine()) != null;) {
        if(++c >= start) tl.add(line);
        if(c == end) break;
      }
    } catch(final IOException ex) {
      throw FILE_IO_ERROR_X.get(info, ex);
    }
    return StrSeq.get(tl);
  }
}
