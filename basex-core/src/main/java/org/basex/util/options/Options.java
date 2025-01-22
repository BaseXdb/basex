package org.basex.util.options;

import static org.basex.query.QueryError.*;
import static org.basex.util.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * This class provides methods for accessing, reading and storing options.
 * Options (name/value pairs) may either be instances of the {@link Option} class.
 * If an instance of this class contains no pre-defined options, assigned options will
 * be added as free options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class Options implements Iterable<Option<?>> {
  /** Yes/No enumeration. */
  public enum YesNo {
    /** Yes. */ YES,
    /** No.  */ NO;

    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }

  /** Yes/No/Omit enumeration. */
  public enum YesNoOmit {
    /** Yes.  */ YES,
    /** No.   */ NO,
    /** Omit. */ OMIT;

    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }

  /** Comment in configuration file. */
  private static final String PROPUSER = "# Local Options";

  /** Map with option names and definitions. */
  private final TreeMap<String, Option<?>> definitions;
  /** Map with option names and values. */
  private final TreeMap<String, Object> values;
  /** Free option assignments. */
  private final HashMap<String, String> free;

  /** Options, cached from an input file. */
  private final StringList user = new StringList();
  /** Options file. */
  private IOFile file;

  /**
   * Default constructor.
   */
  public Options() {
    this((IOFile) null);
  }

  /**
   * Constructor with options file.
   * @param opts options file
   */
  protected Options(final IOFile opts) {
    definitions = new TreeMap<>();
    values = new TreeMap<>();
    free = new HashMap<>();
    try {
      for(final Option<?> opt : options(getClass())) {
        if(opt instanceof Comment) continue;
        final String name = opt.name();
        definitions.put(name, opt);
        values.put(name, opt.value());
      }
    } catch(final Exception ex) {
      throw Util.notExpected(ex);
    }
    if(opts != null) read(opts);
  }

  /**
   * Constructor with options to be copied.
   * @param opts options
   */
  @SuppressWarnings("unchecked")
  protected Options(final Options opts) {
    definitions = (TreeMap<String, Option<?>>) opts.definitions.clone();
    values = (TreeMap<String, Object>) opts.values.clone();
    free = (HashMap<String, String>) opts.free.clone();
    user.add(opts.user);
    file = opts.file;
  }

  /**
   * Writes the options to disk.
   */
  public final synchronized void write() {
    final StringList lines = new StringList();
    try {
      for(final Option<?> opt : options(getClass())) {
        final String name = opt.name();
        if(opt instanceof Comment) {
          if(!lines.isEmpty()) lines.add("");
          lines.add("# " + name);
        } else if(opt instanceof NumbersOption) {
          final int[] ints = get((NumbersOption) opt);
          final int is = ints == null ? 0 : ints.length;
          for(int i = 0; i < is; i++) lines.add(name + i + " = " + ints[i]);
        } else if(opt instanceof StringsOption) {
          final String[] strings = get((StringsOption) opt);
          final int ss = strings == null ? 0 : strings.length;
          lines.add(name + " = " + ss);
          for(int s = 0; s < ss; s++) lines.add(name + (s + 1) + " = " + strings[s]);
        } else {
          lines.add(name + " = " + get(opt));
        }
      }
      lines.add("").add(PROPUSER).add(user);

      // only write file if contents have changed
      final TokenBuilder tb = new TokenBuilder();
      for(final String line : lines) tb.add(line).add(NL);
      final byte[] contents = tb.finish();

      boolean skip = file.exists();
      if(skip) {
        final TokenBuilder tmp = new TokenBuilder(contents.length);
        try(NewlineInput nli = new NewlineInput(file)) {
          for(String line; (line = nli.readLine()) != null;) tmp.add(line).add(NL);
        }
        skip = eq(contents, tmp.finish());
      }
      if(!skip) {
        file.parent().md();
        file.write(contents);
      }
    } catch(final Exception ex) {
      Util.errln("% could not be written.", file);
      Util.debug(ex);
    }
  }

  /**
   * Returns the option with the specified name.
   * @param name name of the option
   * @return value (can be {@code null})
   */
  public final synchronized Option<?> option(final String name) {
    return definitions.get(name);
  }

  /**
   * Returns the value of the specified option.
   * @param option option
   * @return value (can be {@code null})
   */
  public final synchronized Object get(final Option<?> option) {
    return get(option.name());
  }

  /**
   * Returns the value of the specified option.
   * @param name name of option
   * @return value (can be {@code null})
   */
  public final synchronized Object get(final String name) {
    return values.get(name);
  }

  /**
   * Sets an option to a value without checking its type.
   * @param option option
   * @param value value to be assigned
   */
  public final synchronized void put(final Option<?> option, final Object value) {
    values.put(option.name(), value);
  }

  /**
   * Checks if a value was set for the specified option.
   * @param option option
   * @return result of check
   */
  public final synchronized boolean contains(final Option<?> option) {
    return get(option) != null;
  }

  /**
   * Returns the requested string.
   * @param option option to be found
   * @return value or {@code null})
   */
  public final synchronized String get(final StringOption option) {
    return (String) get((Option<?>) option);
  }

  /**
   * Returns the requested number.
   * @param option option to be found
   * @return value or {@code null})
   */
  public final synchronized Integer get(final NumberOption option) {
    return (Integer) get((Option<?>) option);
  }

  /**
   * Returns the requested boolean.
   * @param option option to be found
   * @return value or {@code null})
   */
  public final synchronized Boolean get(final BooleanOption option) {
    return (Boolean) get((Option<?>) option);
  }

  /**
   * Returns the requested value.
   * @param option option to be found
   * @return value or {@code null})
   */
  public final synchronized Value get(final ValueOption option) {
    return (Value) get((Option<?>) option);
  }

  /**
   * Returns the requested string array.
   * @param option option to be found
   * @return value or {@code null})
   */
  public final synchronized String[] get(final StringsOption option) {
    return (String[]) get((Option<?>) option);
  }

  /**
   * Returns the requested integer array.
   * @param option option to be found
   * @return value or {@code null})
   */
  public final synchronized int[] get(final NumbersOption option) {
    return (int[]) get((Option<?>) option);
  }

  /**
   * Returns the requested options.
   * @param option option to be found
   * @param <O> options
   * @return value or {@code null})
   */
  @SuppressWarnings({ "unchecked", "cast"})
  public final synchronized <O extends Options> O get(final OptionsOption<O> option) {
    return (O) get((Option<?>) option);
  }

  /**
   * Returns the requested enum value.
   * @param option option to be found
   * @param <E> enumeration value
   * @return value or {@code null})
   */
  @SuppressWarnings({ "unchecked", "cast"})
  public final synchronized <E extends Enum<E>> E get(final EnumOption<E> option) {
    return (E) get((Option<?>) option);
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
   * Sets the value of an option.
   * @param option option to be set
   * @param value value to be set
   */
  public final synchronized void set(final ValueOption option, final Value value) {
    put(option, value);
  }

  /**
   * Assigns a value after casting it to the correct type. If the option is unknown,
   * it will be added as free option.
   * @param name name of option
   * @param value value
   * @throws BaseXException database exception
   */
  public synchronized void assign(final String name, final String value) throws BaseXException {
    if(definitions.isEmpty()) {
      free.put(name, value);
    } else {
      assign(name, value, -1, true);
    }
  }

  /**
   * Assigns a value after casting it to the correct type. If the option is unknown,
   * it will be added as free option.
   * @param name name of option
   * @param value value to be assigned
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public synchronized void assign(final Item name, final Value value, final InputInfo info)
      throws QueryException {

    final String nm;
    if(name instanceof QNm) {
      nm = string(((QNm) name).unique());
    } else if(name.type.isStringOrUntyped()) {
      nm = string(name.string(info));
    } else {
      throw INVALIDOPTION_X_X_X.get(info, AtomType.STRING, name.type, name);
    }

    if(definitions.isEmpty()) {
      free.put(nm, serialize(value, info));
    } else {
      assign(nm, value, info);
    }
  }

  /**
   * Creates a string representation of the specified value.
   * @param value value
   * @param info input info (can be {@code null})
   * @return string
   * @throws QueryException query exception
   */
  private static String serialize(final Value value, final InputInfo info) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Item item : value) {
      if(!tb.isEmpty()) tb.add(' ');
      if(item instanceof XQMap) {
        final XQMap map = (XQMap) item;
        map.forEach((key, v) -> {
          if(!tb.isEmpty()) tb.add(',');
          tb.add(key.string(info)).add('=');
          if(!v.isItem()) throw INVALIDOPTION_X_X_X.get(info, AtomType.STRING, v.seqType(), v);
          tb.add(string(((Item) v).string(info)).replace(",", ",,"));
        });
      } else if(item instanceof QNm) {
        tb.add(((QNm) item).unique());
      } else {
        tb.add(item.string(info));
      }
    }
    return tb.toString();
  }

  /**
   * Returns all name/value pairs without pre-defined option.
   * @return options
   */
  public final synchronized HashMap<String, String> free() {
    return free;
  }

  /**
   * Returns a map representation of the entries of the requested string (separated by commas).
   * @param option option to be found
   * @return map
   */
  public final Map<String, String> toMap(final StringOption option) {
    return toMap(get(option));
  }

  /**
   * Returns an error string for an unknown option.
   * @param option option
   * @return error string
   */
  public final synchronized String similar(final Object option) {
    return similar(option, definitions);
  }

  /**
   * Returns an error string for an unknown option.
   * @param option option
   * @param options options
   * @return error string
   */
  public static final String similar(final Object option, final Map<String, Option<?>> options) {
    final Object similar = Levenshtein.similar(token(option),
        options.keySet().toArray(String[]::new));
    return similar != null ? Util.info(Text.UNKNOWN_OPT_SIMILAR_X_X, option, similar) :
      unknown(option);
  }

  /**
   * Returns an error string for an unknown option.
   * @param option option
   * @return error string
   */
  public static final String unknown(final Object option) {
    return Util.info(Text.UNKNOWN_OPTION_X, option);
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
   * Overwrites the options with global options and system properties.
   * All properties starting with {@code org.basex.} will be assigned as options.
   */
  public void setSystem() {
    // assign global options
    for(final Entry<String, String> entry : entries()) {
      String name = entry.getKey();
      final String value = entry.getValue();
      if(name.startsWith(DBPREFIX)) {
        name = name.substring(DBPREFIX.length()).toUpperCase(Locale.ENGLISH);
        try {
          if(assign(name, value, -1, false)) Util.debugln(name + Text.COLS + value);
        } catch(final BaseXException ex) {
          Util.errln(ex);
        }
      }
    }
  }

  /**
   * Parses the specified options.
   * @param type media type
   * @throws BaseXException database exception
   */
  public final synchronized void assign(final MediaType type) throws BaseXException {
    for(final Entry<String, String> entry : type.parameters()) {
      if(definitions.isEmpty()) {
        free.put(entry.getKey(), entry.getValue());
      } else {
        assign(entry.getKey(), entry.getValue(), -1, false);
      }
    }
  }

  /**
   * Parses and assigns options string from the specified string.
   * @param string options string
   * @throws BaseXException database exception
   */
  public final synchronized void assign(final String string) throws BaseXException {
    for(final Map.Entry<String, String> entry : toMap(string).entrySet()) {
      assign(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Parses and assigns options from the specified map.
   * @param map options map
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public final synchronized void assign(final XQMap map, final InputInfo info)
      throws QueryException {
    map.forEach((key, value) -> assign(key, value, info));
  }

  /**
   * Returns the names of all options.
   * @return names
   */
  public final synchronized String[] names() {
    final StringList sl = new StringList(definitions.size());
    for(final Option<?> option : this) sl.add(option.name());
    return sl.finish();
  }

  @Override
  public final synchronized Iterator<Option<?>> iterator() {
    return definitions.values().iterator();
  }

  @Override
  public final synchronized String toString() {
    // only those options are listed whose value differs from default value
    final StringBuilder sb = new StringBuilder();
    values.forEach((name, value) -> {
      if(value != null) {
        final StringList list = new StringList();
        final Object value2 = definitions.get(name).value();
        if(value instanceof String[]) {
          for(final String s : (String[]) value) list.add(s);
        } else if(value instanceof int[]) {
          for(final int s : (int[]) value) list.add(Integer.toString(s));
        } else if(value instanceof Options) {
          final String s = value.toString();
          if(value2 == null || !s.equals(value2.toString())) list.add(s);
        } else if(!value.equals(value2)) {
          if(value instanceof Value) {
            // quick and dirty: rewrite "A" to A, true() to true, ...
            for(final Item item : (Value) value) {
              list.add(item.toString().replaceAll("[\"()]", ""));
            }
          } else {
            list.add(value.toString());
          }
        }
        for(final String s : list) {
          if(sb.length() != 0) sb.append(',');
          sb.append(name).append('=').append(s.replace(",", ",,"));
        }
      }
    });
    return sb.toString();
  }

  // STATIC METHODS ===============================================================================

  /**
   * Assigns a value to an option.
   * @param option option
   * @param value value to be assigned
   * @param index index of an array value (optional, can be {@code -1})
   * @param assign function to assign the value
   * @param options current options (can be {@code null})
   * @return error string or {@code null}
   */
  public static String assign(final Option<?> option, final String value, final int index,
      final Consumer<Object> assign, final Options options) {

    final String name = option.name();
    if(option instanceof BooleanOption) {
      Boolean v;
      if(value.isEmpty() && options != null) {
        // no value given: invert current value
        v = options.get((BooleanOption) option);
        if(v != null) v = !v.booleanValue();
      } else {
        v = Strings.toBoolean(value);
      }
      if(v == null) return Util.info(Text.OPT_BOOLEAN_X_X, name, value);
      assign.accept(v);
    } else if(option instanceof NumberOption) {
      final int v = Strings.toInt(value);
      if(v == Integer.MIN_VALUE) return Util.info(Text.OPT_NUMBER_X_X, name, value);
      assign.accept(v);
    } else if(option instanceof StringOption) {
      assign.accept(value);
    } else if(option instanceof ValueOption) {
      final Boolean b = Strings.toBoolean(value);
      assign.accept(b != null ? Bln.get(b) : Str.get(value));
    } else if(option instanceof EnumOption) {
      final EnumOption<?> eo = (EnumOption<?>) option;
      final Object v = eo.get(option instanceof EnumOption ? normalize(value) : value);
      if(v == null) return allowed(eo, value, (Object[]) eo.values());
      assign.accept(v);
    } else if(option instanceof OptionsOption) {
      final Options o = ((OptionsOption<?>) option).newInstance();
      try {
        o.assign(value);
      } catch(final BaseXException ex) {
        return Util.message(ex);
      }
      assign.accept(o);
    } else if(option instanceof NumbersOption && options != null) {
      final int v = Strings.toInt(value);
      if(v == Integer.MIN_VALUE) return Util.info(Text.OPT_NUMBER_X_X, name, value);
      int[] ii = (int[]) options.get(option);
      if(index == -1) {
        if(ii == null) ii = new int[0];
        final IntList il = new IntList(ii.length + 1);
        for(final int i : ii) il.add(i);
        assign.accept(il.add(v).finish());
      } else {
        if(index < 0 || index >= ii.length) return Util.info(Text.OPT_OFFSET_X, name);
        ii[index] = v;
      }
    } else if(option instanceof StringsOption && options != null) {
      String[] ss = (String[]) options.get(option);
      if(index == -1) {
        if(ss == null) ss = new String[0];
        final StringList sl = new StringList(ss.length + 1);
        for(final String s : ss) sl.add(s);
        assign.accept(sl.add(value).finish());
      } else if(index == 0) {
        final int i = Strings.toInt(value);
        if(i < 0) return Util.info(Text.OPT_NUMBER_X_X, name, value);
        options.values.put(name, new String[i]);
      } else {
        if(index <= 0 || index > ss.length) return Util.info(Text.OPT_OFFSET_X, name);
        ss[index - 1] = value;
      }
    } else {
      throw Util.notExpected("Unsupported option (%): %", Util.className(option), option);
    }
    return null;
  }

  /**
   * Returns a message with allowed keys.
   * @param option option
   * @param value supplied value
   * @param all allowed values
   * @return exception
   */
  public static String allowed(final Option<?> option, final String value, final Object... all) {
    final TokenBuilder vals = new TokenBuilder();
    for(final Object a : all) {
      if(!vals.isEmpty()) vals.add(',');
      vals.add(a);
    }
    return Util.info(Text.OPT_ONEOF_X_X_X, option.name(), value, vals);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns all options from the specified class.
   * @param clz options class
   * @return option instances
   * @throws IllegalAccessException exception
   */
  private static Option<?>[] options(final Class<? extends Options> clz)
      throws IllegalAccessException {

    final ArrayList<Option<?>> opts = new ArrayList<>();
    for(final Field f : clz.getFields()) {
      if(!Modifier.isStatic(f.getModifiers())) continue;
      final Object obj = f.get(null);
      if(obj instanceof Option) opts.add((Option<?>) obj);
    }
    return opts.toArray(Option[]::new);
  }

  /**
   * Reads the configuration file and initializes the options.
   * The file is located in the project home directory.
   * @param opts options file
   */
  private synchronized void read(final IOFile opts) {
    file = opts;
    final StringList read = new StringList(), errs = new StringList();
    final boolean exists = file.exists();
    if(exists) {
      try(NewlineInput ni = new NewlineInput(opts)) {
        boolean local = false;
        for(String line; (line = ni.readLine()) != null;) {
          line = line.trim();

          // start of local options
          if(line.equals(PROPUSER)) {
            local = true;
            continue;
          }
          if(local) user.add(line);

          if(line.isEmpty() || line.charAt(0) == '#') continue;
          final int d = line.indexOf('=');
          if(d < 0) {
            errs.add("line \"" + line + "\" ignored.");
            continue;
          }

          final String val = line.substring(d + 1).trim();
          String name = line.substring(0, d).trim();

          // extract numeric value in key
          int num = 0;
          final int ss = name.length();
          for(int s = 0; s < ss; ++s) {
            if(Character.isDigit(name.charAt(s))) {
              num = Strings.toInt(name.substring(s));
              name = name.substring(0, s);
              break;
            }
          }

          if(local) {
            // cache local options as global options
            Prop.put(DBPREFIX + name.toLowerCase(Locale.ENGLISH), val);
          } else {
            try {
              assign(name, val, num, true);
              read.add(name);
            } catch(final BaseXException ex) {
              errs.add(ex.getMessage());
            }
          }
        }
      } catch(final IOException ex) {
        errs.add("file could not be parsed.");
        Util.errln(ex);
      }
    }

    // check if all mandatory files have been read
    boolean ok = true;
    if(errs.isEmpty()) {
      try {
        for(final Option<?> opt : options(getClass())) {
          if(ok && !(opt instanceof Comment)) ok = read.contains(opt.name());
        }
      } catch(final IllegalAccessException ex) {
        throw Util.notExpected(ex);
      }
    }

    if(!ok || !exists || !errs.isEmpty()) {
      write();
      errs.add("writing new configuration file.");
      for(final String s : errs) Util.errln(file + ": " + s);
    }
  }

  /**
   * Assigns the specified name and value.
   * @param name name of option
   * @param value value to be assigned
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private synchronized void assign(final String name, final Value value, final InputInfo info)
      throws QueryException {

    final Option<?> option = definitions.get(name);
    if(option == null) {
      if(getClass() == Options.class || name.startsWith("Q{")) return;
      throw INVALIDOPTION_X.get(info, similar(name));
    }

    final Item item = value.isItem() ? (Item) value : null;
    final SeqType st = value.seqType();
    final QueryFunction<Object, QueryException> expected = type ->
      INVALIDOPTION_X_X_X_X.get(info, name, type, st, value);

    Object result = null;
    if(option instanceof ValueOption) {
      final SeqType est = ((ValueOption) option).seqType();
      if(!st.instanceOf(est)) throw expected.apply(est);
      result = value;
    } else if(option instanceof BooleanOption) {
      final Boolean b = item != null ? Strings.toBoolean(string(item.string(info))) : null;
      if(b == null) throw expected.apply(AtomType.BOOLEAN);
      result = b.booleanValue();
    } else if(option instanceof NumberOption) {
      if(item == null) throw expected.apply(AtomType.INTEGER);
      result = (int) item.itr(info);
    } else if(option instanceof StringOption) {
      result = serialize(value, info);
    } else if(option instanceof StringsOption) {
      final StringList list = new StringList();
      for(final Item it :  value) list.add(serialize(it, info));
      result = list.finish();
    } else if(option instanceof NumbersOption) {
      final IntList list = new IntList();
      for(final Item it :  value) list.add(Strings.toInt(string(it.string(info))));
      result = list.finish();
    } else if(option instanceof EnumOption) {
      final String string = normalize(serialize(value, info));
      final EnumOption<?> eo = (EnumOption<?>) option;
      result = eo.get(string);
      if(result == null) throw INVALIDOPTION_X.get(info, allowed(eo, string, (Object[]) eo.values()));
    } else if(option instanceof OptionsOption) {
      if(!(item instanceof XQMap)) throw expected.apply(SeqType.MAP);
      result = ((OptionsOption<?>) option).newInstance();
      ((Options) result).assign((XQMap) item, info);
    }
    put(option, result);
  }

  /**
   * Normalizes an enumeration value.
   * @param value value
   * @return normalized value
   */
  private static synchronized String normalize(final String value) {
    final String v = value.trim();
    if(v.isEmpty()) return YesNoOmit.OMIT.toString();
    final Boolean b = Strings.toBoolean(v);
    return b == Boolean.TRUE ? Text.YES : b == Boolean.FALSE ? Text.NO : v;
  }

  /**
   * Assigns the specified name and value.
   * @param name name of option
   * @param value value to be assigned
   * @param index index of an array value (optional, can be {@code -1})
   * @param error raise error if the option is unknown
   * @return success flag
   * @throws BaseXException database exception
   */
  private synchronized boolean assign(final String name, final String value, final int index,
      final boolean error) throws BaseXException {

    final Option<?> option = definitions.get(name);
    if(option == null) {
      if(error) throw new BaseXException(similar(name));
      return false;
    }
    final String err = assign(option, value, index, v -> put(option, v), this);
    if(error && err != null) throw new BaseXException(err);

    return true;
  }

  /**
   * Returns a map representation of the comma-separated options string.
   * @param string options string
   * @return map
   */
  private static Map<String, String> toMap(final String string) {
    final HashMap<String, String> map = new HashMap<>();
    final StringBuilder key = new StringBuilder(), value = new StringBuilder();
    final Runnable add = () -> map.put(key.toString().trim(), value.toString());

    boolean left = true;
    final int sl = string.length();
    for(int s = 0; s < sl; s++) {
      final char ch = string.charAt(s);
      if(left) {
        if(ch == '=') {
          left = false;
        } else {
          key.append(ch);
        }
      } else {
        if(ch == ',') {
          if(s + 1 == sl || string.charAt(s + 1) != ',') {
            add.run();
            key.setLength(0);
            value.setLength(0);
            left = true;
            continue;
          }
          // literal commas are escaped by a second comma
          s++;
        }
        value.append(ch);
      }
    }
    if(!left) add.run();
    return map;
  }
}
