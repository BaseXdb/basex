(:~
 : Activity: what the server is running, and for whom.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/activity';

import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace panels = 'dba/lib/panels' at 'panels.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

(:~
 : Activity.
 : @param  $sort   table sort key
 : @param  $job    highlighted job
 : @param  $error  error message
 : @param  $info   info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/activity')
  %rest:query-param('sort',  '{$sort}', 'duration')
  %rest:query-param('job',   '{$job}')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:activity(
  $sort   as xs:string,
  $job    as xs:string?,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  (
    <div class='panel'>
      <div id='jobs-panel' class='pane'>{ panels:jobs($sort) }</div>
    </div>,
    if ($job) {
      let $details := job:list-details($job)
      return <div class='panel'>{
        (: a job that is done does not change any more: the client stops asking for it :)
        <div id='job-details' class='pane' data-done='{ panels:job-done($details) }'>{
          panels:job-details($details) otherwise (
            <h2>{ 'Job: ' || $job }</h2>, 'Job has expired.'
          )
        }</div>
      }</div>
    },
    <div class='panel'>
      <div id='web-panel' class='pane'>{ panels:web-sessions() }</div>
    </div>,
    (: rarely of interest: the panel opens on demand :)
    <div class='panel collapsed'>
      <div id='db-panel' class='pane'>{ panels:db-sessions() }</div>
    </div>
  ) => html:wrap({
    (: no widths: the panels share the page in equal parts, and each scrolls on its own :)
    'header' : $dba:CAT,
    'rows'   : '1fr',
    'scripts': ('cm6', 'editor', 'activity'),
    'init'   : 'initActivity();',
    'info'   : $info,
    'error'  : $error
  })
};
