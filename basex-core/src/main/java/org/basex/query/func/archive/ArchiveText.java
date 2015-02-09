package org.basex.query.func.archive;

import static org.basex.query.QueryText.*;

import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;

/**
 * Archive constants.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
interface ArchiveText {
  /** Packer format: gzip. */
  String GZIP = "gzip";
  /** Packer format: zip. */
  String ZIP = "zip";

  /** QName. */
  QNm Q_ENTRY = QNm.get(ARCHIVE_PREFIX, "entry", ARCHIVE_URI);
  /** QName. */
  QNm Q_OPTIONS = QNm.get(ARCHIVE_PREFIX, "options", ARCHIVE_URI);
  /** QName. */
  QNm Q_FORMAT = QNm.get(ARCHIVE_PREFIX, "format", ARCHIVE_URI);
  /** QName. */
  QNm Q_ALGORITHM = QNm.get(ARCHIVE_PREFIX, "algorithm", ARCHIVE_URI);
  /** Root node test. */
  NodeTest TEST = new NodeTest(Q_ENTRY);

  /** Level. */
  String LEVEL = "compression-level";
  /** Encoding. */
  String ENCODING = "encoding";
  /** Last modified. */
  String LAST_MOD = "last-modified";
  /** Compressed size. */
  String COMP_SIZE = "compressed-size";
  /** Uncompressed size. */
  String SIZE = "size";
  /** Value. */
  String VALUE = "value";

  /** Option: algorithm: deflate. */
  String DEFLATE = "deflate";
  /** Option: algorithm: stored. */
  String STORED = "stored";
  /** Option: algorithm: unknown. */
  String UNKNOWN = "unknown";
}
