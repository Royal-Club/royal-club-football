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
                withCredentials([
                    string(credentialsId: 'TEAM_LOGO_STORAGE_PROVIDER', variable: 'TEAM_LOGO_STORAGE_PROVIDER'),
                    string(credentialsId: 'TEAM_LOGO_R2_ENDPOINT',      variable: 'TEAM_LOGO_R2_ENDPOINT'),
                    string(credentialsId: 'TEAM_LOGO_R2_ACCESS_KEY',    variable: 'TEAM_LOGO_R2_ACCESS_KEY'),
                    string(credentialsId: 'TEAM_LOGO_R2_SECRET_KEY',    variable: 'TEAM_LOGO_R2_SECRET_KEY'),
                    string(credentialsId: 'TEAM_LOGO_R2_BUCKET',        variable: 'TEAM_LOGO_R2_BUCKET')
                ]) {
                    // Write env file using single-quoted sh to avoid Groovy interpolation of secrets
                    sh '''
                        cat > /tmp/rcf-app.env << EOF
TEAM_LOGO_STORAGE_PROVIDER=$TEAM_LOGO_STORAGE_PROVIDER
TEAM_LOGO_R2_ENDPOINT=$TEAM_LOGO_R2_ENDPOINT
TEAM_LOGO_R2_ACCESS_KEY=$TEAM_LOGO_R2_ACCESS_KEY
TEAM_LOGO_R2_SECRET_KEY=$TEAM_LOGO_R2_SECRET_KEY
TEAM_LOGO_R2_BUCKET=$TEAM_LOGO_R2_BUCKET
TEAM_LOGO_BASE_URL=https://royalfootball.club
EOF
                        chmod 600 /tmp/rcf-app.env
                    '''
                    sh """
                        sudo mkdir -p ${APP_DIR}

                        # Move env file into place
                        sudo cp /tmp/rcf-app.env ${APP_DIR}/app.env
                        sudo chown ubuntu:ubuntu ${APP_DIR}/app.env
                        rm -f /tmp/rcf-app.env

                        # Copy the built JAR
                        sudo cp target/${JAR_NAME} ${APP_DIR}/app.jar
                        sudo chown ubuntu:ubuntu ${APP_DIR}/app.jar

                        # Restart the systemd service
                        sudo systemctl daemon-reload
                        sudo systemctl restart ${SERVICE}

                        # Wait and verify
                        sleep 5
                        sudo systemctl is-active ${SERVICE}
                    """
                }
            }
        }
    }

    post {
        success { echo 'Royal Club Football API deployment successful!' }
        failure { echo 'Royal Club Football API deployment failed. Check logs above.' }
    }
}
