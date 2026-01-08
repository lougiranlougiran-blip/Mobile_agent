#!/bin/bash

WORK_DIR="$(realpath "$(dirname "$0")")"

SRC_DIR="$WORK_DIR/src"

OUT_DIR="$WORK_DIR/target/classes/"

rm -rf "$OUT_DIR"/*

mkdir -p "$OUT_DIR"

javac -d "$OUT_DIR" -cp "$SRC_DIR" $(find "$SRC_DIR" -name "*.java")

java -cp "$OUT_DIR" Server.ServeurRMI "$@"
