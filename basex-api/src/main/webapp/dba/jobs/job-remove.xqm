(:~
 : Remove jobs.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/jobs';

import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs';

(:~
 : Removes jobs.
 : @param  $ids  job ids
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/job-remove')
  %rest:query-param('id', '{$ids}')
function dba:job-stop(
  $ids  as xs:string*
) as element(rest:response) {
  let $params := try {
    $ids ! job:remove(.),
    map { 'info': util:info($ids, 'job', 'removed') }
  } catch * {
    map { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
