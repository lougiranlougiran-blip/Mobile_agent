#!/bin/bash

WORK_DIR="$(realpath "$(dirname "$0")")"

SRC_DIR="$WORK_DIR/src"

OUT_DIR="$WORK_DIR/target/classes/"

AGENT_DIR="$SRC_DIR/Agent/"
SERVER_DIR="$SRC_DIR/Server/"

rm -rf "$OUT_DIR"/*

mkdir -p "$OUT_DIR"

javac -d "$OUT_DIR" -cp "$SRC_DIR" "$AGENT_DIR"/*.java "$SERVER_DIR"/*.java

java -cp "$OUT_DIR" Server.Server "$@"
