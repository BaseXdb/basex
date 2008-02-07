import module namespace hp="http://www.basex.org/" at "main.xqm";

hp:write(
  let $commands := $hp:doc/commands
  return (
    <p>{ $commands/text/node() }</p>,
    for $cmd in $commands/command
    return (
      <a name="{ $cmd/name/node() }"><h3>{ $cmd/name/node() }</h3></a>,
      <p><code>{ $cmd/syntax/node() }</code><br/><br/>
      { $cmd/desc/node() }</p>
    )
  )
)
