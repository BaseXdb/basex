/** CodeMirror editors and their output preferences. */

/** Called when the user changes the editor content; a page registers its own. */
let _editor_changed;
/** Called when a query is to be run (Ctrl-Enter); a page registers its own. */
let _editor_run;
/** Called when the 'Indent' preference changes; a page registers its own. */
let _indent_changed;

/** Link to the CodeMirror editor component. */
let _editor;

/** All editors, by the id of the text area each of them replaced. The editor of record is
    among them; the others are only edited and submitted with their form. */
const _editors = {};

/** Link to the CodeMirror output component. */
let _output;

/** Shortest an auto-resized editor gets, and its height on stacked layouts. */
const EDITOR_MIN_HEIGHT = 200;

/** Height of an editor that is not auto-resized. */
const EDITOR_FIXED_HEIGHT = "300px";

/** localStorage key for the 'Indent' output preference. */
const INDENT_KEY = "dba-indent";

/**
 * Returns the height of the page chrome below <main>: the rule, the footer and
 * the body margin. None of it depends on the editor, so it can be measured.
 * @returns {number} height in pixels
 */
function chromeBelowMain() {
  let height = parseFloat(getComputedStyle(document.body).marginBottom);
  for(let el = document.querySelector("main").nextElementSibling; el; el = el.nextElementSibling) {
    height += el.getBoundingClientRect().height;
  }
  return height;
}

/**
 * Reads the stored 'Indent' output preference.
 * @returns {boolean} whether output should be indented
 */
function indentOn() {
  return localStorage.getItem(INDENT_KEY) === "yes";
}

/**
 * Persists the 'Indent' preference and, in the resource view, re-renders with it.
 */
function indentChanged() {
  localStorage.setItem(INDENT_KEY, document.getElementById("indent").checked ? "yes" : "no");
  // the resource view re-renders immediately; the editor applies it on the next run
  _indent_changed?.();
}

/**
 * Returns the content of the editor. The editor is asked itself: the text area it replaced is
 * only kept in sync while the document is edited, not when a whole state is swapped in.
 * @returns {string} content
 */
function editorValue() {
  return _editor ? _editor.getValue() : document.getElementById("editor").value;
}

/**
 * Assigns the content of one of the editors. Without CodeMirror, all but the first text area
 * are left as they are, and are written to directly.
 * @param {string} id id of the text area the editor replaced
 * @param {string} text new content
 */
function setEditorText(id, text) {
  const editor = _editors[id];
  if(editor) editor.setValue(text);
  else document.getElementById(id).value = text;
}

/**
 * Sets the read-only state of the resource editor (CodeMirror or plain textarea).
 * @param {boolean} readOnly read-only state
 */
function editorReadOnly(readOnly) {
  if(_editor.setOption) _editor.setOption("readOnly", readOnly);
  else document.getElementById("editor").readOnly = readOnly;
}

/**
 * Loads the CodeMirror editor extension.
 * @param {string}  language of main editor (for syntax highlighting)
 * @param {boolean|Array} edit text areas that become editors: true for the one called 'editor',
 *          or a list of ids. The first one that exists is the editor of record, which _editor
 *          and the page-wide helpers refer to; any further one is edited and submitted with its
 *          form, and nothing else reads it
 * @param {boolean} resize resize text areas to maximum height
 */
function loadCodeMirror(language, edit, resize) {
  // CodeMirror 6 is delivered as the self-contained window.CM6 bundle
  // Without it, or on Android, fall back to plain textareas
  const useCM = !!window.CM6 && !/android/i.test(navigator.userAgent);
  if(edit) {
    for(const id of edit === true ? [ "editor" ] : edit) {
      const editorArea = document.getElementById(id);
      if(!editorArea) continue;
      if(_editor) {
        if(useCM) _editors[id] = CM6.fromTextArea(editorArea, { language });
      } else if(useCM) {
        _editor = _editors[id] = CM6.fromTextArea(editorArea, {
          language,
          // Lezer-driven syntax-error gutter, only for the XQuery editor
          parseErrors: language === "xquery",
          extraKeys: [
            { key: "Ctrl-Enter", run: () => (_editor_run?.(), true) },
            { key: "Cmd-Enter",  run: () => (_editor_run?.(), true) }
          ],
          onChange: () => _editor_changed?.()
        });
        // only a real editor can be moved to the position an error message names
        _locate = (line, column) => {
          _editor.setCursor({ line: line - 1, ch: column - 1 });
          _editor.focus();
        };
      } else {
        _editor = _editors[id] = {
          setValue(v) { editorArea.value = v; },
          getValue() { return editorArea.value; },
          clearHistory() {},
          focus() { editorArea.focus(); }
        };
        editorArea.onchange = () => _editor_changed?.();
      }
    }
  }

  // the stored 'Indent' preference belongs to the editors, and is restored with them
  const indent = document.getElementById("indent");
  if(indent) indent.checked = indentOn();

  const outputArea = document.getElementById("output");
  if(outputArea != null) {
    if(useCM) {
      _output = CM6.fromTextArea(outputArea, { language: "xml", readOnly: true });
    } else {
      _output = {
        setValue(v) { outputArea.value = v; }
      };
    }
  }

  if(resize === "fill") {
    // a filled layout gives the editors their share of the panel via CSS
  } else if(resize) {
    const refresh = () => {
      // size each pane from its own top to the viewport bottom, so a tall
      // sibling column (e.g. a long resource list) can't shrink it
      // stacked layouts keep the minimum, so the editor does not fill the
      // viewport and push the output and buttons off-screen
      // measured once: reading it per element would interleave layout and style writes
      const reserve = chromeBelowMain();
      const height = elem => stacked() ? EDITOR_MIN_HEIGHT : Math.max(EDITOR_MIN_HEIGHT,
        window.innerHeight - elem.getBoundingClientRect().top - reserve);
      for(const elem of document.querySelectorAll(useCM ? ".cm-editor" : "textarea")) {
        elem.style.height = `${height(elem)}px`;
      }
    };
    window.addEventListener("load", refresh);
    window.addEventListener("resize", refresh);
  } else if(useCM) {
    // no auto-resize (e.g. the users pages): without a height, the editor
    // collapses to a single line. The textarea fallback is sized by style.css
    for(const elem of document.querySelectorAll(".cm-editor")) {
      // an editor is as tall as the text area it replaced asked to be
      const rows = elem.previousElementSibling?.getAttribute("rows");
      if(rows) {
        // a row is a rendered line of the content, measured where it is laid out; an editor
        // that is still hidden has none, and falls back to the declared line height
        const line = elem.querySelector(".cm-line")?.getBoundingClientRect().height ||
          parseFloat(getComputedStyle(elem.querySelector(".cm-scroller")).lineHeight);
        // the box covers border and padding as well, so both are added to the lines
        const content = getComputedStyle(elem.querySelector(".cm-content")), box =
          getComputedStyle(elem);
        elem.style.height = `${rows * line +
          parseFloat(content.paddingTop) + parseFloat(content.paddingBottom) +
          parseFloat(box.borderTopWidth) + parseFloat(box.borderBottomWidth)}px`;
      } else {
        elem.style.height = EDITOR_FIXED_HEIGHT;
      }
    }
  }
}
