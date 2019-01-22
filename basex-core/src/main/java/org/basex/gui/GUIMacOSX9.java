package org.basex.gui;

import java.awt.*;
//import java.awt.desktop.*;
import java.lang.reflect.*;

import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Migration of macOS-specific interface options using capabilities introduced with Java 9.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Alexander Holupirek
 */
//public final class GUIMacOSX9 extends GUIMacOS implements AboutHandler {
public final class GUIMacOSX9 extends GUIMacOS implements InvocationHandler {
  /** Desktop reference, which allows to interact with various desktop capabilities. */
  //private Desktop d;
  private final Class<?> dClass;
  /** Desktop reference. */
  private final Object d;
  /** Taskbar reference to interact with the system task area (taskbar, Dock, etc.). */
  //private Taskbar tb;
  private final Class<?> tbClass;
  /** Taskbar reference. */
  private final Object tb;

  /**
   * Constructor.
   * @param main reference to main window
   * @throws Exception if any error occurs
   */
  GUIMacOSX9(final GUI main) throws Exception {
    super(main);

    //d = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    dClass = Class.forName("java.awt.Desktop");
    d = dClass.getMethod("getDesktop").invoke(null);

    //tb = Taskbar.isTaskbarSupported() ? Taskbar.getTaskbar() : null;
    tbClass = Class.forName("java.awt.Taskbar");
    tb = tbClass.getMethod("getTaskbar").invoke(null);

    final Class<?> aboutHandler = Class.forName("java.awt.desktop.AboutHandler");
    final Object proxy = Proxy.newProxyInstance(GUIMacOSX9.class.getClassLoader(),
        new Class<?>[] { aboutHandler}, this);
    dClass.getDeclaredMethod("setAboutHandler", aboutHandler).invoke(d, proxy);
  }

  @Override
  public void init() {
    try {
      if(tb != null) initDockIcon();
      if(d != null) initAboutMenu();
    } catch(final Exception ex) {
      Util.debug(ex);
    }
  }

  @Override
  public void setBadge(final String value) {
    //if(tb != null && tb.isSupported(Taskbar.Feature.ICON_BADGE_TEXT)) tb.setIconBadge(value);
  }

  @Override
  public void enableOSXFullscreen(final Window window) {
    // Nothing to be done using Java9 and greater
  }

  /**
   * Displays the project icon in the dock and task bar.
   * @throws Exception if any error occurs
   */
  private void initDockIcon() throws Exception {
    //if(tb.isSupported(Taskbar.Feature.ICON_IMAGE)) tb.setIconImage(BaseXImages.get("logo_256"));
    final Class<?>[] params = { Class.forName("java.awt.Image")};
    final Method m = tbClass.getMethod("setIconImage", params);
    m.invoke(tb, BaseXImages.get("logo_256"));
  }

  /**
   * Registers object to react on 'about' action.
   * @throws Exception if any error occurs
   */
  private void initAboutMenu() throws Exception {
    //if(d.isSupported(Action.APP_ABOUT)) d.setAboutHandler(this);
    final Method m = dClass.getMethod("setAboutHandler", this.getClass());
    m.invoke(d, this);
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
    return null;
  }
}
