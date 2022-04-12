# Localstack + Cucumber + Spring Tests



``
Localstack Docker version 0.9.1 works with LocalstackUtils 0.1.15 however the runner only run with latest version and will check and download the latest localstack if it couldn't find one. In order to go around this issue do the following:
``

	
##### pull docker image for localstack/localstack:0.9.1 and then tag it to localstack/localstack:latest	
set pullNewImage = false in the LocalstackDockerProperties`


 

- 1 passo : `docker login docker-devel.depdes.artifactory.prod.cloud.ihf`
- 2 passo:  `docker pull docker-devel.depdes.artifactory.prod.cloud.ihf/localstack:0.9.1`
- 3 passo:  `tag docker-devel.depdes.artifactory.prod.cloud.ihf/localstack/localstack:0.9.1 localstack/localstack:latestd.cloud.ihf/localstack/localstack:0.9.1k:0.9.1 localstack/localstack:latestifactory.prod.cloud.ihf/localstack/localstack`
