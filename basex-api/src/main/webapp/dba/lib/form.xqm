(:~
 : Form controls.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace form = 'dba/lib/form';

import module namespace config = 'dba/lib/config' at 'config.xqm';

(:~
 : Creates an option checkbox.
 : @param  $value  value
 : @param  $label  label
 : @param  $opts   checked options
 : @return checkbox
 :)
declare function form:option(
  $value  as xs:string,
  $label  as xs:string,
  $opts   as xs:string*
) as node()+ {
  form:checkbox('opts', $value, $opts = $value, $label)
};

(:~
 : Creates a checkbox.
 : @param  $name     name of checkbox
 : @param  $value    value
 : @param  $checked  checked state
 : @param  $label    label
 : @return checkbox
 :)
declare function form:checkbox(
  $name     as xs:string,
  $value    as xs:string,
  $checked  as xs:boolean,
  $label    as xs:string
) as node()+ {
  element label {
    element input {
      attribute type { 'checkbox' },
      attribute name { $name },
      attribute value { $value },
      attribute checked { }[$checked]
    },
    text { $label }
  },
  element br { }
};

(:~
 : Creates a button.
 : @param  $action   button action
 : @param  $label    label
 : @param  $options  options: 'CONFIRM' (ask before the action is run), 'CHECK' (consider checkboxes)
 : @return button
 :)
declare function form:button(
  $action   as xs:string,
  $label    as xs:string,
  $options  as enum('CONFIRM', 'CHECK')* := ()
) as element(button) {
  <button>{
    attribute formaction { $action }[$action],
    attribute onclick { 'return confirmAction(this, "' || $label || '");' }[$options = 'CONFIRM'],
    attribute data-check { 'check' }[$options = 'CHECK'],
    $label
  }</button>
};

(:~
 : Creates a labeled form field.
 : @param  $label    field label
 : @param  $control  input control and supplementary content
 : @param  $class    additional class, e.g. 'stacked' for labels above their control
 : @return field
 :)
declare function form:field(
  $label    as xs:string,
  $control  as item()*,
  $class    as xs:string? := ()
) as element(div) {
  <div class='field{ $class ! (' ' || .) }'>{
    <span>{ $label }</span>,
    <div>{ $control }</div>
  }</div>
};

(:~
 : Creates a modal dialog: a form that needs more room than a prompt can offer. It is submitted
 : like any other form, so the action it posts to redirects back to the page it was opened from.
 : @param  $id      id of the dialog; opened by the client with showDialog
 : @param  $title   heading of the dialog; its buttons are the OK and Cancel of every dialog
 : @param  $action  action the form posts to
 : @param  $upload  whether the dialog submits files
 : @param  $fields  form fields
 : @return dialog
 :)
declare function form:dialog(
  $id      as xs:string,
  $title   as xs:string,
  $action  as xs:string,
  $upload  as xs:boolean,
  $fields  as node()*
) as element(dialog) {
  <dialog id='{ $id }-dialog'>
    <form method='post' action='{ $action }' autocomplete='off'>{
      attribute enctype { 'multipart/form-data' }[$upload],
      attribute onsubmit { 'uploading(this);' }[$upload],
      <h2>{ $title }</h2>,
      $fields,
      <div class='buttons'>{
        <button>OK</button>,
        (: 'dialog' closes the dialog instead of submitting it: native, and needs no script :)
        <button formmethod='dialog' formnovalidate=''>Cancel</button>
      }</div>
    }</form>
  </dialog>
};

(:~ Index options that can be assigned when a database is created and optimized. :)
declare %private variable $form:INDEX-OPTIONS := ('textindex', 'attrindex', 'tokenindex',
  'ftindex', 'stemming', 'casesens', 'diacritics');

(:~
 : Creates the index options of a database dialog. Kept next to form:index-map, which turns the
 : same options into the arguments of the database operation.
 : @param  $opts    checked options
 : @param  $lang    language
 : @param  $create  include the options that are reserved for new databases
 : @return form fields
 :)
declare function form:index-options(
  $opts    as xs:string*,
  $lang    as xs:string?,
  $create  as xs:boolean
) as node()+ {
  <h3>{ form:option('textindex', 'Text Index', $opts) }</h3>,
  <h3>{ form:option('attrindex', 'Attribute Index', $opts) }</h3>,
  <h3>{ form:option('tokenindex', 'Token Index', $opts) }</h3>,
  form:option('updindex', 'Incremental Indexing', $opts)[$create],
  <h3>{ form:option('ftindex', 'Fulltext Index', $opts) }</h3>,
  form:option('stemming', 'Stemming', $opts),
  form:option('casesens', 'Case Sensitivity', $opts),
  form:option('diacritics', 'Diacritics', $opts),
  form:field('Language:', <input type='text' name='lang' value='{ $lang }'/>)
};

(:~
 : Returns the index options of a database dialog as database options.
 : @param  $opts    checked options
 : @param  $lang    language
 : @param  $create  include the options that are reserved for new databases
 : @return database options
 :)
declare function form:index-map(
  $opts    as xs:string*,
  $lang    as xs:string?,
  $create  as xs:boolean
) as map(*) {
  map:merge((
    ($form:INDEX-OPTIONS, 'updindex'[$create]) ! map:entry(., $opts = .),
    $lang ! map:entry('language', .)
  ))
};

(:~
 : Creates a chooser for the current directory. Its selected value is the resolved directory:
 : the client stores it and sends it back with its next request.
 : @param  $dir  current directory
 : @return chooser
 :)
declare function form:directory(
  $dir  as xs:string
) as element(select) {
  <select id='dir' class='wide directory' onchange='changeDir(this.value)'>{
      let $dir-path := fn($path) {
        try {
          file:path-to-native($path)
        } catch file:* { }
      }
      let $webapp := $dir-path(db:option('webpath'))[.]
      let $options := (
        [ 'DBA'       , $config:DBA-DIR ],
        [ 'Webapp'    , $webapp ],
        [ 'RESTXQ'    , $dir-path($webapp ! file:resolve-path(db:option('restxqpath'), .)) ],
        [ 'Repository', $dir-path(db:option('repopath')) ],
        [ 'Home'      , Q{org.basex.util.Prop}HOMEDIR() ],
        [ 'Working'   , file:current-dir() ],
        [ 'Temporary' , file:temp-dir() ],
        file:list-roots() ! [ 'Root', string(.) ],
        [ 'Current'   , $dir ]
      )
      let $selected := head(
        for $option at $pos in $options
        where $option(2) = $dir
        return $pos
      )
      for $option at $pos in $options
      let $name := $option(1), $path := $option(2)
      where $path
      return element option {
        attribute value { $path },
        attribute selected { }[$pos = $selected],
        $path[.] ! (($name || ': ')[$name] || .)
      }
    }</select>
};
