(:~
 : Remove jobs.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/jobs';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs';

(:~
 : Remove jobs.
 : @param  $ids  job ids
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/job-remove')
  %rest:form-param('id', '{$ids}')
function dba:job-remove(
  $ids  as xs:string*
) as element(rest:response) {
  try {
    $ids ! job:remove(.),
    web:redirect($dba:CAT, { 'info': utils:info($ids, 'job', 'removed') })
  } catch * {
    web:redirect($dba:CAT, { 'error': $err:description })
  }
};
