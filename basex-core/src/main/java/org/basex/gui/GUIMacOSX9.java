package org.basex.gui;

import java.awt.*;
import java.awt.Desktop.*;
//import java.awt.desktop.*;

import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

import java.lang.reflect.*;

/**
 * Migration of macOS-specific interface options using capabilities introduced with Java 9.
 *
 * @author BaseX Team 2018, BSD License
 * @author Alexander Holupirek
 */
//public final class GUIMacOSX9 extends GUIMacOS implements AboutHandler {
public final class GUIMacOSX9 extends GUIMacOS implements InvocationHandler {

  /** Desktop reference, which allows to interact with various desktop capabilities. */
//  private Desktop d;
  private Class<?> dClass;
  private Object d;
  /** Taskbar reference to interact with the system task area (taskbar, Dock, etc.). */
//  private Taskbar tb;
  private Class<?> tbClass;
  private Object tb;

  /**
   * Constructor.
   * @throws Exception if any error occurs.
   */
  public GUIMacOSX9() throws Exception {
//    d = (Desktop.isDesktopSupported()) ? Desktop.getDesktop() : null;
    dClass = Class.forName("java.awt.Desktop");
    d = dClass.getMethod("getDesktop").invoke(null);

//    tb = (Taskbar.isTaskbarSupported()) ? Taskbar.getTaskbar() : null;
    tbClass = Class.forName("java.awt.Taskbar");
    tb = tbClass.getMethod("getTaskbar").invoke(null);

    Class<?> aboutHandler = Class.forName("java.awt.desktop.AboutHandler");
    Object proxy = Proxy.newProxyInstance(GUIMacOSX9.class.getClassLoader(), new Class<?>[] {aboutHandler}, this);
    dClass.getDeclaredMethod("setAboutHandler", aboutHandler).invoke(d, proxy);
  }

  @Override
  public void init(final GUI gui) {
    try {
    main = gui;
    if (tb != null) {
      initDockIcon();
    }
    if (d != null) {
      initAboutMenu();
    }
    } catch(Exception e) {
      Util.debug(e);
    }
  }

  @Override
  public void adjustMenuBar(GUI gui, GUIMenu menu)
  {
//    if(d != null && d.isSupported(Desktop.Action.APP_MENU_BAR))
//      d.setDefaultMenuBar(menu);
  }

  @Override
  public void setBadge(final String value) {
//    if (tb != null && tb.isSupported(Taskbar.Feature.ICON_BADGE_TEXT))
//      tb.setIconBadge(value);
  }

  @Override
  public void enableOSXFullscreen(Window window) {
    /** Nothing to be done using Java9 and greater. */
  }

  /**
   * Displays the project icon in the dock and task bar.
   * @throws Exception if any error occurs
   */
  private void initDockIcon() throws Exception {
//    if (tb.isSupported(Taskbar.Feature.ICON_IMAGE))
//      tb.setIconImage(BaseXImages.get("logo_256"));
    Class<?>[] PARAMS = { Class.forName("java.awt.Image") };
    Method m = tbClass.getMethod("setIconImage", PARAMS);
    m.invoke(tb, BaseXImages.get("logo_256"));
  }

  /**
   * Registers object to react on 'about' action.
   * @throws Exception if any error occurs
   */
  private void initAboutMenu() throws Exception {
//    if (d.isSupported(Action.APP_ABOUT))
//      d.setAboutHandler(this);
    Method m = dClass.getMethod("setAboutHandler", new Class<?>[] { this.getClass() });
    m.invoke(d, this);
  }

//  /**
//   * Shows about dialog window.
//   *
//   * @param e about event received
//   */
//  @Override
//  public void handleAbout(final AboutEvent e) {
//    new DialogAbout(main);
//  }

@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if ("handleAbout".equals(method.getName())) {
        System.err.println("handleAbout");
    }
    return null;
  }
}
