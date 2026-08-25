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
 : @param  $sort      table sort key
 : @param  $job       highlighted job
 : @param  $download  job whose result is to be downloaded
 : @param  $error     error message
 : @param  $info      info message
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/activity')
  %rest:query-param('sort',     '{$sort}', 'duration')
  %rest:query-param('job',      '{$job}')
  %rest:query-param('download', '{$download}')
  %rest:query-param('error',    '{$error}')
  %rest:query-param('info',     '{$info}')
  %output:method('html')
function dba:activity(
  $sort      as xs:string,
  $job       as xs:string?,
  $download  as xs:string?,
  $error     as xs:string?,
  $info      as xs:string?
) as element(html) {
  (
    <div class='panel'>
      <div id='jobs-panel' class='pane'>{ panels:jobs($sort) }</div>
    </div>,
    if ($job) {
      <div class='panel'>{
        (: a job that is done does not change any more: the client stops asking for it. The form
           is the pane: its blocks are laid out in a column, so that the query and the result
           take the height that the tables leave instead of being fixed to a few lines :)
        <form method='post' autocomplete='off' id='job-details' class='pane column'
              data-done='{ panels:job-done($job) }'>{
          panels:job-details($job) otherwise (
            <h2>{ 'Job: ' || $job }</h2>, 'Job has expired.'
          )
        }</form>
      }</div>
    },
    (: what a job is doing is what the view is opened for: the reports step back to strips
       while one is shown :)
    <div class='panel{ ' collapsed'[$job] }'>
      <div id='web-panel' class='pane'>{ panels:web-sessions() }</div>
    </div>,
    (: rarely of interest: the panels open on demand :)
    <div class='panel collapsed'>
      <div id='ws-panel' class='pane'>{ panels:websockets() }</div>
    </div>,
    <div class='panel collapsed'>
      <div id='caches-panel' class='pane'>{ panels:caches() }</div>
    </div>,
    <div class='panel collapsed'>
      <div id='db-panel' class='pane'>{ panels:db-sessions() }</div>
    </div>,
    (: outside the panels: they are replaced by the refresh, the dialogs are not :)
    panels:job-dialog(),
    panels:session-dialog(),
    panels:websocket-dialog(),
    (: the result of a job that was closed: submitted as soon as the page is there, and the
       request that fetches the file is what consumes the job :)
    if ($download) {
      <form id='download-form' method='post' action='job-result'>
        <input type='hidden' name='id' value='{ $download }'/>
      </form>
    }
  ) => html:wrap({
    (: no widths: the panels share the page in equal parts, and each scrolls on its own :)
    'header' : $dba:CAT,
    'rows'   : '1fr',
    (: the details are inserted before the reports, which moves every panel behind them: what
       is folded away while a job is shown is remembered apart from the overview :)
    'panels' : 'job'[$job],
    'scripts': ('cm6', 'editor', 'activity'),
    'init'   : 'initActivity();',
    'info'   : $info,
    'error'  : $error
  })
};
