package org.basex.gui;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.layout.*;

/**
 * Abstract class for GUI windows.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class AGUI extends JFrame {
  /** Database Context. */
  public final Context context;
  /** GUI options. */
  public final GUIOptions gopts;

  /**
   * Default constructor.
   * @param ctx database context
   * @param opts gui options
   */
  AGUI(final Context ctx, final GUIOptions opts) {
    setIconImage(BaseXImages.get("icon"));
    setTitle(null);
    context = ctx;
    gopts = opts;
  }

  @Override
  public final void setTitle(final String title) {
    final String t = title == null || title.isEmpty() ? "" : " - " + title;
    super.setTitle(Text.TITLE + t);
  }

  /**
   * Sets a cursor.
   * @param c cursor to be set
   */
  public final void cursor(final Cursor c) {
    cursor(c, false);
  }

  /**
   * Sets a cursor, forcing a new look if necessary.
   * @param c cursor to be set
   * @param force new cursor
   */
  public final void cursor(final Cursor c, final boolean force) {
    final Cursor cc = getCursor();
    if(cc != c && (cc != CURSORWAIT || force)) setCursor(c);
  }
}
