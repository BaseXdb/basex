package org.basex.gui.server;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.basex.core.BaseXException;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.ShowUsers;
import org.basex.core.cmd.XQuery;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTable;
import org.basex.util.Table;

/**
 * Panels on the right side of the main window.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class Tab extends BaseXBack {
  /** GUI reference. */
  private final SGUI gui;

  /**
   * Default constructor.
   * @param t type
   * @param g gui reference
   * @throws BaseXException database exception
   */
  public Tab(final String t, final SGUI g) throws BaseXException {
    gui = g;
    mode(Fill.NONE);
    final String dbname = t.substring(0, t.indexOf("-") - 1);
    if(t.endsWith("Users")) {
      initUser(dbname);
    } else if(t.endsWith("Content")) {
      initContent(dbname);
    } else if(t.endsWith("Properties")) {
      initProps(dbname);
    }
  }

  /**
   * Initializes the user tab.
   * @param t database name
   * @throws BaseXException database exception
   */
  private void initUser(final String t) throws BaseXException {
    final Table users = new Table(gui.client.execute(new ShowUsers(t)));
    // [AW] remove Dialog reference from BaseXTable
    final BaseXTable table = new BaseXTable(users, null);
    final JScrollPane sp = new JScrollPane(table);
    BaseXLayout.setHeight(sp, 220);
    BaseXLayout.setWidth(sp, 350);
    add(sp);
  }

  /**
   * Initializes the content tab.
   * @param t database name
   * @throws BaseXException database exception
   */
  private void initContent(final String t) throws BaseXException {
    gui.client.execute(new Open(t));
    // [AW] change to BaseXText
    final JTextArea jt = new JTextArea(gui.client.execute(new XQuery("//*")));
    final JScrollPane sp = new JScrollPane(jt);
    BaseXLayout.setHeight(sp, 420);
    BaseXLayout.setWidth(sp, 550);
    add(sp);
  }

  /**
   * Initializes the properties tab.
   * @param t database name
   * @throws BaseXException database exception
   */
  private void initProps(final String t) throws BaseXException {
    gui.client.execute(new Open(t));
    // [AW] change to BaseXText
    final JTextArea jt = new JTextArea(gui.client.execute(new InfoDB()));
    final JScrollPane sp = new JScrollPane(jt);
    BaseXLayout.setHeight(sp, 320);
    BaseXLayout.setWidth(sp, 450);
    add(sp);
  }
}
