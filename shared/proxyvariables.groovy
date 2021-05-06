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
            env.httpHost = http[0]
            env.httpsHost = https[0]
            env.httpPort = getPort(http)
            env.httpsPort = getPort(https)
                
            env.RUN_PROXY_ARGS ="-e http_proxy=\"${http_proxy}\" -e https_proxy=\"${https_proxy}\" -e HTTP_PROXY=\"${http_proxy}\" -e HTTPS_PROXY=\"${https_proxy}\""
            env.BUILD_PROXY_ARGS="--build-arg http_proxy=\"${http_proxy}\" --build-arg https_proxy=\"${https_proxy}\" --build-arg HTTP_PROXY=\"${http_proxy}\" --build-arg HTTPS_PROXY=\"${https_proxy}\" " 
            env.PROXY_OPTS=""
            
            env.PROXY_OPTS = appendIfSet(env.PROXY_OPTS, "-Dhttps.proxyHost", httpsHost)
            env.PROXY_OPTS = appendIfSet(env.PROXY_OPTS, "-Dhttp.proxyHost", httpHost)
            env.PROXY_OPTS = appendIfSet(env.PROXY_OPTS, "-Dhttps.proxyPort", httpsPort)
            env.PROXY_OPTS = appendIfSet(env.PROXY_OPTS, "-Dhttp.proxyPort", httpPort)
            env.MAVEN_OPTS="-Xmx512m -Duser.home=/build ${env.PROXY_OPTS}"
            env.ANT_OPTS="${env.PROXY_OPTS}"
            
            env.httpRequried = setOidcProxyRequired(httpHost)
            env.httpsRequried = setOidcProxyRequired(httpsHost)
            
            

       }
   }  
   
    _setProxyVariables()
}

def setHost(String host) {
    if( host == '') {
        host = "noProxy"
    }
    return host
}

def setOidcProxyRequired(String val) {
	required = "false"
    if( val != '' ) {
        required = "true"
    }
    return required
}

def setPort(String port) {
    if( port == '') {
        port = "0"
    }
    return port
}


def getPort(String[] proxy) {
    def port = ""
    if( proxy.length > 1) {
        port = proxy[1]
    }
    return port
} 

def appendIfSet(String opt, String arg, String val) {
    if ( val != '') {
        opt = "${opt} ${arg}=${val}"
    }
    return opt
} 

def splitProxy(String proxy) {
    return proxy.replaceAll(".*://", "").split(':');
} 
return this