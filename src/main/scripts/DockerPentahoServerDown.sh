# shutdown docker compose
BASEDIR="`dirname $0`"
cd "$BASEDIR"/generatedFiles
docker compose down -v
cd - > /dev/null