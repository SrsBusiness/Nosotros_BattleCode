#!/bin/sh

if [[ -z "$BATTLECODE" ]]; then
  BATTLECODE=$HOME/battlecode
fi

if [ ! -e $BATTLECODE/MethodCosts.txt ]; then
    echo "Error: not a BattleCode directory: $BATTLECODE"
    exit
fi

cd $BATTLECODE/repos
find . -type d -depth 1 -exec git --git-dir={}/.git --work-tree=$PWD/{} pull origin master \;