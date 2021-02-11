package org.basex.gui.layout;

import org.basex.core.*;

/**
 * JTree node which represents the root folder. The root folder carries
 * the name of the database and has basically an artificial path ('/')
 * which leads to problems if treated like an ordinary folder node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class ResourceRootFolder extends ResourceFolder {
  /**
   * Constructor.
   * @param name name
   * @param path path
   * @param tree tree reference
   * @param context database context
   */
  public ResourceRootFolder(final byte[] name, final byte[] path, final BaseXTree tree,
      final Context context) {
    super(name, path, tree, context);
  }

  @Override
  byte[] subfolder() {
    return path;
  }
}