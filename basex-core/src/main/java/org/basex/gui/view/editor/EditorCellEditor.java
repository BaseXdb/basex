package org.basex.gui.view.editor;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Custom tree cell editor. The edited node name will be updated in
 * {@link EditorNode#setUserObject}.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class EditorCellEditor extends DefaultTreeCellEditor {
  /**
   * Constructor.
   * @param tr tree
   * @param rend renderer
   */
  public EditorCellEditor(final JTree tr, final DefaultTreeCellRenderer rend) {
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
    editingIcon = EditorCellRenderer.icon(val, expanded);
    // replace label to be edited with file name
    final Object value = val instanceof EditorNode ? ((EditorNode) val).file.name() : val;
    return super.getTreeCellEditorComponent(tr, value, selected, expanded, leaf, row);
  }

  @Override
  public boolean isCellEditable(final EventObject event) {
    // check if chosen node is not the root node
    final Object node = tree.getLastSelectedPathComponent();
    return super.isCellEditable(event) && node instanceof TreeNode &&
        ((TreeNode) node).getParent().getParent() != null;
  }
}
