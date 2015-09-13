package org.basex.http.webdav.impl;

import static org.basex.http.webdav.impl.WebDAVUtils.*;

import java.util.*;

import org.basex.util.http.*;

/**
 * Resource meta data.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class ResourceMetaData {
  /** Database owning the resource. */
  public final String db;
  /** Resource path. */
  public final String path;
  /** Resource last modification date. */
  public final Date mdate;
  /** Raw binary file flag. */
  public final boolean raw;
  /** Resource content type. */
  public final MediaType type;
  /** Resource size in bytes. */
  public final Long size;

  /** Default constructor. */
  public ResourceMetaData() {
    this(null, 0L);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param ms resource last modification date in milliseconds
   */
  public ResourceMetaData(final String db, final long ms) {
    this(db, "", ms);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param path resource path
   * @param ms resource last modification date in milliseconds
   */
  ResourceMetaData(final String db, final String path, final long ms) {
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
  ResourceMetaData(final String db, final String path, final long ms, final boolean raw,
      final MediaType type, final Long size) {
    this.db = db;
    this.path = stripLeadingSlash(path);
    this.raw = raw;
    this.type = type;
    this.size = size;
    mdate = ms == -1 ? null : new Date(ms);
  }
}
