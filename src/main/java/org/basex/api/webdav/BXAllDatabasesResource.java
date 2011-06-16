package org.basex.api.webdav;

import static org.basex.api.webdav.BXResourceFactory.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.basex.server.ClientSession;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;

/**
 * WebDAV resource representing the list of all databases.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXAllDatabasesResource extends BXResource implements
  FolderResource {
    // MakeCollectionableResource, PutableResource, GetableResource,
    // PropFindableResource {

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Resource child(final String childName) {
    try {
      final ClientSession cs = login(user, pass);
      try {
        return new BXDocumentDatabase(childName, user, pass);
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
  public List<? extends Resource> getChildren() {
    try {
      final List<BXResource> dbs = new ArrayList<BXResource>();
      final ClientSession cs = login(user, pass);
      try {
        for(final String d : listDatabases(cs))
          dbs.add(isCollection(cs, d) ?
              new BXCollectionDatabase(d) :
              new BXDocumentDatabase(d, user, pass));
        return dbs;
      } finally {
        cs.close();
      }
    } catch(final Exception e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public CollectionResource createCollection(final String newName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) {
//    final String dbname = dbname(newName);
//    if(!Command.validName(dbname, false)) return null;
//    try {
//      CreateDB.create(dbname, inputStream, ctx);
//      return new BXDocumentResource(cs, dbname);
//    } catch(BaseXException e) {
//      // [DP] WebDAV: error handling
//      e.printStackTrace();
//      return null;
//    }
    return null;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) { }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return null;
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
  public void copyTo(final CollectionResource toCollection, final String name) {
  }

  @Override
  public void delete() { }

  @Override
  public void moveTo(final CollectionResource rDest, final String name) { }
}
