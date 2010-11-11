package org.basex.gui;

import static org.basex.gui.GUIConstants.*;
import java.awt.Cursor;
import javax.swing.JFrame;
import org.basex.core.Context;
import org.basex.gui.layout.BaseXLayout;

/**
 * Abstract class for GUI windows.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class AGUI extends JFrame {
  /** Database Context. */
  public final Context context;
  /** GUI properties. */
  public final GUIProp gprop;

  /**
   * Default constructor.
   * @param ctx context reference
   * @param gprops gui properties
   * @param title window title
   */
  protected AGUI(final Context ctx, final GUIProp gprops, final String title) {
    setIconImage(BaseXLayout.image("icon"));
    setTitle(title);
    context = ctx;
    gprop = gprops;
  }

  /**
   * Sets a cursor.
   * @param c cursor to be set
   */
  public void cursor(final Cursor c) {
    cursor(c, false);
  }

  /**
   * Sets a cursor, forcing a new look if necessary.
   * @param c cursor to be set
   * @param force new cursor
   */
  public void cursor(final Cursor c, final boolean force) {
    final Cursor cc = getCursor();
    if(cc != c && (cc != CURSORWAIT || force)) setCursor(c);
  }
}
