## Docker images
docker-build:
	@mvn clean package -DskipTests
	@docker build -t spring-ai-alibaba-playground:latest -f deploy/docker/Dockerfile .

## Docker build remove
docker-build-remove:
	@docker rmi spring-ai-alibaba-playground:latest

## Docker run by default image from ghcr.io
docker-run:
	@echo "Running playground with default image from ghcr.io, only has dashscope api key env to set."
	@echo "If has more function, please refer README#Docker docs."
	@docker run -d -p 8080:8080 \
	-e AI_DASHSCOPE_API_KEY=your_api_key \
	--name spring-ai-alibaba-playground \
	ghrcr.io/spring-ai-alibaba/spring-ai-alibaba-examples/spring-ai-alibaba-playground:latest

## Docker build and run
docker-build-run:
	@docker run -d -p 8080:8080 \
	-e AI_DASHSCOPE_API_KEY=your_api_key \
	--name spring-ai-alibaba-playground \
	spring-ai-alibaba-playground:latest

## Docker Compose
docker-compose: frontend-build
	@docker-compose -f deploy/docker/docker-compose.yml up --build -d

## Kubernetes

## frontend build
frontend-build:
	@cd frontend && pnpm install && pnpm run build

## frontend dev
frontend-dev:
	@cd frontend && pnpm install && pnpm dev
