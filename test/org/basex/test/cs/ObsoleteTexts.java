package org.basex.test.cs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks all project interfaces for potentially obsolete texts.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ObsoleteTexts {
  /** Constants matcher. */
  private static final Pattern CONSTANTS =
    Pattern.compile("\\b([A-Z][A-Z0-9_]+)\\b");
  /** Classes to test. */
  private static final Class<?>[] CLASSES = {
    org.basex.core.Text.class,
    org.basex.build.BuildText.class,
    org.basex.build.fs.FSText.class,
    org.basex.data.DataText.class,
    org.basex.api.dom.BXText.class,
    org.basex.query.QueryText.class,
  }; 

  /** Private constructor, preventing instantiation. */
  private ObsoleteTexts() { }

  /**
   * Test method.
   * @param args ignored command-line arguments.
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    final HashSet<String> set = new HashSet<String>();
    read(new File("src"), set);
    
    for(final Class<?> c : CLASSES) {
      System.out.println(c.getSimpleName() + ".java");
      for(final Field f : c.getDeclaredFields()) {
        final String name = f.getName();
        if(!set.contains(name)) {
          System.out.println("- " + name);
        }
      }
    }
  }
  
  /**
   * Parses all java classes.
   * @param file file reference
   * @param set hash set, containing all string constants
   * @throws Exception exception
   */
  static void read(final File file, final HashSet<String> set)
      throws Exception {

    for(final File f : file.listFiles()) {
      String name = f.getName();
      if(f.isDirectory()) {
        read(f, set);
      } else if(name.endsWith(".java")) {
        name = name.replaceAll("\\.java", "");
        boolean found = false;
        for(final Class<?> c : CLASSES) {
          found |= c.getSimpleName().equals(name);
        }        
        if(found) continue;
        
        final BufferedReader br = new BufferedReader(new FileReader(f));
        while(true) {
          final String l = br.readLine();
          if(l == null) break;

          final Matcher m = CONSTANTS.matcher(l);
          while(m.find()) set.add(m.group(1));
        }
        br.close();
      }
    }
  }
}
