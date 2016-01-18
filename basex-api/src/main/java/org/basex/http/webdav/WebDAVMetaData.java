package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;

import java.util.*;

import org.basex.util.http.*;

/**
 * Resource meta data.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Dimitar Popov
 */
final class WebDAVMetaData {
  /** Database owning the resource. */
  final String db;
  /** Resource path. */
  final String path;
  /** Resource last modification date. */
  final Date mdate;
  /** Raw binary file flag. */
  final boolean raw;
  /** Resource content type. */
  final MediaType type;
  /** Resource size in bytes. */
  final Long size;

  /** Default constructor. */
  WebDAVMetaData() {
    this(null, 0L);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param ms resource last modification date in milliseconds
   */
  WebDAVMetaData(final String db, final long ms) {
    this(db, "", ms);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param path resource path
   * @param ms resource last modification date in milliseconds
   */
  WebDAVMetaData(final String db, final String path, final long ms) {
    this(db, path, ms, false, null,  null);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param path resource path
   * @param ms resource last modification date in milliseconds
   * @param raw raw binary file flag
   * @param type resource media type
   * @param size resource size in bytes
   */
  WebDAVMetaData(final String db, final String path, final long ms, final boolean raw,
      final MediaType type, final Long size) {

    this.db = db;
    this.path = stripLeadingSlash(path);
    this.raw = raw;
    this.type = type;
    this.size = size;
    mdate = ms == -1 ? null : new Date(ms);
  }
}
