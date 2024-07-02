(:~
 : Remove jobs.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/jobs';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs';

(:~
 : Removes jobs.
 : @param  $ids  job ids
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/job-remove')
  %rest:query-param('id', '{$ids}')
function dba:job-remove(
  $ids  as xs:string*
) as element(rest:response) {
  let $params := try {
    $ids ! job:remove(.),
    { 'info': utils:info($ids, 'job', 'removed') }
  } catch * {
    { 'error': $err:description }
  }
  return web:redirect($dba:CAT, $params)
};
