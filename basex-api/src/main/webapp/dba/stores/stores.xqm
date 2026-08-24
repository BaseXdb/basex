(:~
 : Stores: the key/value stores of the server, their entries and their values.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/stores';

import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace panels = 'dba/lib/stores-panels' at 'panels.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'stores';

(:~
 : Stores: the stores, the entries of the selected one, and the value that is looked at.
 : @param  $name   selected store
 : @param  $path   path of the shown level
 : @param  $key    selected key
 : @param  $info   info string
 : @param  $error  error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/stores')
  %rest:query-param('name',  '{$name}', '')
  %rest:query-param('path',  '{$path}')
  %rest:query-param('key',   '{$key}')
  %rest:query-param('info',  '{$info}')
  %rest:query-param('error', '{$error}')
  %output:method('html')
function dba:stores(
  $name   as xs:string,
  $path   as xs:string?,
  $key    as xs:string?,
  $info   as xs:string?,
  $error  as xs:string?
) as element(html) {
  (: the default store is named by the empty string, and is what the view opens with. The level
     that is listed is the store, unless the address names a deeper one; within the store, the
     entry that is looked at is chosen, and the first one if the address names none :)
  let $level := panels:steps($path)
  let $selected := if (empty($level)) { $key[.] otherwise panels:first-key($name) }
  let $value := panels:value($name, ($level, $selected))
  (: a panel is labelled in the markup, not by its heading: it keeps its name while it is
     folded away, and while it has nothing to show and is hidden :)
  return (
    <div class='panel no-divider' data-label='Stores'>
      <div id='stores-panel' class='pane'>{ panels:stores('', 1, $name) }</div>
    </div>,
    <div class='panel no-divider' data-label='Entries'>
      <div id='entries-panel' class='pane'>{
        panels:entries($name, $level, '', 1, $selected)
      }</div>
    </div>,
    (: the editor is created once and outlives the panel above it, which is redrawn. The block
       above it is no pane: a pane claims its share of the height, which the editor needs :)
    <div class='panel no-divider{ ' hidden'[not($value?exists)] }' data-label='Value'>
      <div id='value-panel'>{ panels:value-panel($value) }</div>
      <textarea id='editor' spellcheck='false'>{ $value?text }</textarea>
    </div>
  ) => html:wrap({
    'header' : $dba:CAT,
    'columns': ('30fr', '35fr', '35fr'),
    'rows'   : '1fr',
    'scripts': ('cm6', 'editor', 'stores'),
    'init'   : 'initStores(' || ($value?editable = true()) || ');',
    'info'   : $info,
    'error'  : $error
  })
};

(:~
 : Replaces the value that the path leads to with the result of the edited expression.
 : @param  $name   store
 : @param  $path   path of the value
 : @param  $query  expression that yields the new value
 : @return empty output
 :)
declare
  %updating
  %rest:POST('{$query}')
  %rest:path('/dba/store-save')
  %rest:query-param('name', '{$name}')
  %rest:query-param('path', '{$path}')
  %output:method('text')
function dba:store-save(
  $name   as xs:string?,
  $path   as xs:string,
  $query  as xs:string?
) {
  dba:put($name, panels:steps($path), utils:evaluate($query)),
  update:output('')
};

(:~
 : Runs a store action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/stores/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'add': fn($args) {
      (: the path names what is written: its first step is the key of the entry, the rest
         leads into its value. The dialog of a level supplies the step it adds :)
      let $steps := panels:steps($args?path)
      let $path := (
        $steps,
        if ($args?index = 'true') {
          xs:integer($args?step)
        } else {
          dba:key($args?step, empty($steps))
        }
      )
      return {
        'params': dba:selection($args, head($path)),
        'info'  : utils:info($path[last()], 'entry', 'added'),
        (: the value is any XQuery value: it is supplied as the expression that yields it :)
        'run'   : %updating fn() { dba:add($args?name, $path, utils:evaluate($args?value)) }
      }
    },
    'remove': fn($args) {
      (: the checked children are removed from the level the path leads to; the entries of a
         store are removed from the store itself :)
      let $path := panels:steps($args?path)
      let $steps := $args?step ! panels:steps(.)
      let $key := head($path)
      return {
        (: the entries that were removed are gone; the level they belonged to is not :)
        'params': dba:selection($args, ()),
        'info'  : utils:info($steps, 'entry', 'removed'),
        'run'   : %updating fn() {
          if (empty($path)) {
            $steps ! store:remove(string(.), $args?name)
          } else {
            let $value := store:get($key, $args?name)
            let $level := panels:remove(panels:resolve($value, tail($path)), $steps)
            return store:put($key, panels:replace($value, tail($path), $level), $args?name)
          }
        }
      }
    },
    'write': fn($args) { {
      'params': { 'name': $args?name },
      'info'  : utils:info($args?name, 'store', 'written to disk'),
      'run'   : %updating fn() { store:write($args?name) }
    } },
    'read': fn($args) { {
      'params': { 'name': $args?name },
      'info'  : utils:info($args?name, 'store', 'read from disk'),
      'run'   : %updating fn() { store:read($args?name) }
    } },
    'close': fn($args) { {
      'params': { 'name': $args?name },
      'info'  : utils:info($args?name, 'store', 'closed'),
      'run'   : %updating fn() { store:close($args?name) }
    } },
    'delete': fn($args) { {
      'info': utils:info($args?name, 'store', 'deleted'),
      (: the selection is not passed on: the store it named is gone :)
      'run' : %updating fn() { $args?name ! store:delete(.) }
    } },
    'clear': fn($args) { {
      'info': 'All stores were cleared.',
      'run' : %updating fn() { store:clear() }
    } }
  })
};

(:~
 : Returns the parameters that reproduce the level an action was started from.
 : @param  $args  request parameters
 : @param  $key   entry to be selected (can be empty)
 : @return query parameters
 :)
declare %private function dba:selection(
  $args  as map(*),
  $key   as xs:string?
) as map(*) {
  let $path := $args?path[.]
  return map:merge((
    { 'name': $args?name },
    if ($path) { { 'path': $path } } else { { 'key': $key }[$key] }
  ))
};

(:~
 : Writes a value that no entry holds yet.
 : @param  $name   store
 : @param  $path   path of the value
 : @param  $value  new value
 :)
declare %private function dba:add(
  $name   as xs:string?,
  $path   as item()*,
  $value  as item()*
) as empty-sequence() {
  (: a path that resolves leads to an entry that is there; a position past the last one
     resolves to nothing, so a child of a sequence or an array is always appended :)
  if (exists(panels:resolve(store:get(head($path), $name), tail($path)))) {
    error((), 'Entry already exists: ' || $path[last()] || '.')
  } else {
    dba:put($name, $path, $value)
  }
};

(:~
 : Writes a value to the path that addresses it, rebuilding the entry it belongs to.
 : @param  $name   store
 : @param  $path   path of the value
 : @param  $value  new value
 :)
declare %private function dba:put(
  $name   as xs:string?,
  $path   as item()*,
  $value  as item()*
) as empty-sequence() {
  let $key := head($path), $steps := tail($path)
  return store:put($key, if (empty($steps)) {
    $value
  } else {
    panels:replace(store:get($key, $name), $steps, $value)
  }, $name)
};

(:~
 : Returns the key that an expression yields. A map is keyed by any atomic value, so the key
 : is supplied the way the value is.
 : @param  $query  expression that yields the key
 : @param  $entry  whether the key names an entry of the store
 : @return key
 :)
declare %private function dba:key(
  $query  as xs:string?,
  $entry  as xs:boolean
) as xs:anyAtomicType {
  let $key := utils:evaluate($query)
  return if (count($key) != 1 or not($key instance of xs:anyAtomicType)) {
    error((), 'The key must be a single atomic value.')
  } else if ($entry and not($key instance of xs:string)) {
    error((), 'An entry of a store is named by a string.')
  } else {
    $key
  }
};
