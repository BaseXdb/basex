package org.basex.http.webdav;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.server.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;

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
   * @param h http context
   */
  protected BXAbstractResource(final String d, final String p, final long m,
      final HTTPContext h) {
    super(d, p, m, h);
  }

  @Override
  public void delete() throws BadRequestException {
    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        del();
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
          copyToRoot(name);
        else if(target instanceof BXFolder)
          copyTo((BXFolder) target, name);
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
          moveToRoot(name);
        else if(target instanceof BXFolder)
          moveTo((BXFolder) target, name);
      }
    }.eval();
  }

  /**
   * Delete document or folder.
   * @throws IOException I/O exception
   */
  protected void del() throws IOException {
    final LocalSession session = http.session();
    session.execute(new Open(db));
    session.execute(new Delete(path));

    // create dummy, if parent is an empty folder
    final int ix = path.lastIndexOf(SEP);
    if(ix > 0) createDummy(path.substring(0, ix));
  }

  /**
   * Rename document or folder.
   * @param n new name
   * @throws IOException I/O exception
   */
  protected void rename(final String n) throws IOException {
    final LocalSession session = http.session();
    session.execute(new Open(db));
    session.execute(new Rename(path, n));

    // create dummy, if old parent is an empty folder
    final int i1 = path.lastIndexOf(SEP);
    if(i1 > 0) createDummy(path.substring(0, i1));

    // delete dummy, if new parent is an empty folder
    final int i2 = n.lastIndexOf(SEP);
    if(i2 > 0) deleteDummy(n.substring(0, i2));
  }

  /**
   * Copy folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected abstract void copyToRoot(final String n) throws IOException;

  /**
   * Copy folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected abstract void copyTo(final BXFolder f, final String n) throws IOException;

  /**
   * Move folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected void moveToRoot(final String n) throws IOException {
    // folder is moved to the root: create new database with it
    copyToRoot(n);
    del();
  }

  /**
   * Move folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected void moveTo(final BXFolder f, final String n) throws IOException {
    if(f.db.equals(db)) {
      // folder is moved to a folder in the same database
      rename(f.path + SEP + n);
    } else {
      // folder is moved to a folder in another database
      copyTo(f, n);
      del();
    }
  }
}
