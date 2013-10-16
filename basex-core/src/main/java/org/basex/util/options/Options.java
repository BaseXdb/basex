package org.basex.util.options;

import static java.lang.Integer.*;
import static org.basex.core.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
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
  protected final TreeMap<String, Option> options = new TreeMap<String, Option>();
  /** Map with option names and values. */
  protected final TreeMap<String, Object> values = new TreeMap<String, Object>();
  /** Free option definitions. */
  protected final HashMap<String, String> free = new HashMap<String, String>();
  /** Options, cached from an input file. */
  protected final StringBuilder user = new StringBuilder();

  /** Options file. */
  private IOFile file;

  /**
   * Default constructor.
   */
  public Options() {
    init();
  }

  /**
   * Constructor with options string.
   * @param opts options strings
   * @throws BaseXException database exception
   */
  protected Options(final String opts) throws BaseXException {
    this();
    parse(opts);
  }

  /**
   * Constructor with options file.
   * @param opts options file
   */
  protected Options(final IOFile opts) {
    this();
    if(opts != null) read(opts);
    // overwrite initialized options with system properties
    setSystem();
  }

  /**
   * Initializes all options.
   */
  private void init() {
    try {
      for(final Option opt : options(getClass())) {
        if(opt instanceof Comment) continue;
        final String name = opt.name();
        values.put(name, opt.value());
        options.put(name, opt);
      }
    } catch(final Exception ex) {
      Util.notexpected(ex);
    }
  }

  /**
   * Writes the options to disk.
   */
  public final synchronized void write() {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(file.file()));
      boolean first = true;
      for(final Option opt : options(getClass())) {
        final String name = opt.name();
        if(opt instanceof Comment) {
          if(!first) bw.write(NL);
          bw.write("# " + name + NL);
        } else if(opt instanceof NumbersOption) {
          final int[] ints = get((NumbersOption) opt);
          final int is = ints == null ? 0 : ints.length;
          for(int i = 0; i < is; ++i) bw.write(name + i + " = " + ints[i] + NL);
        } else if(opt instanceof StringsOption) {
          final String[] strings = get((StringsOption) opt);
          final int ss = strings == null ? 0 : strings.length;
          bw.write(name + " = " + ss + NL);
          for(int i = 0; i < ss; ++i) bw.write(name + (i + 1) + " = " + strings[i] + NL);
        } else {
          bw.write(name + " = " + get(opt) + NL);
        }
        first = false;
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
    return values.get(option.name());
  }

  /**
   * Sets an option to a value without checking its type.
   * @param option option
   * @param value value to be assigned
   */
  public final synchronized void put(final Option option, final Object value) {
    values.put(option.name(), value);
  }

  /**
   * Checks if a value was set for the specified option.
   * @param option option
   * @return result of check
   */
  public final synchronized boolean contains(final Option option) {
    return get(option) != null;
  }

  /**
   * Returns the requested string.
   * @param option option to be found
   * @return value
   */
  public final synchronized String get(final StringOption option) {
    return (String) get((Option) option);
  }

  /**
   * Returns the requested number.
   * @param option option to be found
   * @return value
   */
  public final synchronized Integer get(final NumberOption option) {
    return (Integer) get((Option) option);
  }

  /**
   * Returns the requested boolean.
   * @param option option to be found
   * @return value
   */
  public final synchronized Boolean get(final BooleanOption option) {
    return (Boolean) get((Option) option);
  }

  /**
   * Returns the requested string array.
   * @param option option to be found
   * @return value
   */
  public final synchronized String[] get(final StringsOption option) {
    final String[] v = (String[]) get((Option) option);
    return v == null ? null : v.clone();
  }

  /**
   * Returns the requested integer array.
   * @param option option to be found
   * @return value
   */
  public final synchronized int[] get(final NumbersOption option) {
    final int[] v = (int[]) get((Option) option);
    return v == null ? null : v.clone();
  }

  /**
   * Returns the requested options.
   * @param option option to be found
   * @param <O> options
   * @return value
   */
  @SuppressWarnings("unchecked")
  public final synchronized <O extends Options> O get(final OptionsOption<O> option) {
    final O o = (O) get((Option) option);
    if(o == null) return null;
    try {
      final O n = ((Class<O>) o.getClass()).newInstance();
      n.parse(o.toString());
      return n;
    } catch(final Exception ex) {
      throw Util.notexpected(ex);
    }
  }

  /**
   * Returns the requested enum value.
   * @param option option to be found
   * @param <V> enumeration value
   * @return value
   */
  @SuppressWarnings("unchecked")
  public final synchronized <V extends Enum<V>> V get(final EnumOption<V> option) {
    return (V) get((Option) option);
  }

  /**
   * Sets the string value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final StringOption option, final String value) {
    put(option, value);
  }

  /**
   * Sets the integer value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final NumberOption option, final int value) {
    put(option, value);
  }

  /**
   * Sets the boolean value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final BooleanOption option, final boolean value) {
    put(option, value);
  }

  /**
   * Sets the string array value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final StringsOption option, final String[] value) {
    put(option, value);
  }

  /**
   * Sets the integer array value of an option.
   * @param option option to be set
   * @param value value to be written
   */
  public final synchronized void set(final NumbersOption option, final int[] value) {
    put(option, value);
  }

  /**
   * Sets the options of an option.
   * @param option option to be set
   * @param value value to be set
   * @param <O> options
   */
  public final synchronized <O extends Options> void set(final OptionsOption<O> option,
      final O value) {
    put(option, value);
  }

  /**
   * Sets the enumeration of an option.
   * @param option option to be set
   * @param value value to be set
   * @param <V> enumeration value
   */
  public final synchronized <V extends Enum<V>> void set(final EnumOption<V> option,
      final Enum<V> value) {
    put(option, value);
  }

  /**
   * Sets the enumeration of an option.
   * @param option option to be set
   * @param value string value, which will be converted to an enum value or {@code null}
   * @param <V> enumeration value
   */
  public final synchronized <V extends Enum<V>> void set(final EnumOption<V> option,
      final String value) {
    put(option, option.get(value));
  }

  /**
   * Assigns a value after casting it to the correct type. If the option is unknown,
   * it will be added as free option.
   * @param name name of option
   * @param val value
   * @param <V> enumeration value
   * @param <O> options
   * @throws BaseXException database exception
   */
  public synchronized <O extends Options, V extends Enum<V>> void assign(final String name,
      final String val) throws BaseXException {

    if(options.isEmpty()) {
      free.put(name, val);
    } else {
      assign(name, val, -1);
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
  public final synchronized boolean invert(final BooleanOption option) {
    final boolean val = !get(option);
    set(option, val);
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
      try {
        assign(key.substring(DBPREFIX.length()).toUpperCase(Locale.ENGLISH), v);
      } catch(final BaseXException ignore) { /* may belong to another Options instance */ }
    }
  }

  @Override
  public final synchronized Iterator<Option> iterator() {
    return options.values().iterator();
  }

  @Override
  public final synchronized String toString() {
    // only those options are listed the value of which differs from default value
    final StringBuilder sb = new StringBuilder();
    for(final Entry<String, Object> e : values.entrySet()) {
      final String name = e.getKey();
      final Object value = e.getValue();
      if(value == null) continue;

      final StringList sl = new StringList();
      final Object value2 = options.get(name).value();
      if(value instanceof String[]) {
        for(final String s : (String[]) value) sl.add(s);
      } else if(value instanceof int[]) {
        for(final int s : (int[]) value) sl.add(Integer.toString(s));
      } else if(value instanceof Options) {
        final String s = value.toString();
        if(value2 == null || !s.equals(value2.toString())) sl.add(s);
      } else if(!value.equals(value2)) {
        sl.add(value.toString());
      }
      for(final String s : sl) {
        if(sb.length() != 0) sb.append(',');
        sb.append(name).append('=').append(s.replace(",", ",,"));
      }
    }
    return sb.toString();
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns a system property. If necessary, the key will be converted to lower-case
   * and prefixed with the {@link Prop#DBPREFIX} string.
   * @param option option
   * @return value, or empty string
   */
  public static String getSystem(final Option option) {
    String name = option.name().toLowerCase(Locale.ENGLISH);
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
    setSystem(option.name(), val);
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

  /**
   * Returns a list of allowed keys.
   * @param option option
   * @param all allowed values
   * @return exception
   */
  public static String allowed(final Option option, final Object... all) {
    final TokenBuilder vals = new TokenBuilder();
    for(final Object a : all) vals.add(vals.isEmpty() ? "" : ",").add(a.toString());
    return Util.info(Text.OPT_ONEOF, option.name(), vals);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Parses an option string and sets the options accordingly.
   * @param string options string
   * @throws BaseXException database exception
   */
  public synchronized void parse(final String string) throws BaseXException {
    final int sl = string.length();
    int i = 0;
    while(i < sl) {
      int k = string.indexOf('=', i);
      if(k == -1) k = sl;
      final String key = string.substring(i, k).trim();
      final StringBuilder val = new StringBuilder();
      i = k;
      while(++i < sl) {
        final char ch = string.charAt(i);
        if(ch == ',' && (++i == sl || string.charAt(i) != ',')) break;
        val.append(ch);
      }
      assign(key, val.toString());
    }
  }

  /**
   * Reads the configuration file and initializes the options.
   * The file is located in the project home directory.
   * @param opts options file
   * @param <O> options
   * @param <V> enumeration
   */
  private synchronized <O extends Options, V extends Enum<V>> void read(final IOFile opts) {
    file = opts;
    final StringList read = new StringList();
    final StringList errs = new StringList();
    final boolean exists = file.exists();
    boolean local = false;
    if(exists) {
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
            errs.add("Line \"" + line + "\" ignored.");
            continue;
          }

          final String val = line.substring(d + 1).trim();
          String name = line.substring(0, d).trim();

          // extract numeric value in key
          int num = 0;
          final int ss = name.length();
          for(int s = 0; s < ss; ++s) {
            if(Character.isDigit(name.charAt(s))) {
              num = Token.toInt(name.substring(s));
              name = name.substring(0, s);
              break;
            }
          }

          if(local) {
            // cache local options as system properties
            setSystem(name, val);
          } else {
            try {
              assign(name, val, num);
              read.add(name);
            } catch(final BaseXException ex) {
              errs.add(ex.getMessage());
            }
          }
        }
      } catch(final IOException ex) {
        errs.add("File could not be parsed.");
        Util.errln(ex);
      } finally {
        if(br != null) try { br.close(); } catch(final IOException ignored) { }
      }
    }

    // check if all mandatory files have been read
    boolean ok = true;
    if(errs.isEmpty()) {
      try {
        for(final Option opt : options(getClass())) {
          if(ok && !(opt instanceof Comment)) ok = read.contains(opt.name());
        }
      } catch(final IllegalAccessException ex) {
        Util.notexpected(ex);
      }
    }

    if(!exists || !errs.isEmpty()) {
      write();
      errs.add("Writing new configuration file.");
      for(final String s : errs) Util.errln(file + ": " + s);
    }
  }

  /**
   * Assigns the specified name and value.
   * @param <O> options
   * @param <V> enumeration
   * @param name name of option
   * @param val value of option
   * @param num number (optional)
   * @throws BaseXException database exception
   */
  private synchronized <O extends Options, V extends Enum<V>> void assign(
      final String name, final String val, final int num) throws BaseXException {

    final Option option = options.get(name);
    if(option == null) {
      throw new BaseXException(error(name));
    } else if(option instanceof BooleanOption) {
      final boolean v;
      if(val == null || val.isEmpty()) {
        v = !get((BooleanOption) option);
      } else {
        v = Util.yes(val);
        if(!v && !Util.no(val)) throw new BaseXException(Text.OPT_BOOLEAN, option.name());
      }
      put(option, v);
    } else if(option instanceof NumberOption) {
      final int v = toInt(val);
      if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER, option.name());
      put(option, v);
    } else if(option instanceof StringOption) {
      put(option, val);
    } else if(option instanceof EnumOption) {
      @SuppressWarnings("unchecked")
      final EnumOption<V> eo = (EnumOption<V>) option;
      final V v = eo.get(val);
      if(v == null) throw new BaseXException(allowed(option, (Object[]) eo.values()));
      put(option, v);
    } else if(option instanceof OptionsOption) {
      @SuppressWarnings("unchecked")
      final O o = ((OptionsOption<O>) option).newInstance();
      o.parse(val);
      put(option, o);
    } else if(option instanceof NumbersOption) {
      final int v = toInt(val);
      if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER, option.name());
      int[] ii = (int[]) get(option);
      if(num == -1) {
        if(ii == null) ii = new int[0];
        final IntList il = new IntList(ii.length + 1);
        for(final int i : ii) il.add(i);
        il.add(v);
        put(option, il.toArray());
      } else {
        if(num < 0 || num >= ii.length) throw new BaseXException(Text.OPT_OFFSET, option.name());
        ii[num] = v;
      }
    } else if(option instanceof StringsOption) {
      String[] ss = (String[]) get(option);
      if(num == -1) {
        if(ss == null) ss = new String[0];
        final StringList sl = new StringList(ss.length + 1);
        for(final String s : ss) sl.add(s);
        sl.add(val);
        put(option, sl.toArray());
      } else if(num == 0) {
        final int v = toInt(val);
        if(v == MIN_VALUE) throw new BaseXException(Text.OPT_NUMBER, option.name());
        values.put(name, new String[v]);
      } else {
        if(num <= 0 || num > ss.length) throw new BaseXException(Text.OPT_OFFSET, option.name());
        ss[num - 1] = val;
      }
    }
  }

  /**
   * Returns an option name similar to the specified string, or {@code null}.
   * @param name name to be found
   * @return similar name
   */
  private String similar(final String name) {
    final byte[] nm = token(name);
    final Levenshtein ls = new Levenshtein();
    for(final String opts : options.keySet()) {
      if(ls.similar(nm, token(opts))) return opts;
    }
    return null;
  }
}
