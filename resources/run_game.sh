#!/bin/sh

set -e

./halite --replay-directory replays/ -vvv --width 32 --height 32 --seed 1546049512 --no-compression \
"java -jar ../target/MyBot.jar" \
"java -jar bot_versions/beta2.jar"
# "java -jar bot_versions/beta1.jar" \
# "java -jar bot_versions/alpha2.jar"

# good seed 1544406607
# dense halite map 1545883640

MOST_RECENT_REPLAY=$(ls -d $PWD/replays/*.hlt -t | head -n1)
electron ../../fluorine/ -o $MOST_RECENT_REPLAY &
