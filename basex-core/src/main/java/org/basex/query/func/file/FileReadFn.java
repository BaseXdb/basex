package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
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
    public static final StringOption ENCODING = new StringOption(CommonOptions.ENCODING, null,
        Types.STRING_ZO);
    /** Fallback option. */
    public static final BooleanOption FALLBACK = new BooleanOption(CommonOptions.FALLBACK, false);
    /** Normalize newlines. */
    public static final BooleanOption NORMALIZE_NEWLINES =
        new BooleanOption(CommonOptions.NORMALIZE_NEWLINES, true);
  }

  /**
   * Parses the option arguments and checks the file path.
   * @param path file path
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final ParseOptions options(final Path path, final QueryContext qc)
      throws QueryException, IOException {
    final Item options = arg(1).unwrappedItem(qc, info);

    final ParseOptions po = new ParseOptions();
    if(options instanceof final XQMap map) {
      toOptions(map, po, qc);
    } else {
      po.set(ParseOptions.ENCODING, toStringOrNull(options, qc));
    }
    toEncodingOrNull(po.get(ParseOptions.ENCODING), FILE_UNKNOWN_ENCODING_X);

    final boolean fallback = toBooleanOrFalse(arg(2), qc);
    if(fallback) po.set(ParseOptions.FALLBACK, true);

    checkReadable(path);
    return po;
  }
}
