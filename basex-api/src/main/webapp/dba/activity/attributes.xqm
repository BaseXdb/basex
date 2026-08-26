(:~
 : Attribute actions of the activity view. Sessions and WebSocket connections are addressed by
 : an id and hold named values, so both are served by the same code; what tells them apart is
 : what is asked of the server for them.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/attributes';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

(:~
 : Returns the value of a session attribute, as the expression that yields it again.
 : @param  $id    session id
 : @param  $name  attribute name
 : @return expression, and the reason why it cannot be edited
 :)
declare
  %rest:path('/dba/session-value')
  %rest:query-param('id',   '{$id}')
  %rest:query-param('name', '{$name}')
  %output:method('json')
function dba:session-value(
  $id    as xs:string,
  $name  as xs:string
) as map(*) {
  (: an attribute that is gone by now is supplied afresh: the dialog opens empty :)
  dba:value(try { sessions:get($id, $name) } catch sessions:not-found { })
};

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
  (: an attribute of a connection that is gone by now is supplied afresh :)
  dba:value(try { ws:get($id, $name) } catch ws:not-found { })
};

(:~
 : Returns an attribute value as the expression that yields it again.
 : @param  $value  value of the attribute
 : @return expression, and the reason why it cannot be edited
 :)
declare %private function dba:value(
  $value  as item()*
) as map(*) {
  let $expression := utils:expression($value)
  return if ($expression?truncated) {
    { 'text': '', 'note': 'The value is too large to be shown; supply a new one.' }
  } else {
    { 'text': $expression?text, 'note': '' }
  }
};

(:~
 : Runs a session action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/sessions/{$action}')
function dba:session-action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, dba:actions('session',
    sessions:set#3, sessions:delete#2, sessions:close#1))
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
function dba:websocket-action(
  $action  as xs:string
) {
  (: the client of a closed connection opens a new one with its next request :)
  utils:dispatch($dba:CAT, $action, dba:actions('connection',
    ws:set#3, ws:delete#2, ws:close#1))
};

(:~
 : Returns the actions that assign, delete and close what holds attributes.
 : @param  $noun    name of the holder (singular form)
 : @param  $set     assigns an attribute
 : @param  $delete  deletes an attribute
 : @param  $close   closes a holder
 : @return actions
 :)
declare %private function dba:actions(
  $noun    as xs:string,
  $set     as fn(xs:string, xs:string, item()*) as empty-sequence(),
  $delete  as fn(xs:string, xs:string) as empty-sequence(),
  $close   as fn(xs:string) as empty-sequence()
) as map(*) {
  (: the calls are voided: what a dynamic call returns is not known to be empty, and an
     updating function admits nothing else :)
  {
    (: an attribute holds any XQuery value: it is supplied as the expression that yields it :)
    'set': fn($args) { {
      'info': utils:info($args?name, 'attribute', 'assigned'),
      'run' : %updating fn() {
        void($set($args?id, $args?name, utils:evaluate($args?value)))
      }
    } },
    'delete': fn($args) {
      (: an attribute is addressed by what holds it, which is of no interest of its own; the
         row of a holder that holds nothing names no attribute to delete :)
      let $ids := $args?id[substring-after(., '|')]
      return {
        'info': utils:info($ids ! substring-after(., '|'), 'attribute', 'deleted'),
        'run' : %updating fn() {
          void($ids ! $delete(substring-before(., '|'), substring-after(., '|')))
        }
      }
    },
    'close': fn($args) {
      (: a holder is closed as a whole, however many of its attributes were checked :)
      let $ids := distinct-values($args?id ! substring-before(., '|'))
      return {
        'info': utils:info($ids, $noun, 'closed'),
        'run' : %updating fn() { void($ids ! $close(.)) }
      }
    }
  }
};
