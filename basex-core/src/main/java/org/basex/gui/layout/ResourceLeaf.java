package org.basex.gui.layout;

import org.basex.data.*;
import org.basex.index.resource.*;

/**
 * JTree node which represents a leaf.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Lukas Kircher
 */
public final class ResourceLeaf extends ResourceNode {
  /** Resource type. */
  public final ResourceType type;
  /** This leaf is a dummy node displaying that node listing is abbreviated. */
  public final boolean abbr;

  /**
   * Constructor.
   * @param name name of content node
   * @param path node path
   * @param type resource type
   * @param abbr abbreviated node listing
   * @param tree tree reference
   * @param data database reference
   */
  public ResourceLeaf(final byte[] name, final byte[] path, final ResourceType type,
      final boolean abbr, final BaseXTree tree, final Data data) {
    super(name, path, tree, data);
    this.type = type;
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
