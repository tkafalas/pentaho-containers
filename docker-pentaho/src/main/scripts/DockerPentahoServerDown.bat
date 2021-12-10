rem shutdown docker compose
pushd %~dp0
cd generatedFiles
docker compose down -v
popd