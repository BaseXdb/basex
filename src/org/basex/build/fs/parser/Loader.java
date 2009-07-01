/**
 * 
 */
package org.basex.build.fs.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.basex.BaseX;

/**
 * Some utility methods for loading class files from folders, packages or jar
 * files.
 * @author Bastian Lemke
 */
public final class Loader extends ClassLoader {

  // [BL] method for loading all subclasses of an abstract class or interface
  /*
   *  load(Package pkg, Class c) {
   *    ...
   *    c.isInstance(Class.forName(classname).newInstance());
   *    ...
   *  }
   *  
   *  Also possible without instantiating the class?
   */
  
  /** The ClassLoader instance to use for loading classes. */
  private static final Loader INSTANCE = new Loader();

  /** Hidden constructor. */
  private Loader() { /* */}

  /**
   * <p>
   * Load some classes from a package that is on the classpath. The classes may
   * either be in a directory or inside a java archive file. If one of the
   * classes is already loaded, nothing is done (but the class will be included
   * in the results).
   * </p>
   * <p>
   * This method breaks after the first error. Subsequent classes are not
   * loaded.
   * </p>
   * @param pkg the package to load the classes from.
   * @param fileNamePattern the pattern used for matching the class names.
   * @return an array with all the loaded classes.
   * @throws IOException if the classes are located inside a JAR file and any
   *           error occurs while reading from this file.
   */
  public static Class<?>[] load(final Package pkg, //
      final Pattern fileNamePattern) throws IOException {
    ArrayList<Class<?>> loadedClasses = new ArrayList<Class<?>>();
    try {
      String pkgName = pkg.getName();
      String pkgPath = pkgName.replace('.', '/');
      if(!pkgPath.startsWith("/")) pkgPath = "/" + pkgPath;
      Matcher matcher;
      URL pkgUrl = Loader.class.getResource(pkgPath);
      if(pkgUrl == null) return new Class<?>[0];
      File packageDir = new File(pkgUrl.getFile());
      if(packageDir.exists()) { // package located on disk (as directory)
        for(File f : packageDir.listFiles()) {
          String fileName = f.getName();
          if(!fileName.endsWith(".class")) continue;
          fileName = fileName.substring(0, fileName.length() - 6);
          matcher = fileNamePattern.matcher(fileName);
          if(matcher.matches()) {
            String clazz = pkgName + "." + fileName;
            loadedClasses.add(Class.forName(clazz));
          }
        }
      } else { // package is inside a JAR file
        JarURLConnection conn = (JarURLConnection) pkgUrl.openConnection();
        JarFile jfile = conn.getJarFile();
        String starts = conn.getEntryName();
        Enumeration<JarEntry> e = jfile.entries();
        while(e.hasMoreElements()) {
          // [BL] avoid sequential scan of ALL jar entries
          JarEntry entry = e.nextElement();
          String name = entry.getName();
          if(name.startsWith(starts) && //
              name.lastIndexOf('/') <= starts.length() && // skip sub-pkgs
              name.endsWith(".class")) {
            String classname = name.substring(0, name.length() - 6);
            int i = classname.lastIndexOf('/') + 1;
            String shortName = classname.substring(i);
            matcher = fileNamePattern.matcher(shortName);
            if(matcher.matches()) {
              if(classname.startsWith("/")) classname = classname.substring(1);
              classname = classname.replace('/', '.');
              loadedClasses.add(Class.forName(classname));
            }
          }
        }

      }
    } catch(IOException e) {
      throw e;
    } catch(Throwable t) {
      // catch all exceptions and JVM errors and break after the first error.
      BaseX.errln("Failed to load class (%)", t.getMessage());
    }
    return loadedClasses.toArray(new Class<?>[loadedClasses.size()]);
  }

  /**
   * <p>
   * Load some classes from the given directory that is not on the classpath. If
   * one of the classes is already loaded, nothing is done (but the class will
   * be included in the results). This method breaks after the first error.
   * Subsequent classes are not loaded.
   * </p>
   * <p>
   * There may be restrictions for the usage of this classes (e.g. if they
   * extend an abstract class that was loaded before with a different
   * {@link ClassLoader}). Classes that were loaded with a different
   * {@link ClassLoader} may not be able to access fields or methods from these
   * classes.
   * </p>
   * <p>
   * <b> Whenever possible, use {@link #load(Package, Pattern)} instead of this
   * method to avoid problems. </b> Only use this method if the classes are not
   * on the classpath.
   * </p>
   * @param directory the directory to load the classes from.
   * @param fileNamePattern the pattern used for matching the class names. All
   *          inner (sub)subclasses that lives in extra class files (e.g.
   *          MP3Parser$Frame and MP3Parser$Frame$1) must be included in the
   *          pattern.
   * @return an array with all the loaded classes.
   * @throws IOException if any error occurs while reading from a file.
   */
  public static Class<?>[] load(final File directory,
      final Pattern fileNamePattern) throws IOException {
    Matcher matcher;
    ArrayList<Class<?>> foundClasses = new ArrayList<Class<?>>();
    ArrayList<File> subClasses = new ArrayList<File>();
    ArrayList<File> subSubClasses = new ArrayList<File>();
    try {
      if(!directory.isDirectory()) throw new IllegalArgumentException(
          "Is not a directory.");
      for(File f : directory.listFiles()) {
        String fileName = f.getName();
        fileName = fileName.substring(0, fileName.length() - 6);
        matcher = fileNamePattern.matcher(fileName);
        // [BL] detect (sub)subclasses (not matched by the pattern)
        if(matcher.matches()) {
          // hack to detect (sub)subclasses that must be loaded after the
          // classes
          if(fileName.contains("$")) {
            if(fileName.indexOf('$') != fileName.lastIndexOf('$')) {
              subSubClasses.add(f);
              continue;
            }
            subClasses.add(f);
            continue;
          }
          Class<?> c = load(f);
          foundClasses.add(c);
        }
      }
      // load subclasses
      for(File f : subClasses)
        load(f);
      // load subsubclasses
      for(File f : subSubClasses)
        load(f);
    } catch(IOException e) {
      throw e;
    } catch(Throwable t) {
      // catch all exceptions and JVM errors and break after the first error.
      BaseX.errln("Failed to load class (%)", t.getMessage());
    }
    int counter = initializeClasses(foundClasses);
    // return only the correctly initialized classes
    return foundClasses.subList(0, counter).toArray(new Class<?>[counter]);
  }

  /**
   * <p>
   * Load some classes from the given jar file. If one of the classes is already
   * loaded, nothing is done (but the class will be included in the results).
   * This method breaks after the first error. Subsequent classes are not
   * loaded.
   * </p>
   * <p>
   * There may be restrictions for the usage of this classes (e.g. if they
   * extend an abstract class that was loaded before with a different
   * {@link ClassLoader}). Classes that were loaded with a different
   * {@link ClassLoader} may not be able to access fields or methods from these
   * classes!
   * </p>
   * <p>
   * <b>Whenever possible, use {@link #load(Package, Pattern)} instead of this
   * method to avoid problems.</b> Only use this method if the classes are not
   * on the classpath.
   * </p>
   * @param jar the {@link JarFile} to load the classes from.
   * @return an array with all the loaded classes.
   * @throws IOException if one of the classes could not be read.
   */
  public static Class<?>[] load(final JarFile jar) throws IOException {
    Enumeration<JarEntry> e = jar.entries();
    ArrayList<Class<?>> foundClasses = new ArrayList<Class<?>>();
    ArrayList<JarEntry> subClasses = new ArrayList<JarEntry>();
    ArrayList<JarEntry> subSubClasses = new ArrayList<JarEntry>();
    try {
      while(e.hasMoreElements()) {
        JarEntry entry = e.nextElement();
        String name = entry.getName();
        if(entry.isDirectory() || name.endsWith("MANIFEST.MF")) {
          continue;
        }
        // hack to detect (sub)subclasses that must be loaded after the classes
        if(name.contains("$")) {
          if(name.indexOf("$") != name.lastIndexOf("$")) {
            subSubClasses.add(entry);
            continue;
          }
          subClasses.add(entry);
          continue;
        }
        Class<?> c = load(jar, entry);
        foundClasses.add(c);
      }
      // load subclasses
      for(JarEntry entry : subClasses)
        load(jar, entry);
      // load subsubclasses
      for(JarEntry entry : subSubClasses)
        load(jar, entry);
    } catch(IOException ex) {
      throw ex;
    } catch(Throwable t) { // catch all exceptions an JVM errors
      BaseX.errln("Failed to load class (%)", t.getMessage());
    }
    int counter = initializeClasses(foundClasses);
    jar.close();
    // return only the correctly initialized classes
    return foundClasses.subList(0, counter).toArray(new Class<?>[counter]);
  }

  /**
   * Reads a single class from the file.
   * @param f the file to read from.
   * @return the (uninitialized) class.
   * @throws IOException if any error occurs while reading from the file.
   */
  private static Class<?> load(final File f) throws IOException {
    long len = f.length();
    if(len > Integer.MAX_VALUE) throw new IOException(
        "Class file too long to load.");
    byte[] buf = new byte[(int) len];
    FileChannel ch = new RandomAccessFile(f, "r").getChannel();
    ch.read(ByteBuffer.wrap(buf));
    ch.close();
    return INSTANCE.defineClass(null, buf, 0, buf.length);
  }

  /**
   * Reads a single class from the jar file.
   * @param jar the jar file to read from.
   * @param je the entry to read from the jar file.
   * @return the (uninitialized) class.
   * @throws IOException if any error occurs while reading from the file.
   */
  private static Class<?> load(final JarFile jar, final JarEntry je)
      throws IOException {
    long len = je.getSize();
    if(len > Integer.MAX_VALUE) throw new IOException(
        "Class file too long to load.");
    if(len == -1) throw new IOException("Unknown class file size.");
    InputStream in = jar.getInputStream(je);
    byte[] buf = new byte[(int) len];
    int pos = 0;
    while(len - pos > 0) {
      int read = in.read(buf, pos, (int) len - pos);
      if(read == -1) break;
      pos += read;
    }
    return INSTANCE.defineClass(null, buf, 0, buf.length);
  }

  /**
   * Initializes the given classes. Breaks after the first error. Subsequent
   * classes are not initialized.
   * @param classes the classes to initialize.
   * @return the number of successfully initialized classes (breaks after the
   *         first error).
   */
  private static int initializeClasses(final ArrayList<Class<?>> classes) {
    int counter = 0;
    for(Class<?> c : classes) {
      try {
        Class.forName(c.getName(), true, INSTANCE);
        counter++;
      } catch(Throwable t) {
        // catch everything and break after an error.
        BaseX.errln("Failed to load class (%)", t.getMessage());
        break;
      }
    }
    return counter;
  }
}
