(:~
 : Jobs of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/jobs';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
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
    'create': fn($args) { {
      (: a supplied id selects the new job; a generated one is not known before it is started :)
      'params': { 'job': $args?id }[$args?id],
      'info'  : 'Job was started.',
      'run'   : %updating fn() { dba:create($args) }
    } },
    'download': fn($args) { {
      (: the file is fetched by the page this leads to: reading a result closes the job, so the
         view must not keep showing it :)
      'params': { 'download': $args?id },
      'run'   : %updating fn() { () }
    } },
    'replace': fn($args) { {
      'params': { 'job': $args?id },
      'info'  : utils:info($args?id, 'service', 'replaced'),
      'run'   : %updating fn() { dba:replace($args?id, $args?query) }
    } },
    'remove': fn($args) { {
      'info': utils:info($args?id, 'job', 'removed'),
      'run' : %updating fn() { $args?id ! job:remove(.) }
    } },
    'unregister': fn($args) { {
      'info': utils:info($args?id, 'service', 'unregistered'),
      'run' : %updating fn() { $args?id ! job:remove(., { 'service': true() }) }
    } }
  })
};

(:~
 : Starts a job for the query of the dialog.
 : @param  $args  request parameters
 :)
declare %private function dba:create(
  $args  as map(*)
) as empty-sequence() {
  (: the scheduling options are only supplied if they were filled in: an empty string is no
     valid start time, interval or cron expression :)
  let $service := $args?service = 'true'
  let $options := map:merge((
    { 'base-uri': dba:base-uri() },
    (: a job of the DBA is bound by the same limits as its other queries; a service outlives
       the session that registers it, and must not carry its restrictions :)
    if (not($service)) { {
      'timeout'   : config:get($config:TIMEOUT),
      'memory'    : config:get($config:MEMORY),
      'permission': config:get($config:PERMISSION)
    } },
    { 'service': true() }[$service],
    { 'cache': true() }[$args?cache = 'true'],
    for $name in ('id', 'start', 'interval', 'cron', 'end')
    for $value in $args?($name)[.]
    return { $name: $value }
  ))
  return void(job:eval($args?query, (), $options))
};

(:~
 : Returns the base URI of a job that is started here: relative paths resolve against the file
 : directory, as they do in the editor.
 : @return base URI
 :)
declare %private function dba:base-uri() as xs:anyURI {
  (: a service keeps the URI on disk, where the native path that config:files-dir returns
     would not be portable :)
  file:path-to-uri(config:files-dir(()))
};

(:~
 : Replaces the query of a service, keeping its schedule: a job definition is written as a whole,
 : so the service is unregistered and registered again.
 : @param  $id     job id
 : @param  $query  new query
 :)
declare %private function dba:replace(
  $id     as xs:string,
  $query  as xs:string
) as empty-sequence() {
  let $service := job:services()[@id = $id]
  let $base-uri := ($service/@base-uri/string())[.] otherwise dba:base-uri()
  let $options := map:merge((
    { 'service': true() },
    for $option in $service/@*
    return { name($option): string($option) }
  ))
  return (
    (: parsed before anything is removed: a typo must not cost the registration :)
    void(utils:query-parse($query, $base-uri)),
    job:remove($id, { 'service': true() }),
    void(job:eval($query, (), $options))
  )
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
      dba:result($id, false(), utils:error-message($err:module, $err:line-number,
        $err:column-number, $err:description))
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
  (: whatever a query returned: it is served as bytes, not as what its name suggests :)
  utils:attachment($id || (if ($ok) then '.txt' else '.log'), $result,
    'application/octet-stream')
};
