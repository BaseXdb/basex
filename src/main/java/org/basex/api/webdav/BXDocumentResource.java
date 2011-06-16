package org.basex.api.webdav;

import static org.basex.api.webdav.BXResourceFactory.*;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.basex.core.Text;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.server.ClientSession;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * WebDAV resource representing a single-document.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXDocumentResource extends BXResource implements FileResource {
  /** Default content type. */
  private static final String MIMETYPEXML = "text/xml";
  /** Date format of the "Time Stamp" field in INFO DB. */
  private static final DateFormat DATEFORMAT = new SimpleDateFormat(
      "dd.MM.yyyy HH:mm:ss");
  /** Database name. */
  private final String dbname;

  /**
   * Constructor.
   * @param n database name
   * @param u user name
   * @param p user password
   */
  public BXDocumentResource(final String n, final String u, final String p) {
    dbname = n;
    user = u;
    pass = p;
  }

  @Override
  public Date getModifiedDate() {
    try {
      final ClientSession cs = login(user, pass);
      try {
        cs.execute(new Open(dbname));
        final String info = cs.query("db:info()").execute();
        cs.execute(new Close());

        // parse the timestamp
        final String timestamp = "Time Stamp: ";
        final int p = info.indexOf(timestamp);
        if(p < 0) return null;
        final String date = info.substring(p + timestamp.length(),
            info.indexOf(Text.NL, p));
        return DATEFORMAT.parse(date);
      } finally {
        cs.close();
      }
    } catch(Exception e) {
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
    // TODO
  }

  @Override
  public void delete() {
    try {
      final ClientSession cs = login(user, pass);
      try {
        cs.execute("drop database " + dbname);
      } finally {
        cs.close();
      }
    } catch(Exception e) {
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
      final Map<String, String> params, final String contentType) {
    try {
      final ClientSession cs = login(user, pass);
      try {
        cs.setOutputStream(out);
        cs.execute(new Open(dbname));
        cs.query(".").execute();
        cs.execute(new Close());
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
    }
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name)
      throws ConflictException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files)
      throws BadRequestException, NotAuthorizedException, ConflictException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
