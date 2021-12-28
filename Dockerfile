from gradle:jdk17
COPY . /rcrs-server/
WORKDIR /rcrs-server/

RUN gradle completeBuild

CMD ["bash","docker-run.sh"]