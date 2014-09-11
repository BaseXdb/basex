package org.basex.query.func.archive;

import static org.basex.query.QueryText.*;

import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;

/**
 * Admin constants.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
interface ArchiveText {
  /** Packer format: gzip. */
  String GZIP = "gzip";
  /** Packer format: zip. */
  String ZIP = "zip";

  /** Module prefix. */
  String PREFIX = "archive";
  /** QName. */
  QNm Q_ENTRY = QNm.get(PREFIX, "entry", ARCHIVEURI);
  /** QName. */
  QNm Q_OPTIONS = QNm.get(PREFIX, "options", ARCHIVEURI);
  /** QName. */
  QNm Q_FORMAT = QNm.get(PREFIX, "format", ARCHIVEURI);
  /** QName. */
  QNm Q_ALGORITHM = QNm.get(PREFIX, "algorithm", ARCHIVEURI);
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
