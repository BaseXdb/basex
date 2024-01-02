package org.basex.gui.text;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * This class displays editor components with an integrated search bar.
 *
 * @author BaseX Team 2005-24, BSD License
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
   * @param editor editor (can be {@code null})
   */
  public SearchEditor(final GUI gui, final JComponent center, final TextPanel editor) {
    super(false);
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
   * Returns a search button.
   * @return button
   */
  public AbstractButton button() {
    return search.button(Text.FIND);
  }
}
