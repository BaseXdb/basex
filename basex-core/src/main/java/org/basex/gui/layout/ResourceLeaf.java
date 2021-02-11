package org.basex.gui.layout;

import org.basex.core.*;

/**
 * JTree node which represents a leaf (which represents either a raw file
 * or a document).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class ResourceLeaf extends ResourceNode {
  /** Represents raw file (true) or document (false). */
  public final boolean raw;
  /** This leaf is a dummy node displaying that node listing is abbreviated. */
  public final boolean abbr;

  /**
   * Constructor.
   * @param name name of content node
   * @param path node path
   * @param raw represented node is raw file
   * @param abbr abbreviated node listing
   * @param tree tree reference
   * @param context database context
   */
  public ResourceLeaf(final byte[] name, final byte[] path, final boolean raw, final boolean abbr,
      final BaseXTree tree, final Context context) {
    super(name, path, tree, context);
    this.raw = raw;
    this.abbr = abbr;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  void load() {
    // leaves cannot be expanded
  }
}
