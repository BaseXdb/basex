package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import org.basex.gui.*;

/**
 * Displays a text hint on empty text fields.
 * Inspired by {@code http://tips4java.wordpress.com/2009/11/29/text-prompt/}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class BaseXTextHint extends JLabel implements DocumentListener {
  /** Text field. */
  private final BaseXTextField tf;

  /**
   * Constructor.
   * @param text text
   * @param tf text component
   */
  BaseXTextHint(final String text, final BaseXTextField tf) {
    super(text);
    this.tf = tf;

    setForeground(GUIConstants.gray);
    tf.setLayout(new BorderLayout());
    tf.add(this, BorderLayout.CENTER);
    update();
    SwingUtilities.invokeLater(() -> tf.getDocument().addDocumentListener(this));
  }

  /**
   * Check whether the prompt should be visible or not. The visibility will change on updates to the
   * Document and on focus changes.
   */
  public void update() {
    setVisible(tf.getText().isEmpty());
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
