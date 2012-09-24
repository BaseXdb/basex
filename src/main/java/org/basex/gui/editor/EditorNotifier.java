package org.basex.gui.editor;

/**
 * This interfaces passes on notifications to text areas.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface EditorNotifier {
  /**
   * Returns the current editor.
   * @return editor
   */
  Editor getEditor();
}
