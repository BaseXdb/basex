package org.basex.gui;

import java.awt.*;
import java.lang.reflect.*;

import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Sets some Mac OS X specific interface options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Bastian Lemke
 * @author Alexander Holupirek
 */
public final class GUIMacOSX extends GUIMacOS {
  /** Native class name. */
  private static final String C_APPLICATION = "com.apple.eawt.Application";
  /** Native class name. */
  private static final String C_APPLICATION_LISTENER = "com.apple.eawt.ApplicationListener";
  /** Native class name. */
  private static final String C_APPLICATION_EVENT = "com.apple.eawt.ApplicationEvent";

  /** System property identifier. */
  private static final String P_ABOUT_NAME = "com.apple.mrj.application.apple.menu.about.name";

  /** Empty class array. */
  private static final Class<?>[] EC = {};
  /** Empty object array. */
  private static final Object[] EO = {};

  /** Instance of the 'Application' class. */
  private final Object appObj;

  /**
   * Constructor.
   * @param main reference to main window
   * @throws Exception if any error occurs
   */
  GUIMacOSX(final GUI main) throws Exception {
    super(main);

    // name for the dock icon and the application menu
    System.setProperty(P_ABOUT_NAME, Prop.NAME);

    // load native java classes...
    /* Reference to the loaded 'Application' class. */
    final Class<?> appClass = Class.forName(C_APPLICATION);
    appObj = invoke(appClass, null, "getApplication", EC, EO);
    Class.forName(C_APPLICATION_EVENT);

    if(appObj != null) {
      invoke(appObj, "addAboutMenuItem");
      invoke(appObj, "setEnabledAboutMenu", true);
      invoke(appObj, "addPreferencesMenuItem");
      invoke(appObj, "setEnabledPreferencesMenu", true);

      addDockIcon();

      final Class<?> alc = Class.forName(C_APPLICATION_LISTENER);
      final Object listener = Proxy.newProxyInstance(
          Thread.currentThread().getContextClassLoader(),
          new Class[] { alc }, new AppInvocationHandler());
      invoke(appObj, "addApplicationListener", alc, listener);
    }
  }

  /**
   * Adds the project icon to the dock.
   * @throws Exception if any error occurs.
   */
  private void addDockIcon() throws Exception {
    invoke(appObj, "setDockIconImage", Image.class, BaseXImages.get("logo_256"));
  }

  /**
   * Handler for the native application events.
   * @author Bastian Lemke
   */
  class AppInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
      final Object obj = args[0];
      /*
       * Get the name of the method and call the method of this object that has
       * the same name with only the first argument.
       * This emulates the an implementation of the native 'ApplicationListener'
       * interface (@see com.apple.eawt.ApplicationListener on Mac OS X).
       * The argument is an instance of the 'ApplicationEvent' class
       * (@see com.apple.eawt.ApplicationEvent)
       */
      Object result;
      try {
        result = GUIMacOSX.invoke(this, method.getName(), Object.class, obj);
      } catch(final NoSuchMethodException ex) {
        Util.debug(ex);
        result = GUIMacOSX.invoke(this, method.getName());
      }
      // mark the current event as 'handled' if handler doesn't return a false boolean
      GUIMacOSX.invoke(obj, "setHandled", result instanceof Boolean ? (Boolean) result : true);
      return null;
    }

    /** Called when the user selects the About item in the application menu. */
    public void handleAbout() {
      // explicit cast to circumvent Java compiler bug
      GUIMenuCmd.C_ABOUT.execute(main);
    }

    /**
     * Called when the application receives an Open Application event from the
     * Finder or another application.
     */
    public void handleOpenApplication()  { /* NOT IMPLEMENTED */ }

    /**
     * Called when the application receives an Open Application event from the
     * Finder or another application.
     *
     * @param obj application event
     */
    @SuppressWarnings("unused")
    public void handleOpenApplication(final Object obj)  { /* NOT IMPLEMENTED */ }

    /**
     * Called when the application receives an Open Document event from the
     * Finder or another application.
     * @param obj application event
     */
    @SuppressWarnings("unused")
    public void handleOpenFile(final Object obj) {
      // get the associated filename:
      // final String name = (String) GUIMacOSX.invoke(obj, "getFilename");
    }

    /** Called when the Preference item in the application menu is selected. */
    public void handlePreferences() {
      // explicit cast to circumvent Java compiler bug
      GUIMenuCmd.C_PREFS.execute(main);
    }

    /**
     * Called when the application is sent a request to print a particular file
     * or files.
     */
    public void handlePrintFile()  { /* NOT IMPLEMENTED */ }

    /**
     * Called when the application is sent the Quit event.
     *
     * Unlike other handles, quit mustn't set {@code setHandled} or OS X will quit the
     * application.
     *
     * @return always false
     */
    public boolean handleQuit() {
      GUIMenuCmd.C_EXIT.execute(main);
      return false;
    }

    /**
     * Called when the application receives a Reopen Application event from the
     * Finder or another application.
     */
    public void handleReOpenApplication() {
      if(main != null) main.setVisible(true);
    }
  }

  /**
   * Invokes a method without arguments on the given object.
   * @param obj object on which the method should be invoked
   * @param method name of the method to invoke
   * @return return value of the method
   * @throws Exception if any error occurs.
   */
  static Object invoke(final Object obj, final String method) throws Exception {
    return invoke(obj.getClass(), obj, method, EC, EO);
  }

  /**
   * Invokes a method on the given object that expects a single boolean value as
   * argument.
   * @param obj object on which the method should be invoked
   * @param method name of the method to invoke
   * @param arg boolean value that is passed as argument
   * @return return value of the method
   * @throws Exception if any error occurs.
   */
  static Object invoke(final Object obj, final String method, final boolean arg)
      throws Exception {
    return invoke(obj, method, Boolean.TYPE, arg);
  }

  /**
   * Invokes a method on the given object that expects a single argument.
   * @param obj object on which the method should be invoked
   * @param method name of the method to invoke
   * @param argClass "type" of the argument
   * @param argObject argument value
   * @return return value of the method
   * @throws Exception if any error occurs
   */
  static Object invoke(final Object obj, final String method, final Class<?> argClass,
      final Object argObject) throws Exception {
    final Class<?>[] argClasses = { argClass };
    final Object[] argObjects = { argObject };
    return invoke(obj.getClass(), obj, method, argClasses, argObjects);
  }

  /**
   * Invokes a method on the given object that expects multiple arguments.
   * @param clazz class object to get the method from
   * @param obj object on which the method should be invoked ({@code null} for static methods)
   * @param method name of the method to invoke
   * @param argClasses "types" of the arguments
   * @param argObjects argument values
   * @return return value of the method
   * @throws Exception if any error occurs
   */
  private static Object invoke(final Class<?> clazz, final Object obj, final String method,
      final Class<?>[] argClasses, final Object[] argObjects) throws Exception {
    return clazz.getMethod(method, argClasses).invoke(obj, argObjects);
  }
}
