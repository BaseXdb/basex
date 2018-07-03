//package org.basex.gui;
//
//import java.awt.*;
//import java.awt.Desktop.*;
//import java.awt.desktop.*;
//
//import org.basex.gui.dialog.*;
//import org.basex.gui.layout.*;
//
///**
// * Migration of macOS-specific interface options using capabilities introduced with Java 9.
// *
// * @author BaseX Team 2018, BSD License
// * @author Alexander Holupirek
// */
//public final class GUIMacOSX9 extends GUIMacOS implements AboutHandler {
//
//  /** Desktop reference, which allows to interact with various desktop capabilities. */
//  private Desktop d;
//  /** Taskbar reference to interact with the system task area (taskbar, Dock, etc.). */
//  private Taskbar tb;
//
//  /**
//   * Constructor.
//   */
//  public GUIMacOSX9() {
//    d = (Desktop.isDesktopSupported()) ? Desktop.getDesktop() : null;
//    tb = (Taskbar.isTaskbarSupported()) ? Taskbar.getTaskbar() : null;
//  }
//
//  @Override
//  public void init(final GUI gui) {
//    main = gui;
//    if (tb != null) {
//      initDockIcon();
//    }
//    if (d != null) {
//      initAboutMenu();
//    }
//  }
//
//  @Override
//  public void adjustMenuBar(GUI gui, GUIMenu menu)
//  {
//    if(d != null && d.isSupported(Desktop.Action.APP_MENU_BAR))
//      d.setDefaultMenuBar(menu);
//  }
//
//  @Override
//  public void setBadge(final String value) {
//    if (tb != null && tb.isSupported(Taskbar.Feature.ICON_BADGE_TEXT))
//      tb.setIconBadge(value);
//  }
//
//  @Override
//  public void enableOSXFullscreen(Window window) {
//    /** Nothing to be done using Java9 and greater. */
//  }
//
//  /**
//   * Displays the project icon in the dock and task bar.
//   */
//  private void initDockIcon() {
//    if (tb.isSupported(Taskbar.Feature.ICON_IMAGE))
//      tb.setIconImage(BaseXImages.get("logo_256"));
//  }
//
//  /**
//   * Registers object to react on 'about' action.
//   */
//  private void initAboutMenu() {
//    if (d.isSupported(Action.APP_ABOUT))
//      d.setAboutHandler(this);
//  }
//
//  /**
//   * Shows about dialog window.
//   *
//   * @param e about event received
//   */
//  @Override
//  public void handleAbout(final AboutEvent e) {
//    new DialogAbout(main);
//  }
//}
