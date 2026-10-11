package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.layout.*;

/**
 * Popup window with the signature of the function that is currently called.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SignaturePopup extends TextPopup {
  /** Displayed signature. */
  private final BaseXBack content = new BaseXBack().layout(new ColumnLayout());
  /** Position of the popup, relative to the text panel (can be {@code null}). */
  private Point point;

  /**
   * Constructor.
   * @param panel text panel
   */
  SignaturePopup(final TextPanel panel) {
    super(panel);
    content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(lightGray),
      BaseXLayout.border(2, MARGIN, 2, MARGIN)));
    content.setBackground(backColor);
  }

  @Override
  void timeout() {
    open(point);
  }

  /**
   * Shows a signature below the current line.
   * @param signature signature
   * @param name function name, as it was specified in the call
   * @param index index of the argument at the caret ({@code -1}: no argument is emphasized)
   * @param pnt position of the popup, relative to the text panel
   */
  void show(final Signature signature, final String name, final int index, final Point pnt) {
    point = pnt;
    content.removeAll();
    // the name of the argument at the caret is emphasized
    final String args = signature.args();
    final int al = args.length();
    final int s = index == -1 ? al : signature.starts()[index];
    final int e = index == -1 ? al : signature.ends()[index];
    add(name + args.substring(0, s), false);
    add(args.substring(s, e), true);
    add(args.substring(e), false);

    // a visible popup is updated at once, a new one is displayed after a delay
    if(visible()) open(point);
    else schedule();
  }

  /**
   * Adds a part of the signature.
   * @param string string
   * @param bold emphasize the string
   */
  private void add(final String string, final boolean bold) {
    if(string.isEmpty()) return;
    // the signature is rendered like the candidates of the code completion
    final JLabel label = new JLabel(string);
    final Font f = label.getFont().deriveFont((float) popupFontSize);
    label.setFont(bold ? f.deriveFont(Font.BOLD) : f);
    label.setForeground(textColor);
    content.add(label);
  }

  @Override
  Component content() {
    return content;
  }
}
