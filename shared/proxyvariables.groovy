/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2021                                            *
 *                                                                           *
 * Creation Date: 26-04-2021                                                 *
 *                                                                           *
 ****************************************************************************/
 
void execute() {


    def _setProxyVariables = {
    
    environment {
        env.ANT_OPTS
        env.MAVEN_OPTS
    }
        stage('Build - set proxy variables') {
        script {
        env.ANT_OPTS
        env.MAVEN_OPTS
         sh '''
            if [ -n ${http_proxy} ] && [ -n ${https_proxy} ] && [ -n ${http_port} ] && [ -n ${https_port} ]; then
               PROXY_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
               
               ANT_OPTS="-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
               MAVEN_OPTS"-Xmx512m -Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
            elif [ -n ${http_proxy} ] && [ -n ${http_port} ]; then
                PROXY_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
            elif [ -n ${https_proxy} ] && [ -n ${https_port} ]; then
                PROXY_OPTS="-Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
            else
               PROXY_OPTS=""
            fi;
            '''
       }
       }
   }             
    _setProxyVariables()
}
return this