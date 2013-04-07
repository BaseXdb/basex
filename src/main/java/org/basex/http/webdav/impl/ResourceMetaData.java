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
   * @param db database owning the resource
   * @param mod resource last modification date in milliseconds
   */
  public ResourceMetaData(final String db, final long mod) {
    this(db, "", mod);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param p resource path
   * @param mod resource last modification date in milliseconds
   */
  public ResourceMetaData(final String db, final String p, final long mod) {
    this(db, p, mod, false, null,  null, true);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param p resource path
   * @param mod resource last modification date in milliseconds
   * @param raw raw binary file flag
   * @param ctype resource content type
   * @param size resource size in bytes
   */
  public ResourceMetaData(final String db, final String p, final long mod,
      final boolean raw, final String ctype, final Long size) {
    this(db, p, mod, raw, ctype,  size, false);
  }

  /**
   * Constructor.
   * @param db database owning the resource
   * @param p resource path
   * @param mod resource last modification date in milliseconds
   * @param raw raw binary file flag
   * @param ctype resource content type
   * @param size resource size in bytes
   * @param folder folder flag
   */
  public ResourceMetaData(final String db, final String p, final long mod,
      final boolean raw, final String ctype, final Long size, final boolean folder) {
    this.db = db;
    this.path = stripLeadingSlash(p);
    this.mod = mod;
    this.raw = raw;
    this.ctype = ctype;
    this.size = size;
    this.folder = folder;
    this.mdate = this.mod == -1 ? null : new Date(this.mod);
  }
}
