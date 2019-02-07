package org.basex.gui;

import java.awt.*;
//import java.awt.desktop.*;
import java.lang.reflect.*;

import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;

/**
 * Migration of macOS-specific interface options using capabilities introduced with Java 9.
 *
 * @author BaseX Team 2005-19, BSD License
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

    Desktop d = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

    //tb = Taskbar.isTaskbarSupported() ? Taskbar.getTaskbar() : null;
    Class<?> tbClass = Class.forName("java.awt.Taskbar");
    Object tb = tbClass.getMethod("getTaskbar").invoke(null);

    final Class<?> aboutHandler = Class.forName("java.awt.desktop.AboutHandler");
    final Class<?> prefHandler = Class.forName("java.awt.desktop.PreferencesHandler");
    final Object proxy = Proxy.newProxyInstance(GUIMacOSX9.class.getClassLoader(),
        new Class<?>[] { aboutHandler, prefHandler}, this);
    d.getClass().getDeclaredMethod("setAboutHandler", aboutHandler).invoke(d, proxy);
    d.getClass().getDeclaredMethod("setPreferencesHandler", prefHandler).invoke(d, proxy);

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
    if("handleAbout".equals(method.getName())) new DialogAbout(main);
    if("handlePreferences".equals(method.getName())) DialogPrefs.show(main);
    return null;
  }
}
