package org.basex.gui;

import java.awt.*;
import java.lang.reflect.*;

import org.basex.gui.layout.*;

/**
 * Migration of macOS-specific interface options using capabilities introduced with Java 9.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Alexander Holupirek
 */
//public final class GUIMacOSX9 extends GUIMacOS implements AboutHandler {
public final class GUIMacOSX9 extends GUIMacOS implements InvocationHandler {

  /**
   * Constructor.
   * @param main reference to main window
   * @throws Exception if any error occurs
   */
  GUIMacOSX9(final GUI main) throws Exception {
    super(main);

    final Class<?> aboutHandler = Class.forName("java.awt.desktop.AboutHandler");
    final Class<?> prefHandler = Class.forName("java.awt.desktop.PreferencesHandler");
    final Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class<?>[] { aboutHandler, prefHandler }, this);

    final Desktop d = Desktop.getDesktop();
    final Class<?> dc = d.getClass();
    dc.getDeclaredMethod("setAboutHandler", aboutHandler).invoke(d, proxy);
    dc.getDeclaredMethod("setPreferencesHandler", prefHandler).invoke(d, proxy);

    //tb = Taskbar.isTaskbarSupported() ? Taskbar.getTaskbar() : null;
    final Class<?> tbClass = Class.forName("java.awt.Taskbar");
    final Object tb = tbClass.getMethod("getTaskbar").invoke(null);
    tbClass.getMethod("setIconImage", Image.class).invoke(tb, BaseXImages.get("logo_256"));
  }

  ///**
  // * Shows about dialog window.
  // *
  // * @param e about event received
  // */
  //@Override
  //public void handleAbout(final AboutEvent e) {
  //  new DialogAbout(main);
  //}
  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) {
    final String name = method.getName();
    if(name.equals("handleAbout")) GUIMenuCmd.C_ABOUT.execute(main);
    if(name.equals("handlePreferences")) GUIMenuCmd.C_PREFS.execute(main);
    return null;
  }
}
