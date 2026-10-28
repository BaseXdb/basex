package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;

/**
 * Project-specific read-only text area with a selectable text.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXTextArea extends JTextArea {
  /**
   * Constructor.
   * @param win  parent window
   * @param text initial text
   */
  public BaseXTextArea(final BaseXWindow win, final String text) {
    super(text);
    setEditable(false);
    setOpaque(false);
    setBorder(null);
    setFont(UIManager.getFont("Label.font"));
    // tab moves the focus instead of being typed
    setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
    setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    BaseXLayout.addInteraction(this, win);

    new BaseXPopup(this, win.gui(), new GUIPopupCmd(Text.COPY, COPY) {
      @Override
      public void execute() { copy(); }
      @Override
      public boolean enabled(final GUI main) { return BaseXTextArea.this.selected(); }
    }, new GUIPopupCmd(Text.SELECT_ALL, SELECTALL) {
      @Override
      public void execute() { selectAll(); }
    });
  }

  /**
   * Indicates if text is selected.
   * @return result of check
   */
  public boolean selected() {
    return getSelectionStart() != getSelectionEnd();
  }
}
