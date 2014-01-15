#!/bin/sh

if [[ -z "$BATTLECODE" ]]; then
  export BATTLECODE=$HOME/battlecode
fi

if [ ! -e $BATTLECODE/MethodCosts.txt ]; then
    echo "Error: not a BattleCode directory: $BATTLECODE"
    exit
fi

mkdir -p $BATTLECODE/repos
cd $BATTLECODE/repos

git clone git@github.com:andykat/cowsandcows3 andykat
git clone git@github.com:davidvperez/battlecode2014 davidvperez
git clone git@github.com:haysoos/battlecode-2014 haysoos
git clone git@github.com:jacobjenks/DontPanic jacobjenks
git clone git@github.com:therealgin/JinPlayer therealgin
git clone git@github.com:jkelleyy/battlecode jkelleyy
git clone git@github.com:jrshoch/battlecode2014 jrshoch
git clone git@github.com:kennethmyers/Battlecode2014 kennethmyers
git clone git@github.com:AdmiralLima/teams AdmiralLima
git clone git@github.com:ltchin/Battle-and-or-code ltchin
git clone git@github.com:ychen022/miltank14 ychen022
git clone git@github.com:nocomment-battlecode/2014 nocomment
git clone git@github.com:oblodgett/Battlecode2014 oblodgett
git clone git@github.com:phager/Battlecode-2014 phager
git clone git@github.com:RAttab/not-battlecode-2014 RAttab
git clone git@github.com:joehan/Repo7 joehan
git clone git@github.com:skoppula/yummycode-2014 skoppula
git clone git@github.com:skylarjhdownes/booleanisdragonbattlecode2014 skylarjhdownes
git clone git@github.com:ssscoder/BattleCode ssscoder
git clone git@github.com:therealgin/BATTLECODE2014 therealgin2
