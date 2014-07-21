package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DialogExport extends BaseXDialog {
  /** Available encodings. */
  private static final String[] ENCODINGS;
  /** Directory path. */
  private final BaseXTextField path;
  /** Database info. */
  private final BaseXLabel info;
  /** Serialization method. */
  private final BaseXCombo method;
  /** Parameters. */
  private final BaseXTextField mparams;
  /** Encoding. */
  private final BaseXCombo encoding;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Parameters. */
  private final BaseXTextField params;

  // initialize encodings
  static {
    final SortedMap<String, Charset> cs = Charset.availableCharsets();
    ENCODINGS = cs.keySet().toArray(new String[cs.size()]);
  }

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogExport(final GUI main) {
    super(main, EXPORT);

    // create checkboxes
    final BaseXBack p = new BaseXBack(new TableLayout(4, 1, 0, 0));
    p.add(new BaseXLabel(OUTPUT_DIR + COL, true, true).border(0, 0, 6, 0));

    // output label
    BaseXBack pp = new BaseXBack(new TableLayout(1, 2, 8, 0));

    path = new BaseXTextField(main.gopts.get(GUIOptions.INPUTPATH), this);
    path.history(GUIOptions.INPUTS, this);
    pp.add(path);

    final BaseXButton browse = new BaseXButton(BROWSE_D, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    pp.add(browse);
    p.add(pp);

    // provide components for method and encoding
    final MainOptions opts = gui.context.options;
    final SerializerOptions sopts = opts.get(MainOptions.EXPORTER);

    // method (ignore last entry)
    final StringList sl = new StringList();
    for(final SerialMethod sm : SerialMethod.values()) sl.add(sm.name());
    sl.deleteAt(sl.size() - 1);
    method = new BaseXCombo(this, sl.finish());
    method.setSelectedItem(sopts.get(SerializerOptions.METHOD).name());

    mparams = new BaseXTextField(this);
    BaseXLayout.setWidth(mparams, BaseXTextField.DWIDTH * 2 / 3);

    final BaseXBack mth = new BaseXBack(new TableLayout(1, 2, 8, 0));
    mth.add(method);
    mth.add(mparams);

    encoding = new BaseXCombo(this, ENCODINGS);
    String enc = sopts.get(SerializerOptions.ENCODING);
    boolean f = false;
    for(final String s : ENCODINGS) f |= s.equals(enc);
    if(!f) {
      enc = enc.toUpperCase(Locale.ENGLISH);
      for(final String s : ENCODINGS) f |= s.equals(enc);
    }
    encoding.setSelectedItem(f ? enc : sopts.get(SerializerOptions.ENCODING));

    params = new BaseXTextField(sopts.toString(), this);
    params.setToolTipText(tooltip(Serializer.OPTIONS));

    pp = new BaseXBack(new TableLayout(3, 2, 16, 6)).border(8, 0, 8, 0);
    pp.add(new BaseXLabel(METHOD + COL, true, true));
    pp.add(mth);
    pp.add(new BaseXLabel(ENCODING + COL, true, true));
    pp.add(encoding);
    pp.add(new BaseXLabel(PARAMETERS + COL, true, true));
    pp.add(params);
    p.add(pp);
    info = new BaseXLabel(" ").border(8, 0, 0, 0);
    p.add(info);

    // indentation
    set(p, BorderLayout.CENTER);

    // buttons
    pp = new BaseXBack(new BorderLayout());
    buttons = okCancel();
    pp.add(buttons, BorderLayout.EAST);
    set(pp, BorderLayout.SOUTH);

    action(method);
    finish(null);
  }

  /**
   * Creates an encoding combo box and selects the specified encoding.
   * @param dialog dialog reference
   * @param encoding original encoding
   * @return combo box
   */
  static BaseXCombo encoding(final BaseXDialog dialog, final String encoding) {
    final BaseXCombo cb = new BaseXCombo(dialog, ENCODINGS);
    boolean f = false;
    String enc = encoding == null ? Token.UTF8 : encoding;
    for(final String s : ENCODINGS) f |= s.equals(enc);
    if(!f) {
      enc = enc.toUpperCase(Locale.ENGLISH);
      for(final String s : ENCODINGS) f |= s.equals(enc);
    }
    cb.setSelectedItem(enc);
    return cb;
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   */
  private void choose() {
    final IOFile io = new BaseXFileChooser(CHOOSE_DIR, path.getText(), gui).select(Mode.DOPEN);
    if(io != null) path.setText(io.path());
  }

  /**
   * Returns the chosen XML file or directory path.
   * @return file or directory
   */
  public String path() {
    return path.getText().trim();
  }

  @Override
  public void action(final Object comp) {
    final String pth = path();
    final IOFile io = new IOFile(pth);
    String inf = io.isDir() && io.children().length > 0 ? DIR_NOT_EMPTY : null;
    ok = !pth.isEmpty();

    final SerialMethod mth = SerialMethod.valueOf(method.getSelectedItem());
    final OptionsOption<? extends Options> opts =
        mth == SerialMethod.JSON ? SerializerOptions.JSON :
        mth == SerialMethod.CSV ? SerializerOptions.CSV : null;
    final boolean showmparams = opts != null;
    mparams.setEnabled(showmparams);

    if(ok) {
      gui.gopts.set(GUIOptions.INPUTPATH, pth);
      try {
        if(comp == method) {
          if(showmparams) {
            final Options mopts = options(null).get(opts);
            mparams.setToolTipText(tooltip(mopts));
            mparams.setText(mopts.toString());
          } else {
            mparams.setToolTipText(null);
            mparams.setText("");
          }
        }
        Serializer.get(new ArrayOutput(), options(mth));
      } catch(final IOException ex) {
        ok = false;
        inf = ex.getMessage();
      }
    }

    info.setText(inf, ok ? Msg.WARN : Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;

    final SerialMethod mth = SerialMethod.valueOf(method.getSelectedItem());
    try {
      gui.set(MainOptions.EXPORTER, options(mth));
    } catch(final BaseXException ex) { throw Util.notExpected(ex); }

    super.close();
    path.store();
  }

  /**
   * Returns the current serialization options.
   * @param mth consider specified serialization method (may be {@code null})
   * @return options
   * @throws BaseXException database exception
   */
  private SerializerOptions options(final SerialMethod mth) throws BaseXException {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.parse(params.getText());
    sopts.set(SerializerOptions.METHOD, SerialMethod.valueOf(method.getSelectedItem()));
    sopts.set(SerializerOptions.ENCODING, encoding.getSelectedItem());
    if(mth == SerialMethod.JSON) {
      final JsonSerialOptions jopts = new JsonSerialOptions();
      jopts.parse(mparams.getText());
      sopts.set(SerializerOptions.JSON, jopts);
    } else if(mth == SerialMethod.CSV) {
      final CsvOptions copts = new CsvOptions();
      copts.parse(mparams.getText());
      sopts.set(SerializerOptions.CSV, copts);
    }
    return sopts;
  }

  /**
   * Returns a tooltip for the specified options string.
   * @param opts serialization options
   * @return string
   */
  private static String tooltip(final Options opts) {
    final StringBuilder sb = new StringBuilder("<html><b>").append(PARAMETERS).append(":</b><br>");
    for(final Option<?> so : opts) {
      if(!(so instanceof OptionsOption)) sb.append("\u2022 ").append(so).append("<br/>");
    }
    return sb.append("</html>").toString();
  }
}
