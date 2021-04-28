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
        script {
        ANT_OPTS
        MAVEN_OPTS
         sh '''
            if [ -n ${http_proxy} ] && [ -n ${https_proxy} ] && [ -n ${http_port} ] && [ -n ${https_port} ]; then
               PROXY_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
               
               ANT_OPTS="-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
               MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
            elif [ -n ${http_proxy} ] && [ -n ${http_port} ]; then
                PROXY_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
                ANT_OPTS="-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
               MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
            elif [ -n ${https_proxy} ] && [ -n ${https_port} ]; then
                PROXY_OPTS="-Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
                ANT_OPTS="-Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
               MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=proxy.intern.est.fujitsu.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.intern.est.fujitsu.com -Dhttps.proxyPort=8080"
            else
               PROXY_OPTS=""
               ANT_OPTS=""
               MAVEN_OPTS=""
            fi;
            '''
       }
       }
   }             
     def _echo = {
          stage('Print Varliabe'){
            steps{
                echo ${ANT_OPTS}
                echo ${MAVEN_OPTS}
            }
        }
    }
    
    _setProxyVariables()
    _echo
}
return this