package org.basex.http.webdav.impl;

import java.util.Date;

import static org.basex.http.webdav.impl.Utils.stripLeadingSlash;

/**
 * Resource meta data.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class ResourceMetaData {
  /** Database owning the resource. */
  public final String db;
  /** Resource path. */
  public final String path;
  /** Resource last modification date in milliseconds. */
  private final long mod;
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
  public ResourceMetaData(final String d, final String p, final long m) {
    this(d, p, m, false, null,  null);
  }

  /**
   * Constructor.
   * @param d database owning the resource
   * @param p resource path
   * @param m resource last modification date in milliseconds
   * @param r raw binary file flag
   * @param c resource content type
   * @param s resource size in bytes
   */
  public ResourceMetaData(final String d, final String p, final long m,
      final boolean r, final String c, final Long s) {
    db = d;
    path = stripLeadingSlash(p);
    mod = m;
    raw = r;
    ctype = c;
    size = s;
    mdate = mod == -1 ? null : new Date(mod);
  }
}
