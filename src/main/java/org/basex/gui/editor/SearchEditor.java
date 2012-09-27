package org.basex.gui.editor;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.*;

/**
 * This class provides an editor with an attached search panel.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SearchEditor extends BaseXBack {
  /** Search panel. */
  final SearchPanel search;

  /**
   * Constructor.
   * @param gui gui reference
   * @param editor editor
   */
  public SearchEditor(final GUI gui, final Editor editor) {
    this(gui, editor, editor);
  }

  /**
   * Constructor.
   * @param gui gui reference
   * @param center centered component
   * @param editor editor (may be {@code null})
   */
  public SearchEditor(final GUI gui, final JComponent center, final Editor editor) {
    super(Fill.NONE);
    search = new SearchPanel(gui);
    layout(new BorderLayout(0, 2));
    add(center, BorderLayout.CENTER);
    add(search, BorderLayout.SOUTH);
    if(editor != null) search.editor(editor);
  }

  /**
   * Return search component.
   * @return search panel
   */
  public SearchPanel panel() {
    return search;
  }
  /**
   * Sets the search button.
   * @param b button
   * @return self reference
   */
  public SearchEditor button(final BaseXButton b) {
    search.setButton(b);
    return this;
  }
}
