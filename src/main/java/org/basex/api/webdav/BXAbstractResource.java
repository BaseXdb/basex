package org.basex.api.webdav;

import java.io.IOException;

import org.basex.api.HTTPSession;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Rename;
import org.basex.server.Session;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.exceptions.BadRequestException;

/**
 * WebDAV resource representing an abstract folder within a collection database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXAbstractResource extends BXResource implements
    CopyableResource, DeletableResource, MoveableResource {

  /**
   * Constructor.
   * @param d database name
   * @param p path to folder
   * @param m last modified date
   * @param s current session
   */
  public BXAbstractResource(final String d, final String p, final long m,
      final HTTPSession s) {
    super(d, p, m, s);
  }

  @Override
  public void delete() throws BadRequestException {
    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        delete(s);
      }
    }.eval();
  }

  @Override
  public void copyTo(final CollectionResource target, final String name)
      throws BadRequestException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof BXRoot)
          copyToRoot(s, name);
        else if(target instanceof BXFolder)
          copyTo(s, (BXFolder) target, name);
      }
    }.eval();
  }

  @Override
  public void moveTo(final CollectionResource target, final String name)
      throws BadRequestException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof BXRoot)
          moveToRoot(s, name);
        else if(target instanceof BXFolder)
          moveTo(s, (BXFolder) target, name);
      }
    }.eval();
  }

  /**
   * Delete document or folder.
   * @param s current session
   * @throws IOException I/O exception
   */
  protected void delete(final Session s) throws IOException {
    s.execute(new Open(db));
    s.execute(new Delete(path));

    // create dummy, if parent is an empty folder
    final int ix = path.lastIndexOf(SEP);
    if(ix > 0) createDummy(s, db, path.substring(0, ix));
  }

  /**
   * Rename document or folder.
   * @param s current session
   * @param n new name
   * @throws IOException I/O exception
   */
  protected void rename(final Session s, final String n) throws IOException {
    s.execute(new Open(db));
    s.execute(new Rename(path, n));

    // create dummy, if old parent is an empty folder
    final int i1 = path.lastIndexOf(SEP);
    if(i1 > 0) createDummy(s, db, path.substring(0, i1));

    // delete dummy, if new parent is an empty folder
    final int i2 = n.lastIndexOf(SEP);
    if(i2 > 0) deleteDummy(s, db, n.substring(0, i2));
  }

  /**
   * Copy folder to the root, creating a new database.
   * @param s current session
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected abstract void copyToRoot(final Session s, final String n)
      throws IOException;

  /**
   * Copy folder to another folder.
   * @param s current session
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected abstract void copyTo(final Session s, final BXFolder f,
      final String n) throws IOException;

  /**
   * Move folder to the root, creating a new database.
   * @param s current session
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected void moveToRoot(final Session s, final String n)
      throws IOException {
    // folder is moved to the root: create new database with it
    copyToRoot(s, n);
    delete(s);
  }

  /**
   * Move folder to another folder.
   * @param s current session
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected void moveTo(final Session s, final BXFolder f, final String n)
      throws IOException {
    if(f.db.equals(db)) {
      // folder is moved to a folder in the same database
      rename(s, f.path + SEP + n);
    } else {
      // folder is moved to a folder in another database
      copyTo(s, f, n);
      delete(s);
    }
  }
}
