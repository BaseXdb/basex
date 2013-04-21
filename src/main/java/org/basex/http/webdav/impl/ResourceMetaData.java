package org.basex.http.webdav.impl;

import static org.basex.http.webdav.impl.Utils.*;
import java.util.Date;

/**
 * Resource meta data.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public class ResourceMetaData {
  /** Database owning the resource. */
  public final String db;
  /** Resource path. */
  public final String path;
  /** Resource last modification date in milliseconds. */
  public final long mod;
  /** Resource last modification date. */
  public final Date mdate;
  /** Raw binary file flag. */
  public final boolean raw;
  /** Resource content type. */
  public final String ctype;
  /** Resource size in bytes. */
  public final Long size;
  /** Folder flag. */
  public final boolean folder;

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
    this(d, p, m, false, null,  null, true);
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
    this(d, p, m, r, c,  s, false);
  }

  /**
   * Constructor.
   * @param d database owning the resource
   * @param p resource path
   * @param m resource last modification date in milliseconds
   * @param r raw binary file flag
   * @param c resource content type
   * @param s resource size in bytes
   * @param f folder flag
   */
  public ResourceMetaData(final String d, final String p, final long m,
      final boolean r, final String c, final Long s, final boolean f) {
    this.db = d;
    this.path = stripLeadingSlash(p);
    this.mod = m;
    this.raw = r;
    this.ctype = c;
    this.size = s;
    this.folder = f;
    this.mdate = this.mod == -1 ? null : new Date(this.mod);
  }
}
