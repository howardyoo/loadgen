# loadgen
Simple SpringBoot Application generating system loads
loadgen docker + kubernetes deployment steps
1. make the target using maven build
.. mvn package
2. use docker command to build the image
.. docker build -t "howardyoo/loadgen:0.0.4" -t "howardyoo/loadgen:latest" .
3. docker login
.. provide user and password
4. docker push "howardyoo/loadgen:0.0.4"
