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
  utils:value(try { sessions:get($id, $name) } catch sessions:not-found { })
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
  utils:value(try { ws:get($id, $name) } catch ws:not-found { })
};

(:~
 : Assigns a session attribute.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/sessions/set')
function dba:session-set() {
  utils:dispatch($dba:CAT, dba:set(?, sessions:set#3))
};

(:~
 : Deletes a session attribute.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/sessions/delete')
function dba:session-delete() {
  utils:dispatch($dba:CAT, dba:delete(?, sessions:delete#2))
};

(:~
 : Closes sessions and WebSocket connections.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/clients/close')
function dba:clients-close() {
  (: the client of a closed connection opens a new one with its next request :)
  utils:dispatch($dba:CAT, fn($args) {
    {
      'info': utils:info(($args?session, $args?websocket), 'client', 'closed'),
      'run' : %updating fn() {
        void(($args?websocket ! ws:close(.), $args?session ! sessions:close(.)))
      }
    }
  })
};

(:~
 : Assigns a WebSocket attribute.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/websockets/set')
function dba:websocket-set() {
  utils:dispatch($dba:CAT, dba:set(?, ws:set#3))
};

(:~
 : Deletes a WebSocket attribute.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/websockets/delete')
function dba:websocket-delete() {
  utils:dispatch($dba:CAT, dba:delete(?, ws:delete#2))
};

(: the functions below void their calls: what a dynamic call returns is not known to be empty,
   and an updating function admits nothing else :)

(:~
 : Returns the action that assigns an attribute.
 : @param  $args  request parameters
 : @param  $set   assigns an attribute
 : @return action
 :)
declare %private function dba:set(
  $args  as map(*),
  $set   as fn(xs:string, xs:string, item()*) as empty-sequence()
) as utils:action {
  (: an attribute holds any XQuery value: it is supplied as the expression that yields it :)
  {
    'info': utils:info($args?name, 'attribute', 'saved'),
    'run' : %updating fn() {
      void($set($args?id, $args?name, utils:evaluate($args?value)))
    }
  }
};

(:~
 : Returns the action that deletes an attribute.
 : @param  $args    request parameters
 : @param  $delete  deletes an attribute
 : @return action
 :)
declare %private function dba:delete(
  $args    as map(*),
  $delete  as fn(xs:string, xs:string) as empty-sequence()
) as utils:action {
  {
    'info': utils:info($args?name, 'attribute', 'deleted'),
    'run' : %updating fn() { void($delete($args?id, $args?name)) }
  }
};
