#!/bin/bash

docker container run --name sgs-mongo -v ~/sgs/db/:/data/db -d -p 27016:27017 mongo:latest
