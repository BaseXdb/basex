(:~
 : Delete log files.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Deletes database logs.
 : @param  $names  names of log files
 :)
declare
  %rest:GET
  %rest:path("/dba/delete-logs")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:drop(
  $names  as xs:string*
) as element(rest:response) {
  cons:check(),
  try {
    $names ! admin:delete-logs(.),
    web:redirect($dba:CAT, map { 'info': 'Deleted logs: ' || count($names) })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
