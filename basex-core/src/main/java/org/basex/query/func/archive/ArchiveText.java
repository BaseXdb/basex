package org.basex.query.func.archive;

import static org.basex.query.QueryText.*;

import org.basex.query.value.item.*;

/**
 * Archive constants.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
interface ArchiveText {
  /** QName. */
  QNm Q_ENTRY = new QNm(ARCHIVE_PREFIX, "entry", ARCHIVE_URI);
  /** QName. */
  QNm Q_LEVEL = new QNm("compression-level");
  /** QName. */
  QNm Q_ENCODING = new QNm("encoding");
  /** QName. */
  QNm Q_LAST_MODIFIED = new QNm("last-modified");
  /** QName. */
  QNm Q_COMPRESSED_SIZE = new QNm("compressed-size");
  /** QName. */
  QNm Q_SIZE = new QNm("size");

  /** Option: algorithm: deflate. */
  String DEFLATE = "deflate";
  /** Option: algorithm: stored. */
  String STORED = "stored";
  /** Option: algorithm: unknown. */
  String UNKNOWN = "unknown";

  /** Packer format: gzip. */
  String GZIP = "gzip";
  /** Packer format: zip. */
  String ZIP = "zip";
}
