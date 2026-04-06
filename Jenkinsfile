pipeline {
    agent any

    environment {
        APP_DIR     = '/home/application/royalfootball.club'
        JAR_NAME    = 'royal-club-football-v1.0.0.jar'
        SERVICE     = 'royal-club-api'
        PATH        = "/usr/bin:/usr/local/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    # Create deploy dir if not exists
                    sudo -u ubuntu mkdir -p ${APP_DIR}

                    # Copy the built JAR
                    sudo cp target/${JAR_NAME} ${APP_DIR}/app.jar
                    sudo chown ubuntu:ubuntu ${APP_DIR}/app.jar

                    # Restart the systemd service
                    sudo systemctl restart ${SERVICE}

                    # Wait and verify
                    sleep 5
                    sudo systemctl is-active ${SERVICE}
                """
            }
        }
    }

    post {
        success { echo 'Royal Club Football API deployment successful!' }
        failure { echo 'Royal Club Football API deployment failed. Check logs above.' }
    }
}
