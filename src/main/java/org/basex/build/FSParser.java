package org.basex.build;

import java.io.File;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Text;
import org.deepfs.util.FSImporter;
import org.deepfs.util.FSTraversal;
import org.deepfs.util.FSWalker;

/**
 * Creates a database that maps a file system hierarchy.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Bastian Lemke
 */
public final class FSParser extends Progress {
  /** Filesystem importer. */
  private final FSImporter fsi;
  /** Root file(s). */
  private final File[] roots;

  /**
   * Constructor.
   * @param path import root
   * @param ctx the database context to use
   * @param name name of the database
   */
  public FSParser(final String path, final Context ctx, final String name) {
    fsi = new FSImporter(ctx);
    roots = path.equals("/") ? File.listRoots() : new File[] { new File(path) };
    fsi.createDB(name);
  }

  /** Recursively parses the given path. */
  public void parse() {
    final FSWalker walker = new FSWalker(fsi, new FSImporterProgress(this));
    for(final File file : roots) walker.traverse(file);
  }

  @Override
  public String tit() {
    return Text.CREATEFSPROG;
  }

  @Override
  public String det() {
    return fsi == null ? "" : fsi.getCurrentFileName();
  }

  /**
   * Used to stop the importer.
   * @author Bastian Lemke
   */
  private static class FSImporterProgress implements FSTraversal {
    /** The progress. */
    final Progress p;

    /**
     * Constructor.
     * @param progress the progress
     */
    FSImporterProgress(final Progress progress) { p = progress; }

    @Override
    public void levelUpdate(final int l) { /* NOT_USED */}

    @Override
    public void postDirectoryVisit(final File d) { /* NOT_USED */}

    @Override
    public void postTraversalVisit(final File d) { p.checkStop(); }

    @Override
    public void preDirectoryVisit(final File d) { /* NOT_USED */}

    @Override
    public void preTraversalVisit(final File d) { /* NOT_USED */}

    @Override
    public void regularFileVisit(final File f) { p.checkStop(); }

    @Override
    public void symLinkVisit(final File f) { /* NOT_USED */}
  }
}
