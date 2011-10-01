package org.basex.api.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.basex.api.HTTPSession;
import org.basex.core.cmd.CreateDB;
import org.basex.server.Session;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.exceptions.BadRequestException;

/**
 * WebDAV resource representing the list of all databases.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXRootResource extends BXFolder {
  /**
   * Constructor.
   * @param s current session
   */
  public BXRootResource(final HTTPSession s) {
    super(null, null, s);
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // this method must do nothing
  }

  @Override
  public void delete() {
    // this method must do nothing
  }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) {
    // this method must do nothing
  }

  @Override
  public BXResource child(final String childName) {
    return new BXCode<BXResource>(this) {
      @Override
      public BXResource get() throws IOException {
        return listDBs(s).contains(childName) ?
            new BXDatabase(childName, session) : null;
      }
    }.evalNoEx();
  }

  @Override
  public List<BXResource> getChildren() {
    return new BXCode<List<BXResource>>(this) {
      @Override
      public List<BXResource> get() throws IOException {
        final List<BXResource> dbs = new ArrayList<BXResource>();
        for(final String d : listDBs(s)) dbs.add(new BXDatabase(d, session));
        return dbs;
      }
    }.evalNoEx();
  }

  @Override
  public BXDatabase createCollection(final String newName)
      throws BadRequestException {

    return new BXCode<BXDatabase>(this) {
      @Override
      public BXDatabase get() throws IOException {
        final String dbname = dbname(newName);
        s.execute(new CreateDB(dbname));
        return new BXDatabase(dbname, session);
      }
    }.eval();
  }

  @Override
  public BXResource createNew(final String newName, final InputStream input,
      final Long length, final String contentType) throws BadRequestException {

    return new BXCode<BXDatabase>(this) {
      @Override
      public BXDatabase get() throws IOException {
        addFile(s, newName, input);
        return new BXDatabase(dbname(newName), session);
      }
    }.eval();
  }

  @Override
  protected void addXML(final Session s, final String n, final InputStream in)
      throws IOException {
    s.create(dbname(n), in);
  }

  @Override
  protected void addRaw(final Session s, final String n, final InputStream in)
      throws IOException {
    s.execute(new CreateDB(dbname(n)));
    s.store(n, in);
  }
}
