(:~
 : Drop users.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/users';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Drops users.
 : @param  $names  names of users
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/user-drop")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:user-drop(
  $names  as xs:string*
) as empty-sequence() {
  cons:check(),
  try {
    $names ! user:drop(.),
    cons:redirect($dba:CAT, map { 'info': util:info($names, 'user', 'dropped') })
  } catch * {
    cons:redirect($dba:CAT, map { 'error': $err:description })
  }
};
