#!/bin/sh

if [[ -z "$BATTLECODE" ]]
then
  BATTLECODE=$HOME/battlecode
fi

cd $BATTLECODE/repos
find . -type d -depth 1 -exec git --git-dir={}/.git --work-tree=$PWD/{} pull origin master \;