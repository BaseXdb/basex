(:~
 : Settings.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/settings';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~ Categories of the sections of db:system. :)
declare %private variable $dba:SECTIONS := {
  'generalinformation': 'General',
  'globaloptions'     : 'Global Option',
  'localoptions'      : 'Local Option',
  'libraries'         : 'Library'
};

(:~
 : Settings.
 : @param  $sort  table sort key
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/settings')
  %rest:query-param('sort', '{ $sort }', '')
  %output:method('html')
function dba:settings(
  $sort  as xs:string
) as element(html) {
  (: the labels are full sentences: they are placed above their control :)
  let $option := fn($key, $values, $label) {
    form:field($label, form:select($key, $values, config:get($key)), 'stacked')
  }
  (: the bounds of an option restrict its field; without them, any number is accepted :)
  let $number := fn($key, $label, $range) {
    form:field($label,
      <input type='number' name='{ $key }' value='{ config:get($key) }'>{
        if (exists($range)) { attribute min { head($range) }, attribute max { tail($range) } }
      }</input>,
      'stacked')
  }
  (: unsorted, the entries are listed in the order of their categories :)
  let $entries := (
    for $section in db:system()/*
    for $option in $section/*
    let $value := $option/data()
    return {
      'name'    : upper-case($option/name()),
      'value'   : fn() { table:option-value($value) },
      'category': $dba:SECTIONS?(name($section))
    },
    for key $key value $value in prof:runtime()
    return {
      'name'    : upper-case($key),
      'value'   : if ($key = 'processors') then $value else prof:human($value),
      'category': 'Runtime'
    },
    for key $key value $value in proc:property-map()
    order by $key collation '?lang=en'
    return { 'name': $key, 'value': $value, 'category': 'System Property' },
    for $key in available-environment-variables()
    order by $key collation '?lang=en'
    return { 'name': $key, 'value': environment-variable($key), 'category': 'Environment Variable' }
  )
  let $headers := (
    { 'key': 'category', 'label': 'Category', 'width': 'calc(20ch + 0.5rem)' },
    { 'key': 'name', 'label': 'Name', 'width': '30%' },
    { 'key': 'value', 'label': 'Value' }
  )
  (: the search line resembles the one of the logs :)
  let $search := <div class='searchbar'>{
    <h2>System</h2>,
    <input type='text' id='input' placeholder='Search, e.g. cache' autofocus=''
           title='Words that the name, value or category of an entry must contain'
           data-filter='filterSettings'/>,
    form:button('settings/gc', 'Free Memory', title := 'Run the Java garbage collector')
  }</div>
  let $filter-row := <tr>{
    $headers ! <td>{
      <input type='text' class='filter' placeholder='{ ?label }'
             title='Text of the { lower-case(?label) } values to show'
             data-filter='filterSettings'/>
    }</td>
  }</tr>
  (: the information on the server is shown first, the settings of the DBA last :)
  return (
    html:panel(
      <form method='post' autocomplete='off'>{
        table:create($headers, $entries, options := {
          'sort': $sort, 'all': true(), 'sticky': $search, 'filters': $filter-row
        })
      }</form>,
      { 'label': 'System' }),
    html:panel(
      <form method='post' autocomplete='off'>{
        html:heading('DBA Settings',
          form:button('settings/save', 'Save', title := 'Save the settings')),
        <h3>Queries</h3>,
        $number($config:TIMEOUT, 'Timeout, in seconds (0 = disabled)', ()),
        $number($config:MEMORY, 'Memory limit, in MB (0 = disabled)', ()),
        $number($config:MAXCHARS, 'Maximum output size', ()),
        $option($config:PERMISSION, $config:PERMISSIONS, 'Permission'),
        <h3>Live Views</h3>,
        $number($config:INTERVAL, 'Delay between requests, in seconds',
          (1, $config:MAX-INTERVAL))
      }</form>,
      { 'label': 'DBA Settings' })
  ) => html:wrap({
    'header': $dba:CAT, 'divided': true(), 'rows': '1fr', 'columns': ('3fr', '1fr'),
    'scripts': 'settings', 'init': 'initSettings();'
  })
};

(:~
 : Saves the settings.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/settings/save')
function dba:save() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': 'Settings were saved.',
    'run' : %updating fn() { config:save(html:parameters()) }
  } })
};

(:~
 : Triggers garbage collection.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/settings/gc')
function dba:gc() {
  (: the collection runs before the message is composed, which reports its outcome :)
  utils:dispatch($dba:CAT, fn($args) {
    prof:gc(),
    {
      'info': `Garbage collection was triggered (memory in use: { prof:human(prof:runtime('used')) }).`,
      'run' : %updating fn() { }
    }
  })
};
