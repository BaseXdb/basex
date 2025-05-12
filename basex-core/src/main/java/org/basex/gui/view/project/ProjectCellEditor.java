package org.basex.gui.view.project;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Custom tree cell editor. The edited node name will be updated in
 * {@link ProjectNode#setUserObject}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProjectCellEditor extends DefaultTreeCellEditor {
  /**
   * Constructor.
   * @param tr tree
   * @param rend renderer
   */
  ProjectCellEditor(final JTree tr, final DefaultTreeCellRenderer rend) {
    super(tr, rend);
  }

  @Override
  protected void determineOffset(final JTree tr, final Object val, final boolean select,
      final boolean expanded, final boolean leaf, final int row) {

    final int w = editingIcon == null ? 0 : editingIcon.getIconWidth();
    offset = renderer.getIconTextGap() + w - 1;
  }

  @Override
  public Component getTreeCellEditorComponent(final JTree tr, final Object val,
      final boolean selected, final boolean expanded, final boolean leaf, final int row) {

    // choose correct icon
    editingIcon = ProjectCellRenderer.icon(val, expanded);
    // replace label to be edited with file name
    final Object value = val instanceof final ProjectNode node ? node.file.name() : val;
    return super.getTreeCellEditorComponent(tr, value, selected, expanded, leaf, row);
  }

  @Override
  public boolean isCellEditable(final EventObject event) {
    // check if chosen node is not the root node
    return super.isCellEditable(event) && tree.getLastSelectedPathComponent()
        instanceof final TreeNode tn && tn.getParent().getParent() != null;
  }
}
