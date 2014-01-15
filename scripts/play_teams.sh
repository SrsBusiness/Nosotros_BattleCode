#!/bin/sh

if [[ -z "$BATTLECODE" ]]
then
  export BATTLECODE=$HOME/battlecode
fi

OUR_TEAM="Nosotros_BattleCode"

cat > $BATTLECODE/bc.conf <<EOF
# Match server settings
bc.server.throttle=yield
bc.server.throttle-count=50
bc.server.mode=headless

# Game engine settings
bc.engine.debug-methods=false
bc.engine.silence-a=true
bc.engine.silence-b=true
bc.engine.gc=false
bc.engine.gc-rounds=50
bc.engine.upkeep=true
bc.engine.breakpoints=false
bc.engine.bytecodes-used=true

# Client settings
bc.client.opengl=false
bc.client.use-models=true
bc.client.renderprefs2d=
bc.client.renderprefs3d=
bc.client.sound-on=true
bc.client.check-updates=true
bc.client.viewer-delay=50

# Headless settings - for "ant file"
bc.game.maps=rushlane
bc.game.team-a=player_a
bc.game.team-b=$OUR_TEAM
bc.server.save-file=match.rms

# Transcriber settings
bc.server.transcribe-input=matches\\match.rms
bc.server.transcribe-output=matches\\transcribed.txt
EOF

cd $BATTLECODE

find bin -type dir -depth 1 -print0 | while read -d $'\0' -r file ; do
    team=$(basename "$file")
    printf 'Playing team: %s\n' "$team"
    sed -i ".old" "s/player_a/$team/g" bc.conf
    java -classpath ./bin/:./bin/*:./lib/* battlecode.server.Main | grep -Eo '[^ ]+ \([AB]\) wins'
    mv bc.conf.old bc.conf
done
