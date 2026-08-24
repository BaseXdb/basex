(:~
 : WebSocket actions of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/websockets';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

(:~
 : Returns the value of a WebSocket attribute, as the expression that yields it again.
 : @param  $id    WebSocket id
 : @param  $name  attribute name
 : @return expression, and the reason why it cannot be edited
 :)
declare
  %rest:path('/dba/websocket-value')
  %rest:query-param('id',   '{$id}')
  %rest:query-param('name', '{$name}')
  %output:method('json')
function dba:websocket-value(
  $id    as xs:string,
  $name  as xs:string
) as map(*) {
  (: an attribute of a connection that is gone by now is supplied afresh: the dialog opens
     empty :)
  let $value := try { ws:get($id, $name) } catch ws:not-found { }
  let $expression := utils:expression($value)
  return if ($expression?truncated) {
    { 'text': '', 'note': 'The value is too large to be shown; supply a new one.' }
  } else {
    { 'text': $expression?text, 'note': '' }
  }
};

(:~
 : Runs a WebSocket action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/websockets/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    (: an attribute holds any XQuery value: it is supplied as the expression that yields it :)
    'set': fn($args) { {
      'info': utils:info($args?name, 'attribute', 'assigned'),
      'run' : %updating fn() {
        ws:set($args?id, $args?name, utils:evaluate($args?value))
      }
    } },
    'delete': fn($args) { {
      (: an attribute is addressed by the connection that holds it, which is of no interest
         of its own :)
      'info': utils:info($args?id ! substring-after(., '|'), 'attribute', 'deleted'),
      'run' : %updating fn() {
        $args?id ! ws:delete(substring-before(., '|'), substring-after(., '|'))
      }
    } },
    'close': fn($args) {
      (: a connection is closed as a whole, however many of its attributes were checked; the
         client of a closed connection opens a new one with its next request :)
      let $ids := distinct-values($args?id ! substring-before(., '|'))
      return {
        'info': utils:info($ids, 'connection', 'closed'),
        'run' : %updating fn() { $ids ! ws:close(.) }
      }
    }
  })
};
