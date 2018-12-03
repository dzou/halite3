#!/bin/sh

set -e

./halite --replay-directory replays/ -vvv --width 64 --height 64 --seed 1543790775 \
"java -jar ../target/MyBot.jar" \
"java -jar bot_versions/alpha2.jar"
#"java -jar bot_versions/spawner.jar" \
#"java -jar bot_versions/hungarian.jar"

MOST_RECENT_REPLAY=$(ls -d $PWD/replays/*.hlt -t | head -n1)
electron ../../fluorine/ -o $MOST_RECENT_REPLAY &
