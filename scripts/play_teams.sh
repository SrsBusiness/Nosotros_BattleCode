#!/bin/sh
OUR_TEAM="Nosotros_BattleCode"
MAP="rushlane"

match() {
    cd $BATTLECODE
    dflags="-Dbc.game.team-a=$OUR_TEAM -Dbc.game.team-b=$1 -Dbc.game.maps=$MAP"
    lflags="-classpath bin:bin/*:lib/*"
    args="-c /dev/null -h -n"
    java $dflags $lflags battlecode.server.Main $args | grep -Eo '[^ ]+ \([AB]\) wins' | xargs -0 printf "Playing team: $1\n%s\n"
}

if [[ -z "$BATTLECODE" ]]; then
  export BATTLECODE=$HOME/battlecode
fi

if [ ! -e $BATTLECODE/MethodCosts.txt ]; then
    echo "Error: not a BattleCode directory: $BATTLECODE"
    exit
fi

if [[ -n "$1" ]]; then
    match "$1"
    exit
fi

$(cd $BATTLECODE ant clean > /dev/null )
$(cd $BATTLECODE ant build > /dev/null )
ls $BATTLECODE/bin | parallel --no-notice $0 {}
