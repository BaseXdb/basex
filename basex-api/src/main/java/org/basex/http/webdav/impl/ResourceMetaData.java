package org.basex.http.webdav.impl;

import static org.basex.http.webdav.impl.Utils.*;

import java.util.*;

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
  public final String ctype;
  /** Resource size in bytes. */
  public final Long size;

  /** Default constructor. */
  public ResourceMetaData() {
    this(null, 0L);
  }

  /**
   * Constructor.
   * @param d database owning the resource
   * @param m resource last modification date in milliseconds
   */
  public ResourceMetaData(final String d, final long m) {
    this(d, "", m);
  }

  /**
   * Constructor.
   * @param d database owning the resource
   * @param p resource path
   * @param m resource last modification date in milliseconds
   */
  ResourceMetaData(final String d, final String p, final long m) {
    this(d, p, m, false, null,  null);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param path resource path
   * @param ms resource last modification date in milliseconds
   * @param raw raw binary file flag
   * @param ctype resource content type
   * @param s resource size in bytes
   */
  ResourceMetaData(final String db, final String path, final long ms, final boolean raw,
      final String ctype, final Long s) {
    this.db = db;
    this.path = stripLeadingSlash(path);
    this.raw = raw;
    this.ctype = ctype;
    this.size = s;
    mdate = ms == -1 ? null : new Date(ms);
  }
}
