(:~
 : Stop jobs.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/jobs';

import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs';

(:~
 : Stops jobs.
 : @param  $ids  job ids
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/job-stop")
  %rest:query-param("id", "{$ids}")
function dba:job-stop(
  $ids  as xs:string*
) as element(rest:response) {
  dba:job-stop($ids, 'stopped')
};

(:~
 : Discards jobs.
 : @param  $ids  job ids
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/job-discard")
  %rest:query-param("id", "{$ids}")
function dba:job-discard(
  $ids  as xs:string*
) as element(rest:response) {
  dba:job-stop($ids, 'discarded')
};


(:~
 : Stops jobs.
 : @param  $ids     job ids
 : @param  $action  action
 : @return redirection
 :)
declare %private function dba:job-stop(
  $ids     as xs:string*,
  $action  as xs:string
) as element(rest:response) {
  let $params := try {
    $ids ! jobs:stop(.),
    map { 'info': util:info($ids, 'job', $action) }
  } catch * {
    map { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
