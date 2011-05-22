package org.basex.api.webdav;

import static org.basex.util.Token.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Open;
import org.basex.util.IntList;
import org.basex.util.TokenList;
import org.basex.util.TokenObjMap;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * Database collection as WebDAV resource.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura, Dimitar Popov
 */
public class BXDatabaseCollection extends BXResource implements FolderResource {

  /** Collection name. */
  private final String collname;

  public BXDatabaseCollection(final String n, final Context c) {
    collname = n;
    ctx = c;
  }

  @Override
  public CollectionResource createCollection(String newName)
      throws NotAuthorizedException, ConflictException, BadRequestException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource child(String childName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends Resource> getChildren() {
    final List<BXResource> dbs = new ArrayList<BXResource>();
    try {
      // Open database
      new Open(collname).execute(ctx);
      // Get all document nodes
      final TokenObjMap<IntList> dirs = new TokenObjMap<IntList>();
      byte[] doc;
      for(int pre : ctx.doc()) {
        doc = ctx.data.text(pre, true);
        int idx = indexOf(doc, token(Prop.DIRSEP));
        if(idx > 0) {
          // Folder
          byte[] dir = substring(doc, 0, idx);
          if(dirs.get(dir) == null) {
            IntList l = new IntList();
            l.add(pre);
            dirs.add(dir, l);
          } else {
            dirs.get(dir).add(pre);
          }
        } else {
          // XML file
          dbs.add(new BXDocumentResource(ctx, string(doc)));
        }
      }
      final Iterator<byte[]> dirsIt = dirs.iterator();
      byte[] dirName;
      while(dirsIt.hasNext()) {
        dirName = dirsIt.next();
        dbs.add(new BXFolder(collname, dirs.get(dirName).toArray(),
            string(dirName)));
      }
    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return dbs;
  }

  @Override
  public String getUniqueId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return collname;
  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource createNew(String newName, InputStream inputStream,
      Long length, String contentType) throws IOException, ConflictException,
      NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void copyTo(CollectionResource toCollection, String name)
      throws NotAuthorizedException, BadRequestException, ConflictException {
    // TODO Auto-generated method stub

  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
      BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendContent(OutputStream out, Range range,
      Map<String, String> params, String contentType) throws IOException,
      NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub
    int i = 0;
  }

  @Override
  public Long getMaxAgeSeconds(Auth auth) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(String accepts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void moveTo(CollectionResource rDest, String name)
      throws ConflictException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }

}
