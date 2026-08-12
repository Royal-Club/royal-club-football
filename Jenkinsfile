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
                    string(credentialsId: 'TEAM_LOGO_R2_BUCKET',        variable: 'TEAM_LOGO_R2_BUCKET'),
                    string(credentialsId: 'FIREBASE_CREDENTIALS_JSON',  variable: 'FIREBASE_CREDENTIALS_JSON'),
                    // Resend SMTP password. Without it the mail password is blank and every
                    // invitation, dues reminder and password-reset link fails to send.
                    string(credentialsId: 'RESEND_API_KEY',             variable: 'RESEND_API_KEY'),
                    // Link-signing keys. These MUST come from here rather than the defaults in
                    // application.yml: those defaults are committed, so anyone who can read the
                    // repo could forge a password-reset link and take over an account.
                    string(credentialsId: 'PASSWORD_RESET_TOKEN_SECRET', variable: 'PASSWORD_RESET_TOKEN_SECRET'),
                    string(credentialsId: 'RSVP_TOKEN_SECRET',          variable: 'RSVP_TOKEN_SECRET')
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
FIREBASE_CREDENTIALS_JSON=$FIREBASE_CREDENTIALS_JSON
RESEND_API_KEY=$RESEND_API_KEY
PASSWORD_RESET_TOKEN_SECRET=$PASSWORD_RESET_TOKEN_SECRET
RSVP_TOKEN_SECRET=$RSVP_TOKEN_SECRET
# Every emailed link is built from this. Left unset it defaults to localhost:3000,
# which sends members a link that only works on the developer's own machine.
APP_FRONTEND_BASE_URL=https://royalfootball.club
EOF
                        chmod 600 /tmp/rcf-app.env
                    '''
                    sh """
                        sudo mkdir -p ${APP_DIR}

                        # Move env file into place
                        sudo cp /tmp/rcf-app.env ${APP_DIR}/app.env
                        sudo chown ubuntu:ubuntu ${APP_DIR}/app.env
                        rm -f /tmp/rcf-app.env

                        # Ensure systemd service loads app env vars
                        sudo mkdir -p /etc/systemd/system/${SERVICE}.service.d
                        cat > /tmp/10-env.conf << EOF
[Service]
EnvironmentFile=-${APP_DIR}/app.env
EOF
                        sudo cp /tmp/10-env.conf /etc/systemd/system/${SERVICE}.service.d/10-env.conf
                        rm -f /tmp/10-env.conf

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
