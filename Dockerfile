# Qaybta 1-aad: Build-ka barnaamijka (Waxaan ka bilaabaynaa Microsoft JDK 25)
FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu AS build
LABEL authors="mohamedogleh"
WORKDIR /app

# Waxaan mashiinka ku dhex rakibaynaa Maven si uu u haysto Java 25 + Maven
RUN apt-get update && apt-get install -y maven

# Ku koobiyeey koodka oo dhan mashiinka dhexdiisa
COPY . .

# Hadda dhisiddu waxay ku dhex socon doontaa Java 25 si guul leh!
RUN mvn clean package -DskipTests

# Qaybta 2-aad: Runtime-ka rasmiga ah ee Microsoft OpenJDK 25 (Kani waa kan yar ee barnaamijka qaadaya)
FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Dekedda (Port) aad dooratay waa 8081
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]