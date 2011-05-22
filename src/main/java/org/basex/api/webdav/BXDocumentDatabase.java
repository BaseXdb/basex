package org.basex.api.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Copy;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.data.XMLSerializer;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;

/**
 * WebDAV resource representing a document database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXDocumentDatabase extends BXResource implements FileResource {
  /** Database name. */
  private final String dbname;

  /**
   * Constructor.
   * @param c database context
   * @param n database name
   */
  public BXDocumentDatabase(final Context c, final String n) {
    ctx = c;
    dbname = n;
  }

  @Override
  public Date getModifiedDate() {
    try {
      new Open(dbname).execute(ctx);
      final Date d = new Date(ctx.data.meta.time);
      new Close().execute(ctx);
      return d;
    } catch(BaseXException e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String getName() {
    return dbname;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    try {
      new Copy(dbname, name).execute(ctx);
    } catch(BaseXException e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
    }
  }

  @Override
  public void delete() {
    try {
      new DropDB(dbname).execute(ctx);
    } catch(BaseXException e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
    }
  }

  @Override
  public Long getContentLength() {
    // content length is null, because we don't know if the resulting XML will
    // have the same size as the original document (which is available in
    // ctx.data.meta.filesize):
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return MIMETYPEXML;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException {
    try {
      new Open(dbname).execute(ctx);
      final XMLSerializer ser = new XMLSerializer(out);
      ctx.current.serialize(ser);
      ser.close();
      new Close().execute(ctx);
    } catch(BaseXException e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
    }
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
