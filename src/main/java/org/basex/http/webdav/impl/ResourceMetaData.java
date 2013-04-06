package org.basex.http.webdav.impl;

import java.util.Date;

public class ResourceMetaData {
  public final String db;
  public final String path;
  public final long mod;
  public final Date mdate;
  public final boolean raw;
  public final String ctype;
  public final Long size;
  public final boolean folder;

  public ResourceMetaData(final String db, final long mod) {
    this(db, "", mod);
  }

  public ResourceMetaData(final String db, final String p, final long mod) {
    this(db, p, mod, false, null,  null, true);
  }

  public ResourceMetaData(final String db, final String p, final long mod,
      final boolean raw, final String ctype, final Long size) {
    this(db, p, mod, raw, ctype,  size, false);
  }
  public ResourceMetaData(final String db, final String p, final long mod,
      final boolean raw, final String ctype, final Long size, final boolean folder) {
    this.db = db;
    this.path = p;
    this.mod = mod;
    this.raw = raw;
    this.ctype = ctype;
    this.size = size;
    this.folder = folder;
    this.mdate = this.mod == -1 ? null : new Date(this.mod);
  }
}
