package org.basex.gui.layout;

import org.basex.core.*;

/**
 * JTree node which represents a leaf.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Lukas Kircher
 */
public final class ResourceLeaf extends ResourceNode {
  /** Represents binary resource or document. */
  public final boolean binary;
  /** This leaf is a dummy node displaying that node listing is abbreviated. */
  public final boolean abbr;

  /**
   * Constructor.
   * @param name name of content node
   * @param path node path
   * @param binary represented node is binary
   * @param abbr abbreviated node listing
   * @param tree tree reference
   * @param context database context
   */
  public ResourceLeaf(final byte[] name, final byte[] path, final boolean binary,
      final boolean abbr, final BaseXTree tree, final Context context) {
    super(name, path, tree, context);
    this.binary = binary;
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
