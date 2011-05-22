package org.basex.api.webdav;

import static org.basex.data.DataText.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.data.MetaData;
import org.basex.io.DataInput;
import org.basex.io.IO;
import org.basex.util.StringList;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * The list of all databases as WebDAV resource.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura, Dimitar Popov
 */
public class BXAllDatabasesResource extends BXResource implements
    FolderResource {

  /**
   * Constructor.
   * @param c database context
   */
  public BXAllDatabasesResource(final Context c) {
    ctx = c;
  }

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
    return new BXDatabaseResource(childName);
  }

  @Override
  public List<? extends Resource> getChildren() {
    final List<BXResource> dbs = new ArrayList<BXResource>();
    // Get all databases
    final StringList list = org.basex.core.cmd.List.list(ctx);
    for(final String db : list) {
      try {
        new Open(db).execute(ctx);
        if(ctx.data.meta.ndocs > 1) {
          dbs.add(new BXDatabaseCollection(db, ctx));
        } else if (ctx.data.meta.ndocs == 1) {
          dbs.add(new BXDatabaseResource(db));
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
    }
    try {
      new Close().execute(ctx);
    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return dbs;
  }

  @Override
  public CollectionResource createCollection(String arg0)
      throws NotAuthorizedException, ConflictException, BadRequestException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource createNew(String arg0, InputStream arg1, Long arg2,
      String arg3) throws IOException, ConflictException,
      NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void copyTo(final CollectionResource arg0, final String arg1)
      throws NotAuthorizedException, BadRequestException, ConflictException {
    // TODO Auto-generated method stub

  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
      BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public Long getContentLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getContentType(final String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendContent(final OutputStream arg0, final Range arg1,
      final Map<String, String> arg2, final String arg3) throws IOException,
      NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public void moveTo(final CollectionResource arg0, final String arg1)
      throws ConflictException, NotAuthorizedException, BadRequestException {
    // TODO Auto-generated method stub

  }

  @Override
  public Date getCreateDate() {
    // TODO Auto-generated method stub
    return null;
  }
}
