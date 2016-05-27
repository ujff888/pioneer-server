#!/bin/bash
#protoc.sh
protoc -I=. --java_out=/Users/kthh/github/gameserver/game-core/src/main/java/ ./*.proto
