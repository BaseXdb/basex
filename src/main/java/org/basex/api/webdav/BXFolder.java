package org.basex.api.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
import org.basex.data.DataText;
import org.basex.server.Query;
import org.basex.server.Session;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableCollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.BadRequestException;

/**
 * WebDAV resource representing a folder within a collection database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXFolder extends BXAbstractResource implements FolderResource,
    DeletableCollectionResource {

  /**
   * Constructor.
   * @param dbname database name
   * @param folderPath path to folder
   * @param s current session
   */
  public BXFolder(final String dbname, final String folderPath,
      final HTTPSession s) {
    super(dbname, folderPath, s);
  }

  @Override
  public Long getContentLength() {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
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
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
  }

  @Override
  public BXFolder createCollection(final String folder)
      throws BadRequestException {

    return new BXCode<BXFolder>(this) {
      @Override
      public BXFolder get() throws IOException {
        // [DP] WebDAV: possible optimization would be to rename the dummy, if
        // the current folder is empty (which not always the case)
        deleteDummy(s, db, path);
        final String newFolder = path + SEP + folder;
        createDummy(s, db, newFolder);
        return new BXFolder(db, newFolder, session);
      }
    }.eval();
  }

  @Override
  public BXResource child(final String childName) {
    return new BXCode<BXResource>(this) {
      @Override
      public BXResource get() {
        return resource(s, db, path + SEP + childName, session);
      }
    }.evalNoEx();
  }

  @Override
  public List<BXResource> getChildren() {
    return new BXCode<List<BXResource>>(this) {
      @Override
      public List<BXResource> get() throws IOException {
        final List<BXResource> ch = new ArrayList<BXResource>();
        final HashSet<String> paths = new HashSet<String>();
        final Query q = s.query(
            "for $r in db:list($d, $p) return substring-after($r,$p)");
        q.bind("d", db);
        q.bind("p", path);
        while(q.more()) {
          final String p = stripLeadingSlash(q.next());
          final int ix = p.indexOf(SEP);
          // check if document or folder
          if(ix < 0) {
            if(!p.equals(DUMMY))
              ch.add(new BXDocument(db, path + SEP + p, session,
                  isRaw(s, db, path + SEP + p)));
          } else {
            final String folder = path + SEP + p.substring(0, ix);
            if(!paths.contains(folder)) {
              paths.add(folder);
              ch.add(new BXFolder(db, folder, session));
            }
          }
        }
        q.close();
        return ch;
      }
    }.evalNoEx();
  }

  @Override
  public BXResource createNew(final String newName, final InputStream input,
      final Long length, final String contentType) throws BadRequestException {

    return new BXCode<BXResource>(this) {
      @Override
      public BXDocument get() throws IOException {
        s.execute(new Open(db));
        final String doc = path.isEmpty() ? newName : path + SEP + newName;
        // check if document with this path already exists
        if(count(s, db, doc) == 0) {
          addFile(s, newName, input, contentType);
          deleteDummy(s, db, path);
        } else {
          s.replace(doc, input);
        }
        return new BXDocument(db, doc, session, isRaw(s, db, doc));
      }
    }.eval();
  }

  /**
   * Add file content in the current folder.
   * @param s active session
   * @param n file name
   * @param in file content
   * @param mime content type
   * @throws IOException I/O exception
   */
  protected void addFile(final Session s, final String n, final InputStream in,
      final String mime)
      throws IOException {

    // decide if file will be added as an XML document or raw file
    final String c = contentType(n);
    if(c == null && DataText.APP_OCTET.equals(mime)) {
      final BufferedInputStream bi = new BufferedInputStream(in);
      bi.mark(BufferedInputStream.MAX);
      try {
        addXML(s, n, bi);
      } catch(final IOException e) {
        if(bi.pos() >= BufferedInputStream.MAX) throw e;
        bi.reset();
        addRaw(s, n, bi);
      } finally {
        if(bi.pos() < BufferedInputStream.MAX) bi.reset();
        bi.close();
      }
    } else {
      if(mime.contains("xml"))
        addXML(s, n, in);
      else
        addRaw(s, n, in);
    }
  }

  /**
   * Add XML document in the current folder.
   * @param s active session
   * @param n file name
   * @param in file content
   * @throws IOException I/O exception
   */
  protected void addXML(final Session s, final String n, final InputStream in)
      throws IOException {
    s.add(n, path, in);
  }

  /**
   * Add raw file in the current folder.
   * @param s active session
   * @param n file name
   * @param in file content
   * @throws IOException I/O exception
   */
  protected void addRaw(final Session s, final String n, final InputStream in)
      throws IOException {
    s.store(path + SEP + n, in);
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws IOException {
    // folder is copied to the root: create new database with it
    s.execute(new CreateDB(n));
    add(s, n, "");
    s.execute(new Close());
  }

  @Override
  protected void copyTo(final Session s, final BXFolder f, final String n)
      throws IOException {
    // folder is copied to a folder in a database
    add(s, f.db, f.path + SEP + n);
    deleteDummy(s, f.db, f.path);
  }

  /**
   * Add all documents in the folder to another folder.
   * @param s current session
   * @param trgdb target database
   * @param trgdir target folder
   * @throws IOException I/O exception
   */
  private void add(final Session s, final String trgdb, final String trgdir)
      throws IOException {
    final Query q = s.query(
        "for $d in db:list($db, $path) " +
        "let $t := tokenize(substring($d, string-length($path) + 1), '/') " +
        "let $p := string-join(($trgdir, $t[position() < last()]), '/') " +
        "let $n := $t[last()] " +
        "return " +
        "db:add($trgdb, db:open($db, $d), $n, $p)");
    q.bind("db", db);
    q.bind("path", path);
    q.bind("trgdb", trgdb);
    q.bind("trgdir", trgdir);
    q.execute();
  }

  @Override
  public boolean isLockedOutRecursive(final Request request) {
    return false;
  }
}

/**
 * Simple wrapper around {@link java.io.BufferedInputStream} which provides the
 * following extensions:<br/>
 * 1) the current position of the input stream is accessible<br/>
 * 2) {@link #close()} does nothing, if {@link #mark(int)} has been called, and
 * {@link #reset()} not, yet.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
class BufferedInputStream extends java.io.BufferedInputStream {
  /** Max number of resettable bytes. */
  public static final int MAX = 1000000;

  /**
   * Constructor.
   * @param input wrapped input stream.
   */
  public BufferedInputStream(final InputStream input) {
    super(input);
  }

  /**
   * Current position of the input stream.
   * @return position
   */
  public int pos() {
    return pos;
  }

  @Override
  public void close() {
    if(markpos < 0 || markpos == pos) close();
  }
}
