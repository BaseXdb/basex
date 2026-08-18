(:~
 : Jobs of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/jobs';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

(:~
 : Runs a job action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/jobs/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'remove': fn($args) { {
      'info': utils:info($args?id, 'job', 'removed'),
      'run' : %updating fn() { $args?id ! job:remove(.) }
    } }
  })
};

(:~
 : Downloads the result of a job.
 : @param  $id  job id
 : @return rest response and file content
 :)
declare
  %rest:POST
  %rest:path('/dba/job-result')
  %rest:form-param('id', '{$id}', '')
function dba:job-result(
  $id  as xs:string
) as item()+ {
  let $details := job:list-details($id)
  return if (empty($details)) {
    dba:result($id, false(), 'Job has expired.')
  } else if ($details/@state != 'cached') {
    dba:result($id, false(), 'Result is not available yet.')
  } else {
    try {
      dba:result($id, true(), job:result($id))
    } catch * {
      dba:result($id, false(),
        'Stopped at ' || $err:module || ', ' || $err:line-number || '/' ||
          $err:column-number || ':' || char('\n') || $err:description
      )
    }
  }
};

(:~
 : Returns a job result as a downloadable attachment.
 : @param  $id      job id
 : @param  $ok      ok flag
 : @param  $result  job result
 : @return rest response and file content
 :)
declare %private function dba:result(
  $id      as xs:string,
  $ok      as xs:boolean,
  $result  as item()*
) as item()+ {
  let $name := $id || (if ($ok) then '.txt' else '.log')
  return web:response-header(
    { 'media-type': 'application/octet-stream' },
    utils:disposition($name)
  ),
  $result
};
