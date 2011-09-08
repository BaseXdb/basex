package org.basex.api.webdav;

import java.io.IOException;

import org.basex.api.HTTPSession;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Rename;
import org.basex.server.Session;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * WebDAV resource representing an abstract folder within a collection database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXAbstractResource extends BXResource implements
    CopyableResource, DeletableResource, MoveableResource {

  /**
   * Constructor.
   * @param dbname database name
   * @param folderPath path to folder
   * @param s current session
   */
  public BXAbstractResource(final String dbname, final String folderPath,
      final HTTPSession s) {
    super(dbname, folderPath, s);
  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
    BadRequestException {
    Session s = null;
    try {
      s = session.login();
      delete(s);
    } catch(final Exception ex) {
      handle(ex);
      throw new BadRequestException(this, ex.getMessage());
    } finally {
      try { if(s != null) s.close(); } catch(final IOException e) { handle(e); }
    }
  }

  @Override
  public void copyTo(final CollectionResource target, final String name) throws
    NotAuthorizedException, BadRequestException, ConflictException {
    Session s = null;
    try {
      s = session.login();
      if(target instanceof BXAllDatabasesResource)
        copyToRoot(s, name);
      else if(target instanceof BXFolder)
        copyTo(s, (BXFolder) target, name);
    } catch(final Exception ex) {
      handle(ex);
      throw new BadRequestException(this, ex.getMessage());
    } finally {
      try { if(s != null) s.close(); } catch(final IOException e) { handle(e); }
    }
  }

  @Override
  public void moveTo(final CollectionResource target, final String name) throws
    ConflictException, NotAuthorizedException, BadRequestException {
    Session s = null;
    try {
      s = session.login();
      if(target instanceof BXAllDatabasesResource)
        moveToRoot(s, name);
      else if(target instanceof BXFolder)
        moveTo(s, (BXFolder) target, name);
    } catch(final Exception ex) {
      handle(ex);
      throw new BadRequestException(this, ex.getMessage());
    } finally {
      try { if(s != null) s.close(); } catch(final IOException e) { handle(e); }
    }
  }

  /**
   * Delete document or folder.
   * @param s current session
   * @throws BaseXException database exception
   */
  protected void delete(final Session s) throws BaseXException {
    s.execute(new Open(db));
    s.execute(new Delete(path));

    // create dummy, if parent is an empty folder
    final int ix = path.lastIndexOf(SEP);
    if(ix > 0) createDummy(s, db, path.substring(0, ix));
    s.execute(new Close());
  }

  /**
   * Rename document or folder.
   * @param s current session
   * @param n new name
   * @throws BaseXException database exception
   */
  protected void rename(final Session s, final String n) throws BaseXException {
    s.execute(new Open(db));
    s.execute(new Rename(path, n));

    // create dummy, if old parent is an empty folder
    final int i1 = path.lastIndexOf(SEP);
    if(i1 > 0) createDummy(s, db, path.substring(0, i1));

    // delete dummy, if new parent is an empty folder
    final int i2 = n.lastIndexOf(SEP);
    if(i2 > 0) deleteDummy(s, db, n.substring(0, i2));

    s.execute(new Close());
  }

  /**
   * Copy folder to the root, creating a new database.
   * @param s current session
   * @param n new name of the folder (database)
   * @throws BaseXException database exception
   */
  protected abstract void copyToRoot(final Session s, final String n)
      throws BaseXException;

  /**
   * Copy folder to another folder.
   * @param s current session
   * @param f target folder
   * @param n new name of the folder
   * @throws BaseXException database exception
   */
  protected abstract void copyTo(final Session s, final BXFolder f,
      final String n) throws BaseXException;

  /**
   * Move folder to the root, creating a new database.
   * @param s current session
   * @param n new name of the folder (database)
   * @throws BaseXException database exception
   */
  protected void moveToRoot(final Session s, final String n)
      throws BaseXException {
    // folder is moved to the root: create new database with it
    copyToRoot(s, n);
    delete(s);
  }

  /**
   * Move folder to another folder.
   * @param s current session
   * @param f target folder
   * @param n new name of the folder
   * @throws BaseXException database exception
   */
  protected void moveTo(final Session s, final BXFolder f, final String n)
      throws BaseXException {
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
