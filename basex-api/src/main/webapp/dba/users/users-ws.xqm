(:~
 : Refresh the panels of the users view via WebSockets.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/users-ws';

import module namespace panels = 'dba/lib/user-panels' at 'panels.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Handles a message of the users view.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/users', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  let $name := string($json?name)
  return switch ($json?type) {
    case 'users'       return utils:ws-panel('users-panel',
      panels:users(string($json?sort)[.] otherwise 'name', $name))
    (: what is pushed are the contents of the form that submits them; the form itself is the
       panel, and stays where it is :)
    case 'user'        return utils:ws-panel('user-panel', panels:user($name, (), ()))
    case 'permissions' return utils:ws-panel('permissions-panel',
      panels:local-permissions($name))
    default            return error((), 'Unknown message type: ' || $json?type)
  }
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba/users', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Users', $message)
};
