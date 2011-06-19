package org.basex.api.webdav;

import static org.basex.api.webdav.WebDAVServer.*;
import static java.lang.Integer.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import javax.xml.bind.ParseConversionEvent;

import org.basex.core.BaseXException;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXResourceFactory implements ResourceFactory {
  /** Not authorized resource. */
  private static final Resource NOAUTH = new BXNotAuthorizedResource();
  /** File path separator. */
  static final String DIRSEP = System.getProperty("file.separator");

  @Override
  public Resource getResource(final String host, final String p) {
    final Auth a = HttpManager.request().getAuthorization();
    if(a == null && System.getProperty(DBUSER) == null) return NOAUTH;

    final Path path = Path.path(p);
    // root
    if(path.isRoot()) return new BXAllDatabasesResource();
    final String[] parts = path.getParts();
    if(path.getLength() == 1) return new BXCollectionDatabase(parts[0]);
    final String f = p.substring(p.indexOf(parts[1]), p.length());
    try {
      final ClientSession cs = BXResource.login(a.getUser(), a.getPassword());
      try {
        // Check if there is a document in the collection having this path
        final ClientQuery q1 = cs.query("count(collection('" + parts[0]
            + "')/.[doc-name()='" + f + "'])");
        if(parseInt(q1.next()) == 1) return new BXDocument(parts[0],
            p.substring(p.indexOf(parts[1]), p.length()));
        // Check if there are paths in the collection startin with this path
        final ClientQuery q2 = cs.query("count(collection('" + parts[0]
            + "')/.[starts-with(doc-name(), '" + f + "')])");
        if(parseInt(q2.next()) > 0) return new BXFolder(parts[0], p.substring(
            p.indexOf(parts[1]), p.length() - 1));
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Checks if a path points to a folder.
   * @param db database name
   * @param path path
   * @param a authentication
   * @return true if path points to a folder, false 0 otherwise
   */
  private boolean isFolder(final String db, final String path, final Auth a) {
    boolean isFolder = false;
    try {
      final ClientSession cs = BXResource.login(a.getUser(), a.getPassword());
      try {
        // Check if there is a document in the collection having this path
        final ClientQuery q1 = cs.query("collection('" + db
            + "')/.[doc-name()='" + path + "']");
        if(parseInt(q1.next()) == 1) return false;
        final ClientQuery q2 = cs.query("count(collection('" + db
            + "')/.[starts-with(doc-name(), '" + path + "')])");
        if(parseInt(q2.next()) > 1) return true;
      } finally {
        cs.close();
      }
    } catch(Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return isFolder;
  }
}
