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
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/activity')
  %rest:query-param('sort',     '{$sort}', 'duration')
  %rest:query-param('job',      '{$job}')
  %rest:query-param('download', '{$download}')
  %output:method('html')
function dba:activity(
  $sort      as xs:string,
  $job       as xs:string?,
  $download  as xs:string?
) as element(html) {
  let $panel := fn($contents, $options) {
    html:panel($contents, map:put($options, 'divider', true()))
  }
  return (
    $panel(panels:jobs($sort), { 'id': 'jobs-panel', 'label': 'Jobs' }),
    if ($job) {
      (: a job that is done does not change any more: the client stops asking for it. The form
         is the pane: its blocks are laid out in a column, so that the query and the result
         take the height that the tables leave instead of being fixed to a few lines :)
      $panel(
        <form method='post' autocomplete='off' id='job-details' class='pane column'
              data-done='{ panels:job-done($job) }'>{
          panels:job-details($job) otherwise (
            <h2>{ 'Job: ' || $job }</h2>, 'Job has expired.'
          )
        }</form>,
        { 'label': 'Job', 'pane': false() }
      )
    },
    (: what a job is doing is what the view is opened for: the reports step back to strips
       while one is shown :)
    $panel(panels:web-sessions(),
      { 'id': 'web-panel', 'label': 'Web Sessions', 'collapsed': exists($job) }),
    (: rarely of interest: the panels open on demand :)
    $panel(panels:websockets(),
      { 'id': 'ws-panel', 'label': 'WebSockets', 'collapsed': true() }),
    $panel(panels:caches(),
      { 'id': 'caches-panel', 'label': 'Caches', 'collapsed': true() }),
    $panel(panels:db-sessions(),
      { 'id': 'db-panel', 'label': 'Database Sessions', 'collapsed': true() }),
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
    'init'   : 'initActivity();'
  })
};
