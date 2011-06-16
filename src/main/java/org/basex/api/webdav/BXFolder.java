package org.basex.api.webdav;

import static org.basex.util.Token.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.util.IntList;
import org.basex.util.TokenObjMap;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;

/**
 * WebDAV resource representing a folder in a collection database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXFolder extends BXResource implements FolderResource {
  /** Database collection in which the folder resides. */
  private final String dbname;
  /** PRE values of documents contained in the directory. */
  private final int[] prevals;
  /** Path to folder. */
  private final String dirpath;

  /**
   * Constructor.
   * @param db database containing the document
   * @param pres PRE values of documents contained in the directory
   * @param p directory path
   */
  public BXFolder(final String db, final int[] pres, final String p) {
    dbname = db;
    prevals = pres;
    dirpath = p;
  }

  @Override
  public CollectionResource createCollection(final String newName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource child(final String childName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends Resource> getChildren() {
    // final List<BXResource> dbs = new ArrayList<BXResource>();
    // final TokenObjMap<IntList> dirs = new TokenObjMap<IntList>();
    // try {
    // new Open(dbname).execute(ctx);
    // for(final int pre : prevals) {
    // final String doc = string(ctx.data.text(pre, true));
    // final String s = doc.substring(dirpath.length(), doc.length());
    // final int idx = s.lastIndexOf(Prop.DIRSEP);
    // if(idx == 0) dbs.add(new BXDocument(dbname, s.substring(1, s.length()),
    // pre, ctx));
    // else if(idx > 0) {
    // final String[] parts = s.split(Prop.DIRSEP);
    // final byte[] dir = token(dirpath + Prop.DIRSEP + parts[1]);
    // if(dirs.get(dir) == null) dirs.add(dir, new IntList());
    // dirs.get(dir).add(pre);
    // }
    // }
    // for(final byte[] d : dirs)
    // dbs.add(new BXFolder(dbname, dirs.get(d).toArray(), string(d), ctx));
    //
    // new Close().execute(ctx);
    // } catch(BaseXException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // return dbs;
    return null;
  }

  @Override
  public String getName() {
    final int idx = dirpath.lastIndexOf('/');
    return idx >= 0 ? dirpath.substring(idx + 1, dirpath.length()) : dirpath;
  }

  @Override
  public Date getModifiedDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource createNew(final String newName, final InputStream inputStream,
      final Long length, final String contentType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void copyTo(final CollectionResource toCollection, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public void delete() {
    // TODO Auto-generated method stub
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType) {
    // TODO Auto-generated method stub
  }

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
  public void moveTo(final CollectionResource rDest, final String name) {
    // TODO Auto-generated method stub
  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
