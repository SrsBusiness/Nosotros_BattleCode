#!/bin/sh

if [[ -z "$BATTLECODE" ]]; then
  export BATTLECODE=$HOME/battlecode
fi

if [ ! -e $BATTLECODE/MethodCosts.txt ]; then
    echo "Error: not a BattleCode directory: $BATTLECODE"
    exit
fi

find $BATTLECODE/repos -iname 'RobotPlayer.java' -print0 | while read -d $'\0' -r file ; do
    team=$(dirname "$file")
    printf 'Copying file: %s\n' "$team"
    cp -R "$team" $BATTLECODE/teams/
done

cat blacklist.txt | xargs -I % rm -rf "$BATTLECODE/teams/%";
