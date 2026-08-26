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

(:~
 : Settings.
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/settings')
  %output:method('html')
function dba:settings() as element(html) {
  let $system := table:properties(db:system())
  (: boundary between global and local options (keyed by name, not position) :)
  let $local := $system/tr[th/h3 = 'LOCALOPTIONS']
  (: the labels are full sentences: they are placed above their control :)
  let $option := fn($key, $values, $label) {
    form:field($label,
      <select name='{ $key }'>{
        let $selected := config:get($key)
        for $value in $values
        return element option { attribute selected { }[$value = $selected], $value }
      }</select>,
      'stacked'
    )
  }
  (: the bounds of an option restrict its field; without them, any number is accepted :)
  let $number := fn($key, $label, $range) {
    form:field($label,
      <input type='number' name='{ $key }' value='{ config:get($key) }'>{
        if (exists($range)) { attribute min { head($range) }, attribute max { tail($range) } }
      }</input>,
      'stacked')
  }
  let $map-table := fn($map) {
    table:pairs(
      for $key in sort(map:keys($map), '?lang=en')
      return <tr>
        <td><b>{ $key }</b></td>
        <td>{ $map($key) }</td>
      </tr>
    )
  }
  let $panel := fn($contents, $options) {
    html:panel($contents, map:put($options, 'divider', true()))
  }
  return (
    $panel(
      <form method='post' autocomplete='off'>{
        html:heading('Settings', form:button('settings/save', 'Save')),
        <h3>Queries</h3>,
        $number($config:TIMEOUT, 'Timeout, in seconds (0 = disabled)', ()),
        $number($config:MEMORY, 'Memory limit, in MB (0 = disabled)', ()),
        $number($config:MAXCHARS, 'Maximum output size', ()),
        $option($config:PERMISSION, $config:PERMISSIONS, 'Permission'),
        <h3>Tables</h3>,
        $number($config:MAXROWS, 'Displayed table rows', ()),
        <h3>Live Views</h3>,
        $number($config:INTERVAL, 'Delay between requests, in seconds', (1, 10))
      }</form>,
      { 'label': 'Settings' }),
    $panel(
      <form method='post' autocomplete='off'>{
        html:heading('Global Options', form:button('settings/gc', 'GC')),
        table:pairs($local/preceding-sibling::tr[not(th)])
      }</form>,
      { 'label': 'Global Options' }),
    $panel((
      <h2>Local Options</h2>,
      table:pairs($local/following-sibling::tr)
    ), { 'label': 'Local Options' }),
    $panel((
      <h2>Environment Variables</h2>,
      $map-table(map:build(available-environment-variables(), value := environment-variable#1))
    ), { 'label': 'Environment Variables', 'collapsed': true() }),
    $panel((
      <h2>System Properties</h2>,
      $map-table(proc:property-map())
    ), { 'label': 'System Properties', 'collapsed': true() })
  ) => html:wrap({
    'header': $dba:CAT, 'rows': '1fr'
  })
};

(:~
 : Runs a settings action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/settings/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'save': fn($args) { {
      'info': 'Settings were saved.',
      'run' : %updating fn() { config:save(html:parameters()) }
    } },
    'gc': fn($args) { {
      'info': 'Garbage collection was triggered.',
      'run' : %updating fn() { prof:gc() }
    } }
  })
};
