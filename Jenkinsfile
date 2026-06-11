pipeline {
    agent any

    environment {
        APP_DIR     = '/home/application/royalfootball.club'
        JAR_NAME    = 'royal-club-football-v1.0.0.jar'
        SERVICE     = 'royal-club-api'
        PATH        = "/usr/bin:/usr/local/bin:${env.PATH}"

        // Team logo R2 credentials
        TEAM_LOGO_STORAGE_PROVIDER  = credentials('TEAM_LOGO_STORAGE_PROVIDER')
        RCF_R2_ENDPOINT             = credentials('RCF_R2_ENDPOINT')
        RCF_R2_ACCESS_KEY_ID        = credentials('RCF_R2_ACCESS_KEY_ID')
        RCF_R2_SECRET_ACCESS_KEY    = credentials('RCF_R2_SECRET_ACCESS_KEY')
        RCF_R2_BUCKET               = credentials('RCF_R2_BUCKET')
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

                    # Write environment variables for systemd
                    sudo bash -c 'cat > ${APP_DIR}/app.env <<EOF
TEAM_LOGO_STORAGE_PROVIDER=${TEAM_LOGO_STORAGE_PROVIDER}
TEAM_LOGO_R2_ENDPOINT=${RCF_R2_ENDPOINT}
TEAM_LOGO_R2_ACCESS_KEY=${RCF_R2_ACCESS_KEY_ID}
TEAM_LOGO_R2_SECRET_KEY=${RCF_R2_SECRET_ACCESS_KEY}
TEAM_LOGO_R2_BUCKET=${RCF_R2_BUCKET}
TEAM_LOGO_BASE_URL=https://royalfootball.club
EOF'
                    sudo chown ubuntu:ubuntu ${APP_DIR}/app.env
                    sudo chmod 600 ${APP_DIR}/app.env

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
