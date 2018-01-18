(:~
 : Delete log files.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/databases';

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
function dba:drop(
  $names  as xs:string*
) as element(rest:response) {
  try {
    $names ! admin:delete-logs(.),
    web:redirect($dba:CAT, map { 'info': util:info($names, 'log', 'deleted') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
