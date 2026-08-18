(:~
 : Refresh the panels of the stores view via WebSockets.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/stores-ws';

import module namespace panels = 'dba/lib/stores-panels' at 'panels.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Handles a message of the store view.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/stores', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $name := string($json?name)
  let $path := panels:steps($json?path)
  let $sort := string($json?sort)
  let $page := xs:integer($json?page otherwise 1)
  return switch ($json?type) {
    case 'stores'  return utils:ws-panel('stores', panels:stores($sort, $page, $name))
    case 'entries' return utils:ws-panel('entries',
      panels:entries($name, $path, $sort, $page, head(panels:steps($json?selected))))
    case 'value'   return dba:ws-value($name, $path)
    default        return error((), 'Unknown message type: ' || $json?type)
  }
};

(:~
 : Renders the value panel and pushes it, together with the value the editor holds.
 : @param  $name  selected store
 : @param  $path  path of the value
 :)
declare %private function dba:ws-value(
  $name  as xs:string?,
  $path  as item()*
) as empty-sequence() {
  let $value := panels:value($name, $path)
  return utils:ws-send({
    'type'    : 'value',
    'html'    : utils:html(panels:value-panel($value)),
    'text'    : $value?text,
    'editable': $value?editable = true()
  })
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba/stores', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Stores', $message)
};
