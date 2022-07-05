pipeline {
    agent any

    parameters {
        string(name: 'KUBERNETES_MASTER', defaultValue: '', description: 'Kubernetes master node ip address')
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
                echo 'Deploying RabbitMQ'
                sh '''
                    ssh root@${params.KUBERNETES_MASTER} << 'EOF'
                    helm upgrade --install events-rabbitmq bitnami/rabbitmq --namespace events-cdc --set extraPlugins=rabbitmq_consistent_hash_exchange
                    exit
                '''
                sh ''
                echo 'Deploying ActiveMQ'
                echo 'Deploying Redis'
                sh '''
                    ssh root@${params.KUBERNETES_MASTER} << 'EOF'
                    helm upgrade --install events-redis bitnami/redis --namespace events-cdc
                    exit
                '''
                echo 'Deploying Kafka/Zookeeper'
                sh '''
                    ssh root@${params.KUBERNETES_MASTER} << 'EOF'
                    helm upgrade --install events-kafka bitnami/kafka --namespace events-cdc
                    exit
                '''
                echo 'Deploying Events Postgres'
                sh '''
                    scp -r ./events-db/events-postgres/deployment/kubernetes root@${params.KUBERNETES_MASTER}:/opt/
                    ssh root@${params.KUBERNETES_MASTER} << 'EOF'
                    mv -f /opt/kubernetes /opt/events-db-kubernetes
                    kubectl apply -f /opt/events-db-kubernetes
                    exit
                '''
                echo 'Deploying Events Cdc Service'
                sh '''
                    scp -r ./events-cdc/events-cdc-service/deployment/kubernetes root@${params.KUBERNETES_MASTER}:/opt/
                    ssh root@${params.KUBERNETES_MASTER} << 'EOF'
                    mv -f /opt/kubernetes /opt/events-cdc-kubernetes
                    kubectl apply -f /opt/events-cdc-kubernetes
                    exit
                '''
            }
        }
    }
}