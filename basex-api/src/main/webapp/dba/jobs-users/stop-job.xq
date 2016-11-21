(:~
 : Stops jobs.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/jobs-users';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs-users';

(:~
 : Stops jobs.
 : @param  $id  session ids
 :)
declare
  %rest:GET
  %rest:path("/dba/stop-job")
  %rest:query-param("id", "{$ids}")
  %output:method("html")
function dba:drop(
  $ids  as xs:string*
) {
  cons:check(),
  try {
    util:eval("$i ! jobs:stop(.)", map { 'i': $ids }),
    web:redirect($dba:CAT, map { 'info': 'Stopped jobs: ' || count($ids) })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
