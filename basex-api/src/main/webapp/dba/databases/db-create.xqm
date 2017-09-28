(:~
 : Create new database.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for creating a new database.
 : @param  $name   entered name
 : @param  $opts   chosen database options
 : @param  $lang   entered language
 : @param  $error  error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/db-create")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("opts",  "{$opts}")
  %rest:query-param("lang",  "{$lang}", "en")
  %rest:query-param("error", "{$error}")
  %output:method("html")
function dba:db-create(
  $name   as xs:string?,
  $opts   as xs:string*,
  $lang   as xs:string?,
  $error  as xs:string?
) as element(html) {
  cons:check(),

  let $opts := if($opts = 'x') then $opts else ('textindex', 'attrindex')
  return html:wrap(map { 'header': $dba:CAT, 'error': $error },
    <tr>
      <td>
        <form action="db-create" method="post" autocomplete="off">
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:button('create', 'Create')
          }</h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type="hidden" name="opts" value="x"/>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type="hidden" name="opts" value="x"/>
                <input type="text" name="name" value="{ $name }" id="name"/>
                { html:focus('name') }
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td colspan="2">{
                <h3>{ html:option('textindex', 'Text Index', $opts) }</h3>,
                <h3>{ html:option('attrindex', 'Attribute Index', $opts) }</h3>,
                <h3>{ html:option('tokenindex', 'Token Index', $opts) }</h3>,
                html:option('updindex', 'Incremental Indexing', $opts),
                <div class='small'/>,
                <h3>{ html:option('ftindex', 'Fulltext Indexing', $opts) }</h3>
              }</td>
            </tr>
            <tr>
              <td colspan="2">{
                html:option('stemming', 'Stemming', $opts),
                html:option('casesens', 'Case Sensitivity', $opts),
                html:option('diacritics', 'Diacritics', $opts)
              }</td>
            </tr>
            <tr>
              <td>Language:</td>
              <td><input type="text" name="language" value="{ $lang }"/></td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Creates a database.
 : @param  $name  database
 : @param  $opts  database options
 : @param  $lang  language
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/db-create")
  %rest:query-param("name", "{$name}")
  %rest:query-param("opts", "{$opts}")
  %rest:query-param("lang", "{$lang}")
function dba:create(
  $name  as xs:string,
  $opts  as xs:string*,
  $lang  as xs:string?
) as empty-sequence() {
  cons:check(),
  try {
    if(db:exists($name)) then (
      error((), 'Database already exists.')
    ) else (
      db:create($name, (), (), map:merge((
        for $option in ('textindex', 'attrindex', 'tokenindex', 'ftindex',
          'stemming', 'casesens', 'diacritics', 'updindex')
        return map:entry($option, $opts = $option),
        $lang ! map:entry('language', .)))
      ),
      cons:redirect($dba:SUB, map { 'name': $name, 'info': 'Database was created.' })
    )
  } catch * {
    cons:redirect('db-create', map {
      'name': $name, 'opts': $opts, 'lang': $lang, 'error': $err:description
    })
  }
};
