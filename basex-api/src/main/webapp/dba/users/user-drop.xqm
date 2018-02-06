(:~
 : Drop users.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/users';

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
function dba:user-drop(
  $names  as xs:string*
) as empty-sequence() {
  try {
    $names ! user:drop(.),
    util:redirect($dba:CAT, map { 'info': util:info($names, 'user', 'dropped') })
  } catch * {
    util:redirect($dba:CAT, map { 'error': $err:description })
  }
};
