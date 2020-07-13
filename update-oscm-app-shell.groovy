/**
 How to use:
 1. Create a job of type Pipeline
 2. As the pipeline script paste this script
 3. Create the following parameters (adjust defaults as you wish):

 Required environment variables:
 ===
 Name: NODE_NAME
 Type: Option list
 Default: -
 Description: Nodes used to run the pipeline.
 ===
 Name: DOCKER_TAG
 Type: String
 Default: latest
 Description: Docker tag name for created images
 ===
 Name: DOCKER_REGISTRY
 Type: String
 Default: artifactory.intern.est.fujitsu.com:5003
 Description: Registry host and port for the final Docker image. Example: <DOCKER_REGISTRY>/<DOCKER_ORGANIZATION>/oscm-core:<DOCKER_TAG>
 ===
 Name: DOCKER_ORGANIZATION
 Type: String
 Default: oscmdocker
 Description: Organization name for the final Docker image. Example: <DOCKER_REGISTRY>/<DOCKER_ORGANIZATION>/oscm-core:<DOCKER_TAG>
 ===
 Name: REPO_TAG_OSCM
 Type: String
 Default: master
 Description: Branch or tag in the oscm git repository: https://github.com/servicecatalog/oscm
 ===
 Name: REPO_TAG_DOCKERBUILD
 Type: String
 Default: master
 Description: Branch or tag in the oscm-dockerbuild git repository: https://github.com/servicecatalog/oscm-dockerbuild
 ===
 Name: MAVEN_URL
 Type: String
 Default: https://apache.osuosl.org/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz
 Description: URL to download maven from.
 **/

node("${NODE_NAME}") {
    def update = evaluate readTrusted('updates/oscm-app-shell.groovy')

    update.execute()
}
