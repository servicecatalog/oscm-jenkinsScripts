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
	        if ( "${http_proxy}" != ''  && "${https_proxy}" != '') {
                URI httpUri = new URI("${http_proxy}") 
                URI httpsUri = new URI("${https_proxy}") 
                def httpHost = httpUri?.getHost()
                def httpPort = httpUri?.getPort()
                def httpsHost = httpsUri?.getHost()
                def httpsPort = httpsUri?.getPort()
                env.ANT_OPTS="-Dhttp.proxyHost=${httpHost} -Dhttp.proxyPort=${httpPort} -Dhttps.proxyHost=${httpsHost} -Dhttps.proxyPort=${httpsPort}"
                env.MAVEN_OPTS="-Xmx512m -Dhttp.proxyHost=${httpHost} -Dhttp.proxyPort=${httpPort} -Dhttps.proxyHost=${httpsHost} -Dhttps.proxyPort=${httpsPort}"
            } else if ( "${http_proxy}" != '') {
                URI httpUri = new URI("${http_proxy}") 
                env.ANT_OPTS="-Dhttp.proxyHost=httpUri?.getHost() -Dhttp.proxyPort=httpUri?.getPort()"
                env.MAVEN_OPTS="-Xmx512m -Dhttp.proxyHost=httpUri?.getHost() -Dhttp.proxyPort=httpUri?.getPort()"
            } else if ( "${https_proxy}" != '' ) {
                URI httpsUri = new URI("${https_proxy}") 
                env.ANT_OPTS="-Dhttps.proxyHost=httpsUri?.getHost() -Dhttps.proxyPort=httspUri?.getPort()"
                env.MAVEN_OPTS="-Xmx512m -Dhttp.proxyHost= -Dhttps.proxyHost=httpsUri?.getHost() -Dhttps.proxyPort=httspUri?.getPort()"
           } else {
                env.ANT_OPTS=""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build"
           }       
       }
   }  
   
    _setProxyVariables()
}
return this