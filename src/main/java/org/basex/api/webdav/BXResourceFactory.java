package org.basex.api.webdav;

import static org.basex.data.DataText.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.data.MetaData;
import org.basex.io.DataInput;
import org.basex.io.IO;
import org.basex.util.StringList;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura, Dimitar Popov
 */
public class BXResourceFactory implements ResourceFactory {
  /** Database context. */
  private Context ctx;

  /**
   * Constructor.
   * @param c database context
   */
  public BXResourceFactory(final Context c) {
    ctx = c;
  }

  @Override
  public Resource getResource(final String host, final String p) {
    final Path path = Path.path(p);
    if(path.isRoot()) return new BXAllDatabasesResource(ctx);
    else if(path.getLength() == 1) {
      return new BXDatabaseCollection(path.getFirst(), ctx);
    }
    return null;

  }

  /**
   * Finds all databases.
   * @param ctx context
   * @return list with databases
   * @throws IOException
   */
  // public List<BXResource> findAllDatabases(final String name)
  // throws IOException {
  // final List<BXResource> dbs = new ArrayList<BXResource>();
  // final IO dir = IO.get(name);
  // for(final IO f : dir.children()) {
  // if(f.name().startsWith(".")) continue;
  // if(f.isDir()) {
  // final MetaData meta = new MetaData(f.name(), ctx.prop);
  // DataInput in = new DataInput(meta.file(DATAINFO));
  // meta.read(in);
  // if(meta.ndocs == 1) {
  // dbs.add(new BXDatabaseResource(f.name()));
  // } else if(meta.ndocs > 1) {
  // dbs.add(new BXDatabaseCollection(f.name()));
  // }
  // }
  // }
  // return dbs;
  // }
  //
  // private BXResource findDatabase(final String path) throws IOException {
  // final IO io = IO.get(ctx.prop.get(Prop.DBPATH) + path);
  // // TODO: path does not exist
  // if(io.isDir() && !io.name().startsWith(".")) {
  // final MetaData meta = new MetaData(io.name(), ctx.prop);
  // DataInput in = new DataInput(meta.file(DATAINFO));
  // meta.read(in);
  // if(meta.ndocs == 1) {
  // return new BXDatabaseResource(io.name());
  // } else if(meta.ndocs > 1) {
  // return new BXDatabaseCollection(io.name());
  // } else {
  // return null;
  // }
  // }
  // return null;
  // }
}
