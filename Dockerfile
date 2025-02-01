FROM openjdk:18
WORKDIR ai

ADD easyQServer-0.2.3.jar /ai/easyQServer-0.2.3.jar
EXPOSE 7860
COPY entrypoint.sh /entrypoint.sh
# Make the script executable
RUN chmod +x /entrypoint.sh
# Set the entrypoint
ENTRYPOINT ["/entrypoint.sh"]