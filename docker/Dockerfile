FROM jlesage/baseimage-gui:ubuntu-20.04	


# Install OpenJDK-17
RUN apt-get update 
RUN apt-get install -y openjdk-17-jre


COPY  tmp/ /rcrs_server/
WORKDIR /rcrs_server/

RUN ./gradlew 
#clean && \
#    ./gradlew

WORKDIR /rcrs_server/scripts/
CMD bash -c "if [[ $precompute == False ]]; then \
    echo ./start-comprun.sh -m ../maps/$map/map/ -c ../maps/$map/config/  > run.sh; \
    else echo ./start-precompute.sh -m ../maps/$map/map/ -c ../maps/$map/config/  > run.sh; fi; \
    chmod 777 run.sh; \
    ./run.sh"



