(:~
 : Panels of the stores view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panels = 'dba/lib/stores-panels';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Page the deep links of the panels refer to. :)
declare %private variable $panels:CAT := 'stores';

(:~ Label of the store that is addressed if no name is supplied. :)
declare %private variable $panels:DEFAULT := '(default)';

(:~ Maximum length of the value that is shown in a table cell. :)
declare %private variable $panels:PREVIEW := 100;

(:~
 : Creates the contents of the stores panel: the stores to choose from, and what they hold.
 : @param  $sort  sort key of the store list
 : @param  $page  current page of the store list
 : @param  $name  selected store
 : @return panel contents
 :)
declare function panels:stores(
  $sort  as xs:string,
  $page  as xs:integer,
  $name  as xs:string?
) as element()+ {
  (: a store that holds nothing is not listed; the selected one is kept in view, so that a
     store that is being filled does not vanish from the list it was chosen from :)
  let $names := sort(distinct-values(('', store:list(), $name)), '?lang=en')
  (: the list opens by name, which is the order it is built in :)
  let $sort := $sort[.] otherwise 'label'
  return <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'>
    {
      let $headers := (
        { 'key': 'label', 'label': 'Name', 'type': 'dynamic', 'width': '28%' },
        { 'key': 'entries', 'label': 'Count', 'type': 'number', 'order': 'desc',
          'width': '20%' },
        { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'order': 'desc', 'width': '16%' },
        { 'key': 'modified', 'label': 'Date', 'width': '30%' }
      )
      let $entries :=
        for $store in utils:slice($names, $page, $sort)
        let $info := store:info($store)
        return {
          'name': $store,
          'label': panels:select(($store[.] otherwise $panels:DEFAULT), { 'name': $store },
            $store = $name),
          'entries': $info?entries,
          'size': $info?size,
          (: a store that was never written to disk has no date to show :)
          'modified': ($info?modified ! html:date(.)) otherwise '–'
        }
      let $buttons := (
        <button type='button' onclick='newStore()'>New…</button>,
        form:button('stores/delete', 'Delete', ('CHECK', 'CONFIRM')),
        form:button('stores/clear', 'Clear All', 'CONFIRM')
      )
      let $options := {
        'sort': $sort,
        'page': $page,
        'count': count($names),
        'presort': 'label',
        (: the checkbox submits the name: the label of the default store is not what
           addresses it :)
        'select': 'name',
        (: nothing but the buttons above the list, and they stay in reach :)
        'sticky': ()
      }
      return table:create($headers, $entries, $buttons, {}, $options)
    }
  </form>
};

(:~
 : Creates the contents of the entries panel: the children of the level the path leads to.
 : @param  $name  selected store
 : @param  $path  path of the level; its first step is the key of an entry
 : @param  $sort  sort key of the child list
 : @param  $page  current page of the child list
 : @return panel contents
 :)
declare function panels:entries(
  $name      as xs:string?,
  $path      as item()*,
  $sort      as xs:string,
  $page      as xs:integer,
  $selected  as item()? := ()
) as element()+ {
  let $key := head($path)
  let $root := if ($key) { store:get($key, $name) }
  (: a path that does not resolve any more is given up; the client adopts the one that is
     rendered, which is why it is stated in the markup :)
  let $resolved := if (exists($root)) { panels:resolve($root, tail($path)) }
  let $steps := if (exists($resolved)) { $path } else if (exists($root)) { $key } else { () }
  let $value := $resolved otherwise $root
  let $text := panels:path($steps)
  let $positional := $value instance of array(*) or count($value) > 1
  (: the level opens by what its children are named or numbered by :)
  let $sort := $sort[.] otherwise 'label'
  (: a written value is rebuilt along its path, so a step that names a position cannot be :)
  let $editable := every $step in $steps satisfies not($step instance of map(*))
  let $children := if (empty($steps)) {
    for $entry in store:keys($name)
    return {
      'label': $entry,
      'step': panels:step-text($entry),
      'value': fn() { store:get($entry, $name) }
    }
  } else {
    panels:children($value)
  }
  (: the child that is looked at; the level it belongs to is what the panel lists :)
  let $marked := $selected ! panels:step-text(.)
  return (
    <form method='post' autocomplete='off' data-sort='{ $sort }' data-page='{ $page }'
          data-path='{ $text }' data-selected='{ $marked }'>
      <input type='hidden' name='name' value='{ $name }'/>
      <input type='hidden' name='path' value='{ $text }'/>
      {
        let $headers := (
          (: a position is ordered by its value, not as the string it is labelled by :)
          { 'key': 'label', 'label': 'Key', 'type': 'dynamic', 'width': '30%',
            'sort': 'number'[$positional] },
          { 'key': 'value', 'label': 'Value', 'type': 'dynamic', 'width': '40%' },
          { 'key': 'type', 'label': 'Type', 'type': 'dynamic', 'width': '22%' }
        )
        (: the type and the value of a child are only produced for the rows that are shown:
           a store may hold values that are expensive to inspect :)
        let $entries :=
          for $child in utils:slice($children, $page, $sort)
          return {
            'step': $child?step,
            'label': panels:child($child, $child?step = $marked),
            'type': fn() { type-of($child?value()) },
            (: what the value holds is what opens it :)
            'value': fn() {
              let $value := $child?value()
              let $open := panels:open($child, $value)
              return ($open, ' '[$open], panels:preview($value))
            }
          }
        let $buttons := panels:buttons($editable, empty($steps))
        let $options := {
          'sort': $sort,
          'page': $page,
          'count': count($children),
          (: the checkbox submits the step: the label of a key that is neither a string nor
             an integer is not what addresses it :)
          'select': 'step',
          (: what can be done with the level stays in view while its children scroll; the path
             that leads to it is stated below what acts on it :)
          'sticky': (),
          'below': panels:breadcrumb($root, $steps)
        }
        return table:create($headers, $entries, $buttons, { 'name': $name }, $options)
      }
    </form>,

    <div class='note'>Read-only: all steps of the path must be strings and
      integers.</div>[not($editable)],

    (: the index that follows the last child appends one :)
    panels:add-dialog($name, $text, if ($value instance of array(*)) {
      array:size($value) + 1
    } else if ($positional) {
      count($value) + 1
    })
  )
};

(:~
 : Returns the key of the entry that the panel lists first.
 : @param  $name  store
 : @return key; empty sequence if the store is empty
 :)
declare function panels:first-key(
  $name  as xs:string
) as xs:string? {
  (: sorted as the table sorts the column, or the entry that is looked at would not be the
     one that the first row shows :)
  head(sort(store:keys($name), '?lang=en'))
};

(:~
 : Creates the buttons of a level, and those of the store if the level is the store itself.
 : @param  $editable  whether the level can be written to
 : @param  $store     whether the level is the store
 : @return buttons
 :)
declare %private function panels:buttons(
  $editable  as xs:boolean,
  $store     as xs:boolean
) as element()+ {
  <button type='button' onclick='showDialog("add")'>{
    attribute disabled { }[not($editable)], 'Add…'
  }</button>,
  form:button('stores/remove', 'Remove', ('CHECK', 'CONFIRM'))[$editable],
  if ($store) {
    form:button('stores/write', 'Write'),
    form:button('stores/read', 'Read', 'CONFIRM'),
    form:button('stores/close', 'Close')
  }
};

(:~
 : Creates the dialog that adds a child to the level.
 : @param  $name   selected store
 : @param  $path   path of the level
 : @param  $max    position a child is appended at; empty if children carry a key
 : @return dialog
 :)
declare %private function panels:add-dialog(
  $name  as xs:string?,
  $path  as xs:string,
  $max   as xs:integer?
) as element(dialog) {
  let $index := exists($max)
  return form:dialog('add', 'Add Entry', 'stores/add', false(), (
    <input type='hidden' name='name' value='{ $name }'/>,
    <input type='hidden' name='path' value='{ $path }'/>,
    <input type='hidden' name='index' value='{ $index }'/>,
    (: a position is not asked for: a child is appended to the level it belongs to. A key is
       supplied as an expression, as a map is keyed by any atomic value :)
    if ($index) {
      <input type='hidden' name='step' value='{ $max }'/>
    } else {
      form:field('Key:',
        <input type='text' name='step' placeholder="'key'" required='' autofocus=''/>, 'stacked')
    },
    (: no 'required': the editor hides the text area, and a hidden field that fails validation
       cannot be focused, which would block the submit without telling the user why :)
    form:field('Value:',
      <textarea name='value' id='add-value' class='wide' rows='8'/>, 'stacked')
  ))
};

(:~
 : Returns the value that the path leads to, and what can be done with it.
 : @param  $name  selected store
 : @param  $path  path of the value
 : @return properties: whether the value exists and can be edited, the reason why it cannot,
 :         and its text
 :)
declare function panels:value(
  $name  as xs:string?,
  $path  as item()*
) as map(*) {
  let $key := head($path)
  let $root := if ($key) { store:get($key, $name) }
  let $value := if (exists($root)) { panels:resolve($root, tail($path)) }
  return if (empty($value)) {
    (: the store itself has no value, and a path that is gone leads to none :)
    { 'exists': false(), 'text': '' }
  } else {
    let $max := config:get($config:MAXCHARS)
    (: the value is written as the expression that yields it again, so that what is edited here
       can be stored again; what holds further values is laid out over several lines :)
    let $serialized := serialize($value, {
      'method': 'adaptive', 'expression': true(), 'indent': true(), 'limit': $max * 2 + 1
    })
    (: a single item is an expression of its own: the parentheses that the expression method
       puts around every sequence are dropped for it. A truncated text has lost its closing
       one, and is left as it is :)
    let $text := if (count($value) = 1 and starts-with($serialized, '(') and
        ends-with($serialized, ')')) {
      substring($serialized, 2, string-length($serialized) - 2)
    } else {
      $serialized
    }
    let $truncated := string-length($text) > $max
    (: a written value is rebuilt along its path, so a step that names a position cannot be :)
    let $addressable := every $step in $path satisfies not($step instance of map(*))
    return {
      'exists'   : true(),
      'editable' : $addressable and not($truncated),
      'text'     : if ($truncated) { substring($text, 1, $max) } else { $text },
      (: reason why the value cannot be replaced :)
      'note': if ($truncated) {
        'Read-only: the value is too large for editing.'
      } else if (not($addressable)) {
        'Read-only: all steps of the path must be strings and integers.'
      }
    }
  }
};

(:~
 : Creates the contents of the value panel; the value itself is held by the editor.
 : @param  $value  value properties, as returned by panels:value
 : @return panel contents; empty if the path leads to no value
 :)
declare function panels:value-panel(
  $value  as map(*)
) as element()* {
  if (not($value?exists)) {
    (: nothing is shown: the panel is not shown either, so it needs no placeholder :)
  } else {
    <div class='buttons'>{
      (: enabled by the client once it knows that the value can be edited :)
      <button type='button' id='save-value' onclick='saveValue()' disabled=''>Replace</button>
    }</div>,
    <div class='note warn'>{ $value?note }</div>[$value?note]
  }
};

(:~
 : Creates the path of the shown level; every step but the last leads back to its level.
 : @param  $root  value of the entry (empty for the store itself)
 : @param  $path  path of the level; its first step is the key of an entry
 : @return breadcrumb
 :)
declare %private function panels:breadcrumb(
  $root  as item()*,
  $path  as item()*
) as element(div)? {
  if (empty($path)) {
    (: the top level is where the path starts: there is none to state :)
  } else {
    let $labels := (
      'Root',
      for $step at $pos in $path
      return if ($pos = 1) {
        string($step)
      } else {
        panels:label($root, subsequence(tail($path), 1, $pos - 1))
      }
    )
    return <div class='note ellipsis'>{
      for $label at $pos in $labels
      return (
        (: text nodes, as two adjacent strings would be separated by a space :)
        text { ' » ' }[$pos > 1],
        (: the last step leads to the level that is shown, and nowhere to go :)
        if ($pos = count($labels)) {
          text { $label }
        } else {
          <a href='#' data-depth='{ $pos - 1 }'
             onclick='truncatePath(this.dataset.depth); return false;'>{
            attribute class { 'root' }[$pos = 1],
            $label
          }</a>
        }
      )
    }</div>
  }
};

(:~
 : Returns the label of the last step of a path.
 : @param  $root    value of the entry
 : @param  $prefix  path up to and including the step
 : @return label
 :)
declare %private function panels:label(
  $root    as item()*,
  $prefix  as item()*
) as xs:string {
  let $step := $prefix[last()]
  return if ($step instance of map(*)) {
    let $parent := panels:resolve($root, subsequence($prefix, 1, count($prefix) - 1))
    return string(map:keys($parent)[$step?pos])
  } else {
    string($step)
  }
};

(:~
 : Returns the children of a value, in entry order.
 : @param  $value  value of the level
 : @return label, step and value of every child
 :)
declare %private function panels:children(
  $value  as item()*
) as map(*)* {
  if ($value instance of map(*)) {
    for $key at $pos in map:keys($value)
    (: a key that is neither a string nor an integer is not written down as itself :)
    let $step := if ($key instance of (xs:string|xs:integer)) {
      panels:step-text($key)
    } else {
      '{"pos":' || $pos || '}'
    }
    return { 'label': string($key), 'step': $step, 'value': fn() { $value($key) } }
  } else if ($value instance of array(*)) {
    for $pos in 1 to array:size($value)
    return { 'label': string($pos), 'step': string($pos), 'value': fn() { $value($pos) } }
  } else if (count($value) > 1) {
    (: a sequence is addressed by position, as an array is :)
    for $item at $pos in $value
    return { 'label': string($pos), 'step': string($pos), 'value': fn() { $item } }
  }
};

(:~
 : Removes children from a value.
 : @param  $value  value of the level
 : @param  $steps  steps that lead to the children
 : @return new value
 :)
declare function panels:remove(
  $value  as item()*,
  $steps  as item()*
) as item()* {
  if ($value instance of map(*)) {
    map:remove($value, for $step in $steps return if ($step instance of map(*)) {
      map:keys($value)[$step?pos]
    } else {
      $step
    })
  } else if ($value instance of array(*)) {
    array:remove($value, $steps ! xs:integer(.))
  } else if (count($value) > 1) {
    (: an item is dropped; a level that is left with a single one is that item :)
    $value[not(position() = ($steps ! xs:integer(.)))]
  } else {
    error((), 'Value cannot be descended into: ' || type-of($value))
  }
};

(:~
 : Creates the link of a child: the label that shows its value.
 : @param  $child     label, step and value of the child
 : @param  $selected  whether the child is the one that is shown
 : @return function creating the link
 :)
declare %private function panels:child(
  $child     as map(*),
  $selected  as xs:boolean
) as fn() as element(a) {
  fn() {
    <a href='#' data-step='{ $child?step }'
       onclick='selectChild(this.dataset.step); return false;'>{
      attribute class { 'selected' }[$selected],
      $child?label
    }</a>
  }
};

(:~
 : Creates the marker that opens a child as the level that is listed.
 : @param  $child  label, step and value of the child
 : @param  $value  value of the child
 : @return marker, or empty sequence
 :)
declare %private function panels:open(
  $child  as map(*),
  $value  as item()*
) as element(a)? {
  (: an empty map or array is a level as well: it is where its first child is added :)
  if ($value instance of map(*) or $value instance of array(*) or count($value) > 1) {
    <a href='#' data-step='{ $child?step }' title='Show the entries of this value'
       onclick='descend(this.dataset.step); return false;'>↘</a>
  }
};

(:~
 : Returns the text of a step: what identifies a child of a level.
 : @param  $step  step, or the key of a child
 : @return text
 :)
declare function panels:step-text(
  $step  as item()
) as xs:string {
  if ($step instance of map(*)) {
    '{"pos":' || $step?pos || '}'
  } else if ($step instance of xs:string) {
    (: a name is written as itself; anything else is JSON, whose dots are escaped :)
    if (matches($step, '^\p{L}[\p{L}\p{N}_-]*$')) {
      $step
    } else {
      replace(serialize($step, { 'method': 'json' }), '\.', '\\u002E')
    }
  } else {
    string($step)
  }
};

(:~
 : Returns the text of a path: its steps, separated by dots.
 : @param  $steps  steps
 : @return text
 :)
declare function panels:path(
  $steps  as item()*
) as xs:string {
  (: a dot survives a query string as itself, which a slash does not :)
  string-join($steps ! panels:step-text(.), '.')
};

(:~
 : Returns the steps that the text of a path denotes.
 : @param  $path  path (can be empty)
 : @return steps
 :)
declare function panels:steps(
  $path  as xs:string?
) as item()* {
  (: a step escapes the dots of its own, so every dot that is left separates two of them :)
  for $step in tokenize($path[.], '\.')
  return if (matches($step, '^["{]')) {
    let $json := parse-json($step)
    return if ($json instance of map(*)) { { 'pos': xs:integer($json?pos) } } else { string($json) }
  } else if (matches($step, '^-?\d+$')) {
    xs:integer($step)
  } else {
    $step
  }
};

(:~
 : Returns the value a path leads to.
 : @param  $value  value to descend into
 : @param  $steps  remaining steps
 : @return value; empty sequence if a step does not resolve
 :)
declare function panels:resolve(
  $value  as item()*,
  $steps  as item()*
) as item()* {
  if (empty($steps)) {
    $value
  } else {
    let $step := head($steps)
    let $child := if ($value instance of map(*)) {
      $value(if ($step instance of map(*)) { map:keys($value)[$step?pos] } else { $step })
    } else if ($value instance of array(*)) {
      $value[$step instance of xs:integer and $step = 1 to array:size($value)]($step)
    } else if (count($value) > 1 and $step instance of xs:integer) {
      $value[$step]
    }
    return if (exists($child)) { panels:resolve($child, tail($steps)) }
  }
};

(:~
 : Replaces the value a path leads to, rebuilding every level along it.
 : @param  $value  value to descend into
 : @param  $steps  remaining steps
 : @param  $new    new value
 : @return new value
 :)
declare function panels:replace(
  $value  as item()*,
  $steps  as item()*,
  $new    as item()*
) as item()* {
  if (empty($steps)) {
    $new
  } else {
    let $step := head($steps)
    return if ($value instance of map(*)) {
      map:put($value, $step, panels:replace($value($step), tail($steps), $new))
    } else if ($value instance of array(*)) {
      let $size := array:size($value)
      return if ($step <= $size) {
        array:put($value, $step, panels:replace($value($step), tail($steps), $new))
      } else if ($step = $size + 1) {
        array:append($value, panels:replace((), tail($steps), $new))
      } else {
        error((), 'No such index: ' || $step || '.')
      }
    } else if (count($value) > 1) {
      let $size := count($value)
      return if ($step <= $size + 1) {
        for $pos in 1 to max(($size, $step))
        let $item := $value[$pos]
        return if ($pos = $step) {
          panels:replace($item, tail($steps), $new)
        } else {
          $item
        }
      } else {
        error((), 'No such index: ' || $step || '.')
      }
    } else {
      error((), 'Value cannot be descended into: ' || type-of($value))
    }
  }
};

(:~
 : Creates a link that selects a store; its reference is a deep link naming the selection.
 : @param  $label     link label
 : @param  $params    selection the link refers to
 : @param  $selected  whether the link refers to what is shown
 : @return function creating the link
 :)
declare %private function panels:select(
  $label     as xs:string,
  $params    as map(*),
  $selected  as xs:boolean
) as fn() as element(a) {
  fn() {
    <a href='{ web:create-url($panels:CAT, $params) }'>{
      attribute class { 'selected' }[$selected],
      attribute data-select { $params?name },
      attribute onclick { 'selectStore(this.dataset.select); return false;' },
      $label
    }</a>
  }
};

(:~
 : Returns how much a value holds.
 : @param  $count  number of children
 : @param  $name   name of a child, in singular form
 : @return count
 :)
declare %private function panels:count(
  $count  as xs:integer,
  $name   as xs:string
) as xs:string {
  `{ $count } { if ($count = 1) { $name } else { replace($name, 'y$', 'ie') || 's' } }`
};

(:~
 : Returns the beginning of the serialized value of an entry.
 : @param  $value  value
 : @return the value in a code element, how much a value that holds further values holds, or
 :         nothing for a binary value
 :)
declare %private function panels:preview(
  $value  as item()*
) as item()* {
  let $count := count($value)
  (: how many values there are is what a cell can say about a value that holds further ones :)
  return if ($count > 1) {
    panels:count($count, 'item')
  } else if ($value instance of map(*)) {
    panels:count(map:size($value), 'entry')
  } else if ($value instance of array(*)) {
    panels:count(array:size($value), 'member')
  } else if ($value instance of xs:base64Binary or $value instance of xs:hexBinary) {
    (: nothing: a binary has nothing to read :)
  } else {
    (: serialize more characters than requested, as the limit represents number of bytes :)
    <code>{
      utils:chop(serialize($value, {
        'method': 'basex', 'limit': $panels:PREVIEW * 2 + 1
      }), $panels:PREVIEW)
    }</code>
  }
};
