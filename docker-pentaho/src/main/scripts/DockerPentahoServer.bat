@echo off

REM *****************************************************************************
REM
REM Pentaho Data Integration
REM
REM Copyright (C) 2008 - 2021 by Hitachi Vantara : http://www.hitachivantara.com
REM
REM *****************************************************************************
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with
REM the License. You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM
REM *****************************************************************************
setlocal
pushd %~dp0
set JAVA_TOOL_OPTIONS=
java -Dsun.security.krb5.debug=true -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=12144 -cp  lib/*;docker-pentaho-9.3.0.0-SNAPSHOT.jar org.pentaho.docker.support.DockerPentahoServerCli %*
popd