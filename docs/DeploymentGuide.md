Here's the complete fresh guide for `royalfootball.club` backend:

---

## Step 1 — On the server: install Maven (if not already)

```bash
mvn -v
# if not found:
sudo apt install -y maven
```

---

## Step 2 — Create the app directory

```bash
sudo mkdir -p /home/application/royalfootball.club
sudo chown -R ubuntu:ubuntu /home/application/royalfootball.club
```

---

## Step 3 — Create the systemd service

```bash
sudo nano /etc/systemd/system/royal-club-api.service
```

Paste:

```ini
[Unit]
Description=Royal Club Football API
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/application/royalfootball.club
ExecStart=/usr/bin/java -jar /home/application/royalfootball.club/app.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable royal-club-api
sudo systemctl start royal-club-api
sudo systemctl is-active royal-club-api
```

---

## Step 4 — Add sudoers rules for Jenkins

```bash
sudo visudo
```

Append at the bottom:
```
jenkins ALL=(ALL) NOPASSWD: /bin/systemctl restart royal-club-api, /bin/systemctl is-active royal-club-api
```

---

## Step 5 — Add GitHub credentials in Jenkins (if not already)

> If the `Royal-Club` GitHub org is a different account than `badrulme`, add a new credential:

**Manage Jenkins → Credentials → System → Global → Add Credentials**
- Kind: `Username with password`
- Username: GitHub username with access to `Royal-Club` org
- Password: GitHub Fine-grained PAT (Contents: Read-only for `royal-club-football` repo)
- ID: `github-creds-brfc`

---

## Step 6 — Create the Jenkins pipeline job

1. **New Item** → `royal-club-football-api` → **Pipeline** → OK
2. **General** → GitHub project: `https://github.com/Royal-Club/royal-club-football/`
3. **Build Triggers** → leave unchecked (manual)
4. **Pipeline** → Pipeline script from SCM
   - SCM: `Git`
   - Repo: `https://github.com/Royal-Club/royal-club-football.git`
   - Credentials: `github-creds-brfc`
   - Branch: `*/master`
   - Script Path: Jenkinsfile
5. **Save**

---

## Step 7 — Commit and push the Jenkinsfile

```bash
cd "D:\Others\BRFC\royal-club-football"
git add Jenkinsfile
git commit -m "ci: add Jenkins pipeline for Royal Club Football API"
git push
```

---

## Step 8 — Build Now

In Jenkins, open `royal-club-football-api` and click **Build Now**.

Pipeline stages:
1. Checkout from GitHub
2. `mvn clean package -DskipTests` → builds `royal-club-football-v1.0.0.jar`
3. Copies JAR to `/home/application/royalfootball.club/app.jar`
4. `systemctl restart royal-club-api`
5. Verifies service is active 

