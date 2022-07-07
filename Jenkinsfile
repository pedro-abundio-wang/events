pipeline {
    agent any

    parameters {
        string(name: 'KUBERNETES_MASTER', defaultValue: '', description: 'Kubernetes master node ip address')
        string(name: 'KUBERNETES_NAMESPACE', defaultValue: '', description: 'Deploy application to specific namespace')
        string(name: 'BUILD_VERSION', defaultValue: 'v0.0.1', description: 'Build version')
        string(name: 'DOCKER_IMAGE_REPOSITORY', defaultValue: '10.110.38.26:18080/library', description: 'Docker image repository')
    }

    stages {
        stage('Build') {
            steps {
                sh 'printenv | sort'

                sh './gradlew :events-db:events-postgres:dockerBuildImage'
                sh './gradlew :events-db:events-postgres:dockerPushImage'

                sh './gradlew :events-cdc:events-cdc-service:clean'
                sh './gradlew :events-cdc:events-cdc-service:build'
                sh './gradlew :events-cdc:events-cdc-service:bootBuildImage'
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
                    kubernetes_master.user = 'root'
                    kubernetes_master.password = 'root'
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
                    sshCommand remote: kubernetes_master, command: "helm upgrade --install events-postgres /opt/events-postgres/helm --set image.tag=${params.BUILD_VERSION} --set image.repository=${params.DOCKER_IMAGE_REPOSITORY}/events-postgres --namespace ${params.KUBERNETES_NAMESPACE}"
                    // deploying events-cdc-service
                    sshCommand remote: kubernetes_master, command: "mkdir -p /opt/events-cdc-service"
                    sshPut remote: kubernetes_master, from: './events-cdc/events-cdc-service/deployment/helm', into: '/opt/events-cdc-service'
                    sshCommand remote: kubernetes_master, command: "helm upgrade --install events-cdc-service /opt/events-cdc-service/helm --set image.tag=${params.BUILD_VERSION} --set image.repository=${params.DOCKER_IMAGE_REPOSITORY}/events-cdc-service --namespace ${params.KUBERNETES_NAMESPACE}"
                }
            }
        }
    }
}