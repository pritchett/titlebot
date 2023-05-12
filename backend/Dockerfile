FROM openjdk:11

ENV SCALA_VERSION 3.1.0
ENV SBT_VERSION 1.5.5

RUN curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup -y
RUN ./cs install scala:$SCALA_VERSION && ./cs install scalac:$SCALA_VERSION
RUN ./cs install sbt

WORKDIR backend
COPY . ./

RUN ~/.local/share/coursier/bin/sbt compile

CMD ["/root/.local/share/coursier/bin/sbt", "run"]

