package org.basex.query.func.archive;

import static org.basex.util.Token.*;
import static org.basex.query.QueryText.*;

import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;

/**
 * Archive constants.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
interface ArchiveText {
  /** Packer format: gzip. */
  String GZIP = "gzip";
  /** Packer format: zip. */
  String ZIP = "zip";

  /** QName. */
  QNm Q_ENTRY = new QNm(ARCHIVE_PREFIX, "entry", ARCHIVE_URI);
  /** Root node test. */
  NameTest ENTRY = new NameTest(Q_ENTRY);

  /** Level. */
  byte[] LEVEL = token("compression-level");
  /** Encoding. */
  byte[] ENCODING = token("encoding");
  /** Last modified. */
  byte[] LAST_MODIFIED = token("last-modified");
  /** Compressed size. */
  byte[] COMPRESSED_SIZE = token("compressed-size");
  /** Uncompressed size. */
  byte[] SIZE = token("size");

  /** Option: algorithm: deflate. */
  String DEFLATE = "deflate";
  /** Option: algorithm: stored. */
  String STORED = "stored";
  /** Option: algorithm: unknown. */
  String UNKNOWN = "unknown";
}
