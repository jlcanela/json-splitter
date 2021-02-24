#!/bin/bash

sbt clean coverage test coverageReport
./tools/publish.sh
