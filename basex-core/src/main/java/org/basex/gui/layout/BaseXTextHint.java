package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.basex.gui.*;

/**
 * Displays a text hint on empty text fields.
 * Inspired by {@code http://tips4java.wordpress.com/2009/11/29/text-prompt/}.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class BaseXTextHint extends JLabel implements DocumentListener {
  /** Text component. */
  private final JTextComponent component;

  /**
   * Constructor.
   * @param text text
   * @param comp text component
   */
  public BaseXTextHint(final String text, final JTextComponent comp) {
    super(text);
    component = comp;

    setForeground(GUIConstants.GRAY);
    setBorder(new EmptyBorder(comp.getInsets()));
    setFont(comp.getFont());

    comp.getDocument().addDocumentListener(this);
    comp.setLayout(new BorderLayout());
    comp.add(this);
    update();
  }

  /**
   * Check whether the prompt should be visible or not. The visibility will change on updates to the
   * Document and on focus changes.
   */
  private void update() {
    setVisible(component.getText().isEmpty());
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    update();
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    update();
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
  }
}
