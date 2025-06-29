FROM node:18-alpine
# or FROM openjdk:17-slim
# or FROM python:3.11-slim

WORKDIR /app

COPY package*.json ./
# or COPY pom.xml ./
# or COPY requirements.txt ./

RUN npm install
# or RUN mvn dependency:go-offline
# or RUN pip install -r requirements.txt

COPY . .

RUN npm run build
# or RUN mvn package
# or RUN python -m compileall

EXPOSE 8080

CMD ["npm", "start"]
# or CMD ["java", "-jar", "target/service.jar"]
# or CMD ["python", "src/main.py"]
