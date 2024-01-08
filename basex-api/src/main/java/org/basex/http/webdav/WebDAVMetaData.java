package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;

import java.util.*;

import org.basex.index.resource.*;
import org.basex.util.*;

/**
 * Resource meta data.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Dimitar Popov
 */
final class WebDAVMetaData {
  /** Database owning the resource (can be {@code null}). */
  final String db;
  /** Resource path. */
  final String path;
  /** Last modification date of resource. */
  final Date mdate;
  /** Resource type. */
  final String type;
  /** Resource content type. */
  final String contentType;
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
    this(db, path, ResourceType.XML.toString(), null, ms, null);
  }

  /**
   * Constructor.
   * @param db database owning the resource (can be {@code null})
   * @param path resource path
   * @param type resource type
   * @param contentType resource media type (can be {@code null})
   * @param ms resource last modification date (can be {@code null})
   * @param size resource size in bytes (can be {@code null} or empty)
   */
  WebDAVMetaData(final String db, final String path, final String type,
      final String contentType, final String ms, final String size) {

    this.db = db;
    this.path = stripLeadingSlash(path);
    this.type = type;
    this.contentType = contentType;
    this.size = size == null || size.isEmpty() ? null : Long.valueOf(size);
    mdate = ms == null ? null : new Date(DateTime.parse(ms).getTime());
  }
}
