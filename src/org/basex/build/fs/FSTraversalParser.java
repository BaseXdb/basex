package org.basex.build.fs;

import java.io.File;

import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Text;
import org.basex.core.proc.Close;
import org.deepfs.util.FSImporter;
import org.deepfs.util.FSWalker;

/**
 * Creates a database that maps a file system hierarchy.
 * @author Bastian Lemke
 */
public class FSTraversalParser extends Progress {

  // [BL] fix windows paths
  // [BL] representation for multiple windows devices.
  
  /** The FSImporter. */
  private FSImporter importer;

  /**
   * Recursively parses the given path.
   * @param path a directory.
   * @param context the database context to use.
   * @param dbName the name of the database to create.
   */
  public void parse(final String path, final Context context,
      final String dbName) {
    importer = new FSImporter(context, dbName);
    new Close().execute(context);
    new FSWalker(importer).traverse(new File(path));
  }

  @Override
  public String tit() {
    return Text.CREATEFSPROG;
  }

  @Override
  public String det() {
    return importer == null ? "" : importer.getCurrentFileName();
  }

  @Override
  public double prog() {
    return 0;
  }

}
