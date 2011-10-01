package org.basex.api.webdav;

import static org.basex.query.func.Function.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.cmd.CreateDB;
import org.basex.io.IO;
import org.basex.server.Query;
import org.basex.server.Session;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;

/**
 * WebDAV resource representing an XML document.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXDocument extends BXAbstractResource implements FileResource {
  /** Raw flag. */
  final boolean raw;
  /** Content type. */
  final String ctype;

  /**
   * Constructor.
   * @param dbname name of database this document belongs to.
   * @param docpath document path to root
   * @param s current session
   * @param r raw flag
   * @param c content type
   */
  public BXDocument(final String dbname, final String docpath,
      final HTTPSession s, final boolean r, final String c) {
    super(dbname, docpath, s);
    raw = r;
    ctype = c;
  }

  @Override
  public Long getContentLength() {
    return null;
  }

  @Override
  public Date getCreateDate() {
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files) throws BadRequestException {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return ctype;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException, BadRequestException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        s.setOutputStream(out);
        final Query q = s.query(raw ? "declare option output:method 'raw'; " +
            DBRETRIEVE.args("$db", "$path") : DBOPEN.args("$db", "$path"));
        q.bind("db", db);
        q.bind("path", path);
        q.execute();
      }
    }.eval();
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws IOException {

    // document is copied to the root: create new database with it
    final String nm = n.endsWith(IO.XMLSUFFIX) ?
        n.substring(0, n.length() - IO.XMLSUFFIX.length()) : n;
    s.execute(new CreateDB(nm));
    add(s, nm, "", n);
  }

  @Override
  protected void copyTo(final Session s, final BXFolder f, final String n)
      throws IOException {

    // folder is copied to a folder in a database
    add(s, f.db, f.path, n);
    deleteDummy(s, f.db, f.path);
  }

  /**
   * Adds a document to the specified target.
   * @param s current session
   * @param trgdb target database
   * @param trgdir target directory
   * @param name new name
   * @throws IOException I/O exception
   */
  protected void add(final Session s, final String trgdb, final String trgdir,
      final String name) throws IOException {

    final Query q = s.query(
        "if (" + DBISRAW.args("$sdb", "$spath") + ") then " +
        DBSTORE.args("$db", "$path||'/'||$name",
            DBRETRIEVE.args("$sdb", "$spath")) + " else " +
        DBADD.args("$db", DBOPEN.args("$sdb", "$spath"), "$name", "$path"));
    q.bind("db", trgdb);
    q.bind("sdb", db);
    q.bind("spath", path);
    q.bind("name", name);
    q.bind("path", trgdir);
    q.execute();
  }
}
