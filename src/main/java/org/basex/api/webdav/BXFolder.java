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
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.util.IntList;
import org.basex.util.TokenObjMap;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public final class BXFolder extends BXResource implements FolderResource {

  /** Database collection in which the folder resides. */
  private final String dbname;
  /** Pre values of document nodes in which this folder is found. */
  private final int[] prevals;
  /** Path to folder. */
  private final String dirpath;

  public BXFolder(final String db, final int[] pres, final String p) {
    dbname = db;
    prevals = pres;
    dirpath = p;
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
      new Open(dbname).execute(ctx);
      String doc;
      final TokenObjMap<IntList> dirs = new TokenObjMap<IntList>();
      for(int pre : prevals) {
        doc = string(ctx.data.text(pre, true));
        String s = doc.substring(dirpath.length(), doc.length());
        int idx = s.lastIndexOf(Prop.DIRSEP);
        if(idx == 0) dbs.add(new BXDatabaseResource(dirpath.substring(1,
            dirpath.length())));
        else if(idx > 0) {
          String[] parts = s.split(Prop.DIRSEP);
          byte[] dir = token(dirpath + Prop.DIRSEP + parts[0]);
          if(dirs.get(dir) == null) {
            IntList l = new IntList();
            l.add(pre);
            dirs.add(dir, l);
          } else {
            dirs.get(dir).add(pre);
          }
        }
      }
      final Iterator<byte[]> dirsIt = dirs.iterator();
      byte[] dirName;
      while(dirsIt.hasNext()) {
        dirName = dirsIt.next();
        dbs.add(new BXFolder(dbname, dirs.get(dirName).toArray(),
            string(dirName)));
      }
    } catch(BaseXException e) {
      try {
        new Close().execute(ctx);
      } catch(BaseXException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
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
    final int idx = dirpath.lastIndexOf(Prop.DIRSEP);
    return idx >= 0 ? dirpath.substring(idx + 1, dirpath.length()) : dirpath;
  }

  @Override
  public Object authenticate(String user, String password) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean authorise(Request request, Method method, Auth auth) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getRealm() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String checkRedirect(Request request) {
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
