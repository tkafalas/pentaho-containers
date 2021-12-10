#!/bin/sh
 
#export CATALINA_OPTS="$CATALINA_OPTS -DNODE_NAME=$(hostname) -Djava.awt.headless=true"

# Here we will override all the files from the "/docker-entrypoint-init" into the pentaho base folder.
# This allows users to change configuration files before server starts

# Next line set by DockerPentahoServer CLI
INSTALLATION_PATH=/opt/pentaho
echo found $(find /docker-entrypoint-init/ -type f -print | wc -l) files to be copied
cp -r /docker-entrypoint-init/* $INSTALLATION_PATH/data-integration/

exec "$@"