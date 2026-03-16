docker compose -f docker-compose.dev.yaml up -d
docker compose -f docker-compose.dev.yaml build gateway-service --no-cache 
docker compose -f docker-compose.dev.yaml up -d gateway-service
docker compose -f docker-compose.dev.yaml build identity-service --no-cache
docker compose -f docker-compose.dev.yaml up -d identity-service
docker compose -f docker-compose.dev.yaml build feed-service --no-cache 
docker compose -f docker-compose.dev.yaml up -d feed-service