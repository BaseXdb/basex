(:~
 : Panels of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panels = 'dba/lib/panels';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Creates the contents of the jobs panel.
 : @param  $sort  table sort key
 : @return panel contents
 :)
declare function panels:jobs(
  $sort  as xs:string
) as element()+ {
  <form method='post' autocomplete='off'>
    <h2>Jobs</h2>
    {
      let $headers := (
        { 'key': 'id', 'label': 'ID' },
        { 'key': 'state', 'label': 'State' },
        { 'key': 'duration', 'label': 'Dur.', 'type': 'number', 'order': 'desc' },
        { 'key': 'user', 'label': 'User' },
        { 'key': 'time', 'label': 'Time', 'type': 'time', 'order': 'desc' },
        { 'key': 'start', 'label': 'Start', 'type': 'time', 'order': 'desc' }
      )
      let $entries :=
        let $curr := job:current()
        for $details in job:list-details()
        let $id := $details/@id
        (: the job that renders this table is of no interest :)
        where not($id = $curr)
        let $sec := (xs:dayTimeDuration($details/@duration) div xs:dayTimeDuration('PT1S'))
          otherwise 0
        let $time := data($details/@time)
        let $start := data($details/@start)
        order by $sec descending, $start descending
        return {
          'id': $id,
          'state': $details/@state,
          'duration': html:duration($sec),
          'user': $details/@user,
          'time': $time,
          'start': $start otherwise $time
        }
      let $buttons := (
        form:button('jobs/remove', 'Remove', ('CHECK', 'CONFIRM')),
        <label title='Refresh the view every second'>{
          <input type='checkbox' id='live' data-live='activity' checked=''
                 onchange='liveChanged()'/>, ' Live'
        }</label>
      )
      let $options := { 'sort': $sort, 'presort': 'duration' }
      return table:create($headers, $entries, $buttons, {}, $options) update {
        (: replace job ids with links; the separator after the checkbox stays outside :)
        for $tr in descendant::tr[not(th)]
        for $text in $tr/td[1]/text()
        for $id in data($tr/@id)
        return replace node $text with (
          substring-before($text, $id),
          <a href='?job={ $id }'>{ $id }</a>
        )
      }
    }
  </form>
};

(:~
 : Creates the details of a job.
 : @param  $details  job details; if empty, the job is unknown
 : @return details, or empty sequence if the job is unknown
 :)
declare function panels:job-details(
  $details  as element()?
) as element()* {
  let $job := string($details/@id)
  where $details
  return <form method='post' autocomplete='off'>{
    <input type='hidden' name='id' value='{ $job }'/>,
    (: the heading ends after the id, which can be long and is clipped rather than wrapped :)
    <h2>{ 'Job: ' || $job }</h2>,
    <div class='buttons'>{ form:button('jobs/remove', 'Remove') }</div>,

    panels:job-information($details),

    let $bindings := job:bindings($job)
    where map:size($bindings) > 0
    return (
      <h3>Query Bindings</h3>,
      <table>{
        map:for-each($bindings, fn($key, $value) {
          <tr>
            <td><b>{ if ($key) then '$' || $key else 'Context' }</b></td>
            <td><code>{
              utils:chop(serialize($value, { 'method': 'basex' }), 1000)
            }</code></td>
          </tr>
        })
      }
      </table>
    ),

    if ($details/@state = 'cached') {
      let $output := try {
        utils:serialize(job:result($job, { 'keep': true() }))
      } catch * {
        'Stopped at ' || $err:module || ', ' || $err:line-number || '/' ||
          $err:column-number || ':' || char('\n') || $err:description
      }
      where $output
      return (
        <h3>{
          'Result', '&#xa0;',
          form:button('job-result', 'Download')
        }
        </h3>,
        <textarea id='output' readonly='' spellcheck='false'>{ $output }</textarea>
      )
    },

    <h3>Job String</h3>,
    <textarea readonly='' spellcheck='false'>{ string($details) }</textarea>
  }</form>
};

(:~
 : Indicates whether a job has finished, and its details will not change any more.
 : @param  $details  job details; if empty, the job is gone and will not come back
 : @return result of check
 :)
declare function panels:job-done(
  $details  as element()?
) as xs:boolean {
  empty($details) or $details/@state = 'cached'
};

(:~
 : Creates the general information of a job.
 : @param  $details  job details
 : @return heading and table
 :)
declare %private function panels:job-information(
  $details  as element()
) as element()+ {
  <h3>General Information</h3>,
  <table>{
    for $value in $details/@*
    for $name in name($value)[. != 'id']
    return <tr>
      <td><b>{ utils:capitalize($name) }</b></td>
      <td>{ string($value) }</td>
    </tr>
  }</table>
};

(:~
 : Creates the contents of the web sessions panel.
 : @return panel contents
 :)
declare function panels:web-sessions() as element()+ {
  <form method='post' autocomplete='off'>
    <h2>Web Sessions</h2>
    {
      let $headers := (
        { 'key': 'id', 'label': 'ID', 'type': 'id' },
        { 'key': 'name', 'label': 'Name' },
        { 'key': 'value', 'label': 'Value' },
        { 'key': 'access', 'label': 'Last Access', 'type': 'time', 'order': 'desc' },
        { 'key': 'you', 'label': 'You' }
      )
      let $entries :=
        for $id in sessions:ids()
        (: a session can be dropped between being listed and being read; skip the ones that
           are gone by then, rather than failing the whole panel :)
        for $access in try { sessions:accessed($id) } catch sessions:not-found { }
        let $you := if (session:id() = $id) then '✓' else '–'
        (: supported session ids (application-specific, can be extended) :)
        for $name in (try { sessions:names($id) } catch sessions:not-found { })
          [. = ($config:SESSION-KEY, 'id')]
        let $value := try {
          sessions:get($id, $name)
        } catch sessions:get {
          '–' (: non-XQuery session value :)
        }
        let $string := utils:chop(serialize($value, { 'method': 'basex' }), 20)
        order by $access descending
        return {
          'id': $id || '|' || $name,
          'name': $name,
          'value': $string,
          'access': $access,
          'you': $you
        }
      let $buttons := form:button('sessions/kill', 'Kill', ('CHECK', 'CONFIRM'))
      return table:create($headers, $entries, $buttons)
    }
  </form>
};

(:~
 : Creates the contents of the database sessions panel.
 : @return panel contents
 :)
declare function panels:db-sessions() as element()+ {
  <h2>Database Sessions</h2>,
  table:create(
    (
      { 'key': 'address', 'label': 'Address' },
      { 'key': 'user', 'label': 'User' }
    ),
    admin:sessions() ! { 'address': @address, 'user': @user }
  )
};
