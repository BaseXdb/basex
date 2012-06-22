package org.basex.http.webdav;

import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;

/**
 * WebDAV resource representing the list of all databases.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXRoot extends BXFolder {
  /**
   * Constructor.
   * @param h http context
   */
  public BXRoot(final HTTPContext h) {
    super(null, null, 0, h);
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
  public BXResource child(final String name) {
    return new BXCode<BXResource>(this) {
      @Override
      public BXResource get() throws IOException {
        return dbExists(name, http) ? database(name, http) : null;
      }
    }.evalNoEx();
  }

  @Override
  public List<BXResource> getChildren() {
    return new BXCode<List<BXResource>>(this) {
      @Override
      public List<BXResource> get() throws IOException {
        final List<BXResource> dbs = new ArrayList<BXResource>();
        final LocalQuery q = http.session().query(
            "for $d in " + _DB_LIST_DETAILS.args() +
            "return ($d/text(), $d/@modified-date/data())");
        try {
          while(q.more()) {
            final String name = q.next();
            final long mod = Dtm.parse(q.next());
            dbs.add(new BXDatabase(name, mod, http));
          }
        } catch(final Exception ex) {
          Util.errln(ex);
        } finally {
          q.close();
        }
        return dbs;
      }
    }.evalNoEx();
  }

  @Override
  public BXDatabase createCollection(final String newName) throws BadRequestException {
    return new BXCode<BXDatabase>(this) {
      @Override
      public BXDatabase get() throws IOException {
        final String dbname = dbname(newName);
        http.session().execute(new CreateDB(dbname));
        return database(dbname, http);
      }
    }.eval();
  }

  @Override
  public BXResource createNew(final String newName, final InputStream input,
      final Long length, final String contentType) throws BadRequestException {

    return new BXCode<BXDatabase>(this) {
      @Override
      public BXDatabase get() throws IOException {
        addFile(newName, input);
        return database(dbname(newName), http);
      }
    }.eval();
  }

  @Override
  protected void addXML(final String n, final InputStream in) throws IOException {
    http.session().create(dbname(n), in);
  }

  @Override
  protected void addRaw(final String n, final InputStream in) throws IOException {
    final LocalSession session = http.session();
    session.execute(new CreateDB(dbname(n)));
    session.store(n, in);
  }
}
