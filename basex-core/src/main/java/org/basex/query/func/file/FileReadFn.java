package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.nio.file.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.options.*;

/**
 * Functions for reading files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class FileReadFn extends FileFn {
  /** Parse Options. */
  public static final class ParseOptions extends Options {
    /** Encoding option. */
    public static final StringOption ENCODING = CommonOptions.ENCODING;
    /** Fallback option. */
    public static final BooleanOption FALLBACK = CommonOptions.FALLBACK;
  }

  /**
   * Parses the option arguments and checks the file path.
   * @param path file path
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  final ParseOptions options(final Path path, final QueryContext qc) throws QueryException {
    final Item arg1 = arg(1).unwrappedItem(qc, info);

    final ParseOptions po = new ParseOptions();
    if(arg1 instanceof final XQMap map) {
      toOptions(map, po, qc);
    } else {
      po.set(ParseOptions.ENCODING, toStringOrNull(arg1, qc));
    }
    toEncodingOrNull(po.get(ParseOptions.ENCODING), FILE_UNKNOWN_ENCODING_X);

    final boolean fallback = toBooleanOrFalse(arg(2), qc);
    if(fallback) po.set(ParseOptions.FALLBACK, true);

    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());
    if(!Files.isReadable(path)) throw FILE_ACCESS_X.get(info, path.toAbsolutePath());
    return po;
  }
}
