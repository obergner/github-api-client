#!/bin/bash - 

cat project.clj | awk '/defproject/ { gsub(/"/, "", $3); print $3 }'
