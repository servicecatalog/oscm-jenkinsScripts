/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2021                                            *
 *                                                                           *
 * Creation Date: 26-04-2021                                                 *
 *                                                                           *
 ****************************************************************************/

void execute() {


    def _setProxyVariables = {
    

        stage('Build - set proxy variables') { 
            http = splitProxy("${http_proxy}")
            https = splitProxy("${https_proxy}")
            def httpHost = http[0]
            def httpsHost = https[0]
            def httpPort = getPort(http)
            def httpsPort = getPort(https)
                
            env.RUN_PROXY_ARGS ="-e http_proxy=\"${http_proxy}\" -e https_proxy=\"${https_proxy}\" -e HTTP_PROXY=\"${http_proxy}\" -e HTTPS_PROXY=\"${https_proxy}\""
            env.BUILD_PROXY_ARGS="--build-arg http_proxy=\"${http_proxy}\" --build-arg https_proxy=\"${https_proxy}\" --build-arg HTTP_PROXY=\"${http_proxy}\" --build-arg HTTPS_PROXY=\"${https_proxy}\" " 
                
	        if ( "${http_proxy}" != ''  && "${https_proxy}" != '') {
                env.ANT_OPTS="-Dhttp.proxyHost=${httpHost} -Dhttp.proxyPort=${httpPort} -Dhttps.proxyHost=${httpsHost} -Dhttps.proxyPort=${httpsPort}"
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=${httpHost} -Dhttp.proxyPort=${httpPort} -Dhttps.proxyHost=${httpsHost} -Dhttps.proxyPort=${httpsPort}"
            } else if ( "${http_proxy}" != '') {
                env.ANT_OPTS="-Dhttp.proxyHost=${httpHost} -Dhttp.proxyPort=${httpPort} "
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=${httpHost} -Dhttp.proxyPort=${httpPort}"
            } else if ( "${https_proxy}" != '' ) {
                env.ANT_OPTS=" -Dhttps.proxyHost=${httpsHost} -Dhttps.proxyPort=${httpsPort}"
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttps.proxyHost=${httpsHost} -Dhttps.proxyPort=${httpsPort}"
           } else {
                env.ANT_OPTS=""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build"
           }       
       }
   }  
   
    _setProxyVariables()
}

def getPort(String[] proxy) {
    def port
    if( proxy.length > 1) {
        port = proxy[1]
    }
    return port
} 

def splitProxy(String proxy) {
    return proxy.replaceAll(".*://", "").split(':');
} 
return this