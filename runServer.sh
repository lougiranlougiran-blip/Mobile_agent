#!/bin/bash

WORK_DIR="$(realpath "$(dirname "$0")")"
OUT_DIR="$WORK_DIR/target/classes/"
AGENT_DIR="$WORK_DIR/Agent/"
SERVER_DIR="$WORK_DIR/Server/"

mkdir -p "$OUT_DIR"

javac -d "$OUT_DIR" -cp "$OUT_DIR" "$AGENT_DIR"/*.java "$SERVER_DIR"/*.java

java -cp "$OUT_DIR" Server "$@"
