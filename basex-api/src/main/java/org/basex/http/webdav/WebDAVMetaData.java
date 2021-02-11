package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Resource meta data.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
final class WebDAVMetaData {
  /** Database owning the resource (can be {@code null}). */
  final String db;
  /** Resource path. */
  final String path;
  /** Last modification date of resource. */
  final Date mdate;
  /** Raw binary file flag. */
  final boolean raw;
  /** Resource content type. */
  final MediaType type;
  /** Resource size in bytes. */
  final Long size;

  /** Default constructor. */
  WebDAVMetaData() {
    this(null, null);
  }

  /**
   * Constructor.
   * @param db database owning the resource (can be {@code null})
   * @param ms resource last modification date (can be {@code null})
   */
  WebDAVMetaData(final String db, final String ms) {
    this(db, "", ms);
  }

  /**
   * Constructor.
   * @param db database owning the resource (can be {@code null})
   * @param path resource path
   * @param ms resource last modification date (can be {@code null})
   */
  WebDAVMetaData(final String db, final String path, final String ms) {
    this(db, path, ms, false, null, null);
  }

  /**
   * Constructor.
   * @param db database owning the resource (can be {@code null})
   * @param path resource path
   * @param ms resource last modification date (can be {@code null})
   * @param raw raw binary file flag
   * @param type resource media type (can be {@code null})
   * @param size resource size in bytes (can be {@code null} or empty)
   */
  WebDAVMetaData(final String db, final String path, final String ms, final boolean raw,
      final MediaType type, final String size) {

    this.db = db;
    this.path = stripLeadingSlash(path);
    this.raw = raw;
    this.type = type;
    this.size = size == null || size.isEmpty() ? null : Long.valueOf(size);
    mdate = ms == null ? null : new Date(DateTime.parse(ms).getTime());
  }
}
