package org.basex.gui.text;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.*;

/**
 * This class displays editor components with an integrated search bar.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class SearchEditor extends BaseXBack {
  /** Search bar. */
  private final SearchBar search;

  /**
   * Constructor.
   * @param gui gui reference
   * @param editor editor
   */
  public SearchEditor(final GUI gui, final TextPanel editor) {
    this(gui, editor, editor);
  }

  /**
   * Constructor.
   * @param gui gui reference
   * @param center centered component
   * @param editor editor (may be {@code null})
   */
  public SearchEditor(final GUI gui, final JComponent center, final TextPanel editor) {
    super(Fill.NONE);
    search = new SearchBar(gui);
    layout(new BorderLayout(0, 2));
    add(center, BorderLayout.CENTER);
    add(search, BorderLayout.SOUTH);
    if(editor != null) search.editor(editor, false);
  }

  /**
   * Return search bar.
   * @return search bar
   */
  public SearchBar bar() {
    return search;
  }

  /**
   * Sets the search button.
   * @param help help text
   * @return self reference
   */
  public AbstractButton button(final String help) {
    return search.button(help);
  }
}
