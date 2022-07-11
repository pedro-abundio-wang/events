pipeline {

    agent any

    options {
        // timeout period for the pipeline run
        timeout(time: 1, unit: 'HOURS')
        // skip checkout scm if you want for debug
        // skipDefaultCheckout()
    }

    parameters {
        // kubernetes master parameters
        string(name: 'KUBERNETES_MASTER', defaultValue: '10.110.38.32', description: 'Kubernetes master node ip address')
        string(name: 'KUBERNETES_MASTER_OS_USER', defaultValue: 'root', description: 'Kubernetes master node os username')
        string(name: 'KUBERNETES_MASTER_OS_PASS', defaultValue: 'root', description: 'Kubernetes master node os password')
        // build parameters
        string(name: 'BUILD_VERSION', defaultValue: 'v0.0.1', description: 'Build version')
        // kubernetes deployment parameters
        string(name: 'KUBERNETES_NAMESPACE', defaultValue: 'events-cdc', description: 'Deploy application to specific namespace')
        // harbor docker
        // the following 2 parameters can not name 'DOCKER_HOST' and 'DOCKER_PORT'
        // because unknown fucking bugs, guess those key word conflict with jenkins
        string(name: 'HARBOR_DOCKER_HOST', defaultValue: '10.110.38.26', description: 'Harbor docker host')
        string(name: 'HARBOR_DOCKER_PORT', defaultValue: '2375', description: 'Harbor docker port')
        // harbor
        string(name: 'HARBOR_HOST', defaultValue: '10.110.38.26', description: 'Harbor host')
        string(name: 'HARBOR_PORT', defaultValue: '18080', description: 'Harbor port')
        string(name: 'HARBOR_PROJECT', defaultValue: 'library', description: 'Harbor project')
        string(name: 'HARBOR_USER', defaultValue: 'admin', description: 'Harbor user')
        string(name: 'HARBOR_PASS', defaultValue: 'Harbor12345', description: 'Harbor password')
    }

    stages {
        stage('Build') {
            steps {
                sh 'printenv | sort'

                sh './gradlew clean'

                sh """
                    ./gradlew :events-db:events-postgres:dockerBuildImage \
                        -PbuildVersion=${params.BUILD_VERSION} \
                        -PdockerHost=${params.HARBOR_DOCKER_HOST} \
                        -PdockerPort=${params.HARBOR_DOCKER_PORT} \
                        -PharborHost=${params.HARBOR_HOST} \
                        -PharborPort=${params.HARBOR_PORT} \
                        -PharborProject=${params.HARBOR_PROJECT} \
                        -PharborUser=${params.HARBOR_USER} \
                        -PharborPass=${params.HARBOR_PASS}
                """
                sh """
                    ./gradlew :events-db:events-postgres:dockerPushImage \
                        -PbuildVersion=${params.BUILD_VERSION} \
                        -PdockerHost=${params.HARBOR_DOCKER_HOST} \
                        -PdockerPort=${params.HARBOR_DOCKER_PORT} \
                        -PharborHost=${params.HARBOR_HOST} \
                        -PharborPort=${params.HARBOR_PORT} \
                        -PharborProject=${params.HARBOR_PROJECT} \
                        -PharborUser=${params.HARBOR_USER} \
                        -PharborPass=${params.HARBOR_PASS}
                """

                sh """
                    ./gradlew :events-cdc:events-cdc-service:build \
                        -PbuildVersion=${params.BUILD_VERSION} \
                        -PdockerHost=${params.HARBOR_DOCKER_HOST} \
                        -PdockerPort=${params.HARBOR_DOCKER_PORT} \
                        -PharborHost=${params.HARBOR_HOST} \
                        -PharborPort=${params.HARBOR_PORT} \
                        -PharborProject=${params.HARBOR_PROJECT} \
                        -PharborUser=${params.HARBOR_USER} \
                        -PharborPass=${params.HARBOR_PASS}
                """
                // Using Cloud Native Buildpacks. You do not need a Dockerfile any more!!!
                sh """
                    ./gradlew :events-cdc:events-cdc-service:bootBuildImage \
                        -PbuildVersion=${params.BUILD_VERSION} \
                        -PdockerHost=${params.HARBOR_DOCKER_HOST} \
                        -PdockerPort=${params.HARBOR_DOCKER_PORT} \
                        -PharborHost=${params.HARBOR_HOST} \
                        -PharborPort=${params.HARBOR_PORT} \
                        -PharborProject=${params.HARBOR_PROJECT} \
                        -PharborUser=${params.HARBOR_USER} \
                        -PharborPass=${params.HARBOR_PASS}
                """
            }
        }
        stage('Test') {
            steps {
                echo 'Unit Test'
                echo 'Integration Test'
                echo 'Component Test'
                echo 'E2E Test'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    // remote kubernetes master server
                    def kubernetes_master = [:]
                    kubernetes_master.name = 'kubernetes_master_node'
                    kubernetes_master.host = params.KUBERNETES_MASTER
                    kubernetes_master.user = params.KUBERNETES_MASTER_OS_USER
                    kubernetes_master.password = params.KUBERNETES_MASTER_OS_PASS
                    kubernetes_master.allowAnyHosts = true
                    // create kubernetes namespace if it does not exist
                    sshCommand remote: kubernetes_master, command: "kubectl create namespace ${params.KUBERNETES_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"
                    // deploying rabbitmq
                    sshCommand remote: kubernetes_master, command: "helm upgrade --install events-rabbitmq bitnami/rabbitmq --namespace ${params.KUBERNETES_NAMESPACE} --set extraPlugins=rabbitmq_consistent_hash_exchange"
                    // deploying redis
                    sshCommand remote: kubernetes_master, command: "helm upgrade --install events-redis bitnami/redis --namespace ${params.KUBERNETES_NAMESPACE}"
                    // deploying kafka
                    sshCommand remote: kubernetes_master, command: "helm upgrade --install events-kafka bitnami/kafka --namespace ${params.KUBERNETES_NAMESPACE}"
                    // deploying events-postgres
                    sshCommand remote: kubernetes_master, command: "mkdir -p /opt/events-postgres"
                    sshPut remote: kubernetes_master, from: './events-db/events-postgres/deployment/helm', into: '/opt/events-postgres'
                    sshCommand remote: kubernetes_master, command: "helm upgrade --install events-postgres /opt/events-postgres/helm --set image.tag=${params.BUILD_VERSION} --set image.repository=${params.HARBOR_HOST}:${params.HARBOR_PORT}/${params.HARBOR_PROJECT}/events-postgres --namespace ${params.KUBERNETES_NAMESPACE}"
                    // deploying events-cdc-service
                    sshCommand remote: kubernetes_master, command: "mkdir -p /opt/events-cdc-service"
                    sshPut remote: kubernetes_master, from: './events-cdc/events-cdc-service/deployment/helm', into: '/opt/events-cdc-service'
                    sshCommand remote: kubernetes_master, command: "helm upgrade --install events-cdc-service /opt/events-cdc-service/helm --set image.tag=${params.BUILD_VERSION} --set image.repository=${params.HARBOR_HOST}:${params.HARBOR_PORT}/${params.HARBOR_PROJECT}/events-cdc-service --namespace ${params.KUBERNETES_NAMESPACE}"
                }
            }
        }
    }
}