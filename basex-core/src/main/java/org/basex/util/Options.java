package org.basex.util;

import static org.basex.core.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * This class provides methods for accessing, reading and storing options.
 * Options (name/value pairs) may either be instances of the {@link Option} class.
 * If an instance of this class contains no pre-defined options, assigned options will
 * be added as free options.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class Options implements Iterable<Option> {
  /** Comment in configuration file. */
  private static final String PROPUSER = "# Local Options";

  /** Map with option names and definition. */
  private final HashMap<String, Option> options = new HashMap<String, Option>();
  /** Map with option names and values. */
  private final TreeMap<String, Object> values = new TreeMap<String, Object>();
  /** Free option definitions. */
  private final HashMap<String, String> free = new HashMap<String, String>();
  /** Options, cached from an input file. */
  private final StringBuilder user = new StringBuilder();

  /** Options file. */
  private IOFile file;

  /**
   * Constructor.
   */
  public Options() {
    this(null);
  }

  /**
   * Constructor, reading options from a configuration file.
   * @param suffix if {@code null}, file parsing will be skipped
   */
  public Options(final String suffix) {
    try {
      for(final Option opt : options(getClass())) {
        if(opt.value != null) {
          values.put(opt.name, opt.value);
          options.put(opt.name, opt);
        }
      }
    } catch(final Exception ex) {
      ex.printStackTrace();
      Util.notexpected(ex);
    }
    if(suffix != null) read(suffix);
    // sets options stored in system properties
    setSystem();
  }

  /**
   * Writes the options to disk.
   */
  public final synchronized void write() {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(file.file()));
      for(final Option opt : options(getClass())) {
        if(opt.value == null) {
          bw.write(NL + "# " + opt.name + NL);
          continue;
        }

        final Object val = values.get(opt.name);
        if(val instanceof String[]) {
          final String[] str = (String[]) val;
          bw.write(opt.name + " = " + str.length + NL);
          final int is = str.length;
          for(int i = 0; i < is; ++i) {
            if(str[i] != null) bw.write(opt.name + (i + 1) + " = " + str[i] + NL);
          }
        } else if(val instanceof int[]) {
          final int[] num = (int[]) val;
          final int ns = num.length;
          for(int i = 0; i < ns; ++i) {
            bw.write(opt.name + i + " = " + num[i] + NL);
          }
        } else {
          bw.write(opt.name + " = " + val + NL);
        }
      }
      bw.write(NL + PROPUSER + NL);
      bw.write(user.toString());
    } catch(final Exception ex) {
      Util.errln("% could not be written.", file);
      Util.debug(ex);
    } finally {
      if(bw != null) try { bw.close(); } catch(final IOException ignored) { }
    }
  }

  /**
   * Returns the option with the specified name.
   * @param name name of the option
   * @return value (may be {@code null})
   */
  public final synchronized Option option(final String name) {
    return options.get(name);
  }

  /**
   * Returns the value of the specified option.
   * @param option option
   * @return value (may be {@code null})
   */
  public final synchronized Object get(final Option option) {
    return values.get(option.name);
  }

  /**
   * Assigns a value to an option.
   * @param option option
   * @param value value to be assigned
   */
  public final synchronized void put(final Option option, final Object value) {
    if(values.put(option.name, value) == null)
      Util.notexpected("Option \"" + option.name + "\" not defined.");
  }

  /**
   * Returns the requested string.
   * @param option option to be found
   * @return value
   */
  public final synchronized String string(final Option option) {
    return (String) get(option, String.class);
  }

  /**
   * Returns the requested number.
   * @param option option to be found
   * @return value
   */
  public final synchronized int number(final Option option) {
    return (Integer) get(option, Integer.class);
  }

  /**
   * Returns the requested boolean.
   * @param option option to be found
   * @return value
   */
  public final synchronized boolean bool(final Option option) {
    return (Boolean) get(option, Boolean.class);
  }

  /**
   * Returns the requested string array.
   * @param option option to be found
   * @return value
   */
  public final synchronized String[] strings(final Option option) {
    return (String[]) get(option, String[].class);
  }

  /**
   * Returns the requested integer array.
   * @param option option to be found
   * @return value
   */
  public final synchronized int[] numbers(final Option option) {
    return (int[]) get(option, int[].class);
  }

  /**
   * Assigns a string to an option.
   * @param option option to be found
   * @param value value to be written
   */
  public final synchronized void string(final Option option, final String value) {
    put(option, value);
  }

  /**
   * Assigns an integer to an option.
   * @param option option to be found
   * @param value value to be written
   */
  public final synchronized void number(final Option option, final int value) {
    put(option, value);
  }

  /**
   * Assigns a boolean to an option.
   * @param option option to be found
   * @param value value to be written
   */
  public final synchronized void bool(final Option option, final boolean value) {
    put(option, value);
  }

  /**
   * Assigns a string array to an option.
   * @param option option to be found
   * @param value value to be written
   */
  public final synchronized void strings(final Option option, final String[] value) {
    put(option, value);
  }

  /**
   * Assigns an integer array to an option.
   * @param option option to be found
   * @param value value to be written
   */
  public final synchronized void numbers(final Option option, final int[] value) {
    put(option, value);
  }

  /**
   * Assigns a value after casting it to the correct type. If the option is unknown,
   * it will be added as free option.
   *
   * @param name name of option
   * @param value value
   * @return success flag
   * @throws IllegalArgumentException invalid argument
   */
  public final synchronized boolean set(final String name, final String value) {
    final Option option = options.get(name);
    if(option != null) {
      set(option, value);
      return true;
    }
    free.put(name, value);
    return false;
  }

  /**
   * Assigns a value after casting it to the correct type.
   * @param option option
   * @param value value
   * @throws IllegalArgumentException invalid argument
   */
  public final synchronized void set(final Option option, final String value) {
    final Object type = option.value;
    if(type instanceof Boolean) {
      // toggle boolean if no value was specified
      final boolean empty = value == null || value.isEmpty();
      put(option, empty ? !((Boolean) get(option)) : Util.yes(value));
    } else if(type instanceof Integer) {
      put(option, Integer.parseInt(value));
    } else if(type instanceof String) {
      put(option, value);
    } else if(type instanceof String[]) {
      // add value to array
      String[] ss = (String[]) get(option);
      if(ss == null) ss = new String[0];
      final StringList sl = new StringList(ss.length + 1);
      for(final String s : ss) sl.add(s);
      sl.add(value);
      put(option, sl.toArray());
    } else if(type instanceof int[]) {
      // add value to array
      int[] ii = (int[]) get(option);
      if(ii == null) ii = new int[0];
      final IntList il = new IntList(ii.length + 1);
      for(final int i : ii) il.add(i);
      il.add(Integer.parseInt(value));
      put(option, il.toArray());
    } else {
      Util.notexpected("Unknown option type: " + Util.className(type));
    }
  }

  /**
   * Returns all name/value pairs without pre-defined option.
   * @return options
   */
  public final synchronized HashMap<String, String> free() {
    return free;
  }

  /**
   * Indicates if options have been pre-defined.
   * @return result of check
   */
  public final synchronized boolean predefined() {
    return !values.isEmpty();
  }

  /**
   * Returns an error string for an unknown option.
   * @param name name of option
   * @return error string
   */
  public final synchronized String error(final String name) {
    final String sim = similar(name);
    return Util.info(sim != null ? Text.UNKNOWN_OPT_SIMILAR_X_X :
      Text.UNKNOWN_OPTION_X, name, sim);
  }

  /**
   * Inverts the boolean value of an option.
   * @param option option
   * @return new value
   */
  public final synchronized boolean invert(final Option option) {
    final boolean val = !bool(option);
    bool(option, val);
    return val;
  }

  /**
   * Scans the system properties and initializes the database options.
   * All properties starting with {@code org.basex.} will be assigned as options.
   */
  public final void setSystem() {
    // collect parameters that start with "org.basex."
    final StringList sl = new StringList();
    for(final Object key : System.getProperties().keySet()) {
      final String k = key.toString();
      if(k.startsWith(DBPREFIX)) sl.add(k);
    }
    // assign properties
    for(final String key : sl) {
      final String v = System.getProperty(key);
      final Option opt = option(key.substring(DBPREFIX.length()).toUpperCase(Locale.ENGLISH));
      if(opt != null) set(opt, v);
    }
  }

  @Override
  public final synchronized Iterator<Option> iterator() {
    return options.values().iterator();
  }

  @Override
  public final synchronized String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Entry<String, Object> e : values.entrySet()) {
      final String key = e.getKey();
      final Object value = e.getValue();
      final StringList sl = new StringList();
      if(value instanceof String[]) {
        for(final String s : (String[]) value) sl.add(s);
      } else if(value instanceof int[]) {
        for(final int s : (int[]) value) sl.add(Integer.toString(s));
      } else {
        sl.add(value.toString());
      }
      for(final String s : sl) {
        if(sb.length() != 0) sb.append(',');
        sb.append(key).append('=').append(s.replace(",", ",,"));
      }
    }
    return sb.toString();
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns a system property. If necessary, the key will
   * be converted to lower-case and prefixed with the {@link Prop#DBPREFIX} string.
   * @param option option
   * @return value, or empty string
   */
  public static String getSystem(final Option option) {
    String name = option.name.toLowerCase(Locale.ENGLISH);
    if(!name.startsWith(DBPREFIX)) name = DBPREFIX + name;
    final String v = System.getProperty(name);
    return v == null ? "" : v;
  }

  /**
   * Sets a system property if it has not been set before.
   * @param option option
   * @param val value
   */
  public static void setSystem(final Option option, final Object val) {
    setSystem(option.name, val);
  }

  /**
   * Sets a system property if it has not been set before. If necessary, the key will
   * be converted to lower-case and prefixed with the {@link Prop#DBPREFIX} string.
   * @param key key
   * @param val value
   */
  public static void setSystem(final String key, final Object val) {
    final String name = key.indexOf('.') != -1 ? key :
      DBPREFIX + key.toLowerCase(Locale.ENGLISH);
    if(System.getProperty(name) == null) System.setProperty(name, val.toString());
  }

  /**
   * Returns all options from the specified class.
   * @param clz options class
   * @return option instances
   * @throws IllegalAccessException exception
   */
  public static final Option[] options(final Class<? extends Options> clz)
      throws IllegalAccessException {

    final ArrayList<Option> opts = new ArrayList<Option>();
    for(final Field f : clz.getFields()) {
      if(!Modifier.isStatic(f.getModifiers())) continue;
      final Object obj = f.get(null);
      if(obj instanceof Option) opts.add((Option) obj);
    }
    return opts.toArray(new Option[opts.size()]);
  };

  // PROTECTED METHODS ==================================================================

  /**
   * Parses an option string and sets the options accordingly.
   * @param string options string
   * @param error throw exception when a value is unknown
   * @throws BaseXException database exception
   */
  protected final synchronized void parse(final String string, final boolean error)
      throws BaseXException {
    final BaseXException ex = parse(string);
    if(error && ex != null) throw ex;
  }

  /**
   * Parses an option string and sets the options accordingly.
   * @param string options string
   * @return exception, or {@code null}
   */
  public final synchronized BaseXException parse(final String string) {
    free.clear();
    final int sl = string.length();
    int i = 0;
    while(i < sl) {
      final int k = string.indexOf('=', i);
      if(k == -1) break;
      final String key = string.substring(i, k).trim();
      final StringBuilder val = new StringBuilder();
      i = k;
      while(++i < sl) {
        final char ch = string.charAt(i);
        if(ch == ',' && (++i == sl || string.charAt(i) != ',')) break;
        val.append(ch);
      }
      try {
        set(key, val.toString());
      } catch(final IllegalArgumentException ex) {
        return new BaseXException(Text.INVALID_VALUE_X_X, key, val);
      }
    }
    return free.isEmpty() ? null : new BaseXException(error(free.keySet().iterator().next()));
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Reads the configuration file and initializes the options.
   * The file is located in the project home directory.
   * @param suffix optional suffix of options file
   */
  private synchronized void read(final String suffix) {
    file = new IOFile(HOME + IO.BASEXSUFFIX + suffix);

    final StringList read = new StringList();
    final TokenBuilder err = new TokenBuilder();
    boolean local = false;
    if(!file.exists()) {
      err.addExt("Saving options in \"%\"..." + NL, file);
    } else {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader(file.file()));
        for(String line; (line = br.readLine()) != null;) {
          line = line.trim();

          // start of local options
          if(line.equals(PROPUSER)) {
            local = true;
            continue;
          }
          if(local) user.append(line).append(NL);

          if(line.isEmpty() || line.charAt(0) == '#') continue;
          final int d = line.indexOf('=');
          if(d < 0) {
            err.addExt("%: \"%\" ignored. " + NL, file, line);
            continue;
          }

          final String val = line.substring(d + 1).trim();
          String key = line.substring(0, d).trim();

          // extract numeric value in key
          int num = 0;
          final int ss = key.length();
          for(int s = 0; s < ss; ++s) {
            if(Character.isDigit(key.charAt(s))) {
              num = Integer.parseInt(key.substring(s));
              key = key.substring(0, s);
              break;
            }
          }
          // cache local options as system properties
          if(local) {
            setSystem(key, val);
            continue;
          }

          final Object entry = values.get(key);
          if(entry == null) {
            err.addExt("%: \"%\" not found. " + NL, file, key);
          } else if(entry instanceof String) {
            values.put(key, val);
          } else if(entry instanceof Integer) {
            values.put(key, Integer.parseInt(val));
          } else if(entry instanceof Boolean) {
            values.put(key, Boolean.parseBoolean(val));
          } else if(entry instanceof String[]) {
            if(num == 0) {
              values.put(key, new String[Integer.parseInt(val)]);
            } else {
              ((String[]) entry)[num - 1] = val;
            }
          } else if(entry instanceof int[]) {
            ((int[]) entry)[num] = Integer.parseInt(val);
          }
          // add key for final check
          read.add(key);
        }
      } catch(final Exception ex) {
        err.addExt("% could not be parsed." + NL, file);
        Util.debug(ex);
      } finally {
        if(br != null) try { br.close(); } catch(final IOException ignored) { }
      }
    }

    // check if all mandatory files have been read
    try {
      if(err.isEmpty()) {
        boolean ok = true;
        for(final Option opt : options(getClass())) {
          if(ok && opt.value != null) ok = read.contains(opt.name);
        }
        if(!ok) err.addExt("Saving options in \"%\"..." + NL, file);
      }
    } catch(final IllegalAccessException ex) {
      Util.notexpected(ex);
    }

    if(!err.isEmpty()) {
      Util.err(err.toString());
      write();
    }
  }

  /**
   * Retrieves the value of the specified option. Throws an error if value cannot be read.
   * @param opt option
   * @param c expected type
   * @return result
   */
  private Object get(final Option opt, final Class<?> c) {
    final Object entry = values.get(opt.name);
    if(entry == null) Util.notexpected("Option " + opt.name + " not defined.");

    final Class<?> cc = entry.getClass();
    if(c != cc) Util.notexpected("Option '" + opt.name + "' is of type " + Util.className(cc));
    return entry;
  }

  /**
   * Returns a key similar to the specified string, or {@code null}.
   * @param key key to be found
   * @return similar key
   */
  private String similar(final String key) {
    final byte[] name = token(key);
    final Levenshtein ls = new Levenshtein();
    for(final String opts : values.keySet()) {
      if(ls.similar(name, token(opts))) return opts;
    }
    return null;
  }
}
