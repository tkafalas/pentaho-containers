# shutdown docker compose
BASEDIR="`dirname $0`"
cd "$BASEDIR"/generatedFiles
sudo docker-compose down -v
cd - > /dev/null