#!/bin/sh

set -e

./halite --replay-directory replays/ -vvv --width 32 --height 32 "java -jar ../target/MyBot.jar" "java -jar bot_versions/random_bot.jar"
