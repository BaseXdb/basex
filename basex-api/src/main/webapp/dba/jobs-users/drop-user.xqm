(:~
 : Drop users.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/jobs-users';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs-users';

(:~
 : Drops users.
 : @param  $names  names of users
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/drop-user")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:drop(
  $names  as xs:string*
) {
  cons:check(),
  try {
    $names ! user:drop(.),
    cons:redirect($dba:CAT, map { 'info': 'Dropped users: ' || count($names) })
  } catch * {
    cons:redirect($dba:CAT, map { 'error': $err:description })
  }
};
