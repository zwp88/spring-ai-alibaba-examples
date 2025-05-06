mvn clean install -DskipTests
docker build -t spring-ai-alibaba-playground .


mkdir -p logs

docker run -d -p 8080:8080 -v $(pwd)/src/main/resources/mcp-libs:/app/resources/mcp-libs -v $(pwd)/src/main/resources/rag/markdown:/app/resources/rag/markdown -v $(pwd)/src/main/resources/db:/app/resources/db -v $(pwd)/logs:/app/logs --name spring-ai-alibaba-playground spring-ai-alibaba-playground
