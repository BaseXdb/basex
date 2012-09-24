package org.basex.gui.layout;

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
  BaseXEditor getEditor();
}
