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
 * WebDAV resource representing a collection database.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXCollectionDatabase extends BXDatabase implements FolderResource {
  /**
   * Constructor.
   * @param db database containing the document
   * @param c context
   */
  public BXCollectionDatabase(final String db, final Context c) {
    dbname = db;
    ctx = c;
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
    final List<BXResource> dbs = new ArrayList<BXResource>();
    final TokenObjMap<IntList> dirs = new TokenObjMap<IntList>();
    try {
      new Open(dbname).execute(ctx);
      for(final int pre : ctx.doc()) {
        final byte[] doc = ctx.data.text(pre, true);
        final int idx = indexOf(doc, token(Prop.DIRSEP));
        if(idx <= 0)
          dbs.add(new BXDocument(dbname, string(doc), pre, ctx));
        else {
          // Folder
          final byte[] dir = substring(doc, 0, idx);
          if(dirs.get(dir) == null) dirs.add(dir, new IntList());
          dirs.get(dir).add(pre);
        }
      }
      for(final byte[] d : dirs)
        dbs.add(new BXFolder(dbname, dirs.get(d).toArray(), string(d), ctx));

      new Close().execute(ctx);
    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return dbs;
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
    // TODO
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
    // TODO Auto-generated method stub
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
