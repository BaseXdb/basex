(:~
 : Delete log files.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Deletes database logs.
 : @param  $names  names of log files
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/log-delete")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:drop(
  $names  as xs:string*
) as element(rest:response) {
  cons:check(),
  try {
    $names ! admin:delete-logs(.),
    web:redirect($dba:CAT, map { 'info': util:info($names, 'log', 'deleted') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
