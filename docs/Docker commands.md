docker compose -f docker-compose.dev.yaml up -d
docker compose -f docker-compose.dev.yaml build gateway --no-cache 
docker compose -f docker-compose.dev.yaml up -d gateway
docker compose -f docker-compose.dev.yaml build identity --no-cache
docker compose -f docker-compose.dev.yaml up -d identity