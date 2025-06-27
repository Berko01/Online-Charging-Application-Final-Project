pipeline {
    agent any


    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/RamazanAkdag/Online-Charging-Application-Final-Project'
            }
        }

        stage('Compile') {
            steps {
                withMaven(globalMavenSettingsConfig: '', jdk: 'jdk17', maven: 'maven3', mavenSettingsConfig: '', traceability: true) {
                    sh "mvn compile -DskipTests"
                }
            }
        }

    //    stage('Test') {
    //      steps{
    //              withMaven(globalMavenSettingsConfig: '', jdk: 'jdk17', maven: 'maven3', mavenSettingsConfig: '', traceability: true) {
    //                    sh "mvn test"
     //             }  
       //     }
      //  }

        stage('File System Scan') {
            steps {
                sh "trivy fs --format table -o trivy-fs-report.html ."
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh '''
                    SONAR_TOKEN=$(cat /opt/secrets/sonarqube_secret)
                    SONAR_HOST=$(cat /opt/secrets/sonarqube_ip)
        
                    $SCANNER_HOME/bin/sonar-scanner \
                        -Dsonar.projectName=OCS \
                        -Dsonar.projectKey=OCS \
                        -Dsonar.java.binaries=. \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.host.url=http://$SONAR_HOST:9000 \
                        -Dsonar.exclusions=**/settings.xml,**/trivy-fs-report.html,**/trivy-scan-*.html \
                    | tee sonar-log.txt
                '''
            }
        }

        stage('Quality Gate') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    script {
                        sleep 5 // SonarQube sunucusunun analizi işlemesi için ilk bekleme

                        echo "📄 Sonar Log İçeriği:"
                        def sonarLog = readFile('sonar-log.txt')
                        echo sonarLog

                        // 🔍 taskId'yi logdan çek
                        def taskId = sh(
                            script: "grep -o 'task?id=[^\" ]*' sonar-log.txt | cut -d'=' -f2",
                            returnStdout: true
                        ).trim()

                        if (!taskId) {
                            error("❌ taskId bulunamadı! 'sonar-log.txt' içinde 'task?id=' satırı yok.")
                        }

                        // 🔐 Secret'ları al
                        def sonarToken = sh(script: "cat /opt/secrets/sonarqube_secret", returnStdout: true).trim()
                        def sonarHost = sh(script: "cat /opt/secrets/sonarqube_ip", returnStdout: true).trim()

                        // 🔄 analysisId için retry mekanizması
                        def analysisId = ''
                        def retries = 10

                        for (int i = 0; i < retries; i++) {
                            analysisId = sh(
                                script: "curl -s -u ${sonarToken}: http://${sonarHost}:9000/api/ce/task?id=${taskId} | jq -r '.task.analysisId'",
                                returnStdout: true
                            ).trim()

                            if (analysisId && analysisId != "null") {
                                echo "✅ Analysis ID bulundu: ${analysisId}"
                                break
                            }

                            echo "⏳ Analysis ID henüz hazır değil, 5 saniye bekleniyor... (Deneme ${i+1}/${retries})"
                            sleep 5
                        }

                        if (!analysisId || analysisId == "null") {
                            error("❌ Analysis ID alınamadı, analiz tamamlanmamış olabilir.")
                        }

                        // ✅ Quality Gate kontrolü
                        def gateStatus = sh(
                            script: "curl -s -u ${sonarToken}: http://${sonarHost}:9000/api/qualitygates/project_status?analysisId=${analysisId} | jq -r '.projectStatus.status'",
                            returnStdout: true
                        ).trim()

                        echo "✅ Quality Gate sonucu: ${gateStatus}"

                        if (gateStatus != "OK") {
                            echo "❌ Quality Gate başarısız: ${gateStatus}"
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                }
            }
        }


        stage('Inject Nexus Info into Parent POM') {
            steps {
                script {
                    def nexusIp = sh(script: "cat /opt/secrets/nexus_ip", returnStdout: true).trim()

                    sh '''
                        NEXUS_IP=$(cat /opt/secrets/nexus_ip)
                        sed -i '/<\\/project>/i \
                        <distributionManagement>\\n\
                            <repository>\\n\
                            <id>maven-releases</id>\\n\
                            <url>http://'"$NEXUS_IP"':8081/repository/maven-releases/</url>\\n\
                            </repository>\\n\
                            <snapshotRepository>\\n\
                            <id>maven-snapshots</id>\\n\
                            <url>http://'"$NEXUS_IP"':8081/repository/maven-snapshots/</url>\\n\
                            </snapshotRepository>\\n\
                        </distributionManagement>' pom.xml
                        '''

                }
            }
        }

        stage('Create Maven Settings with Nexus Credentials') {
            steps {
                script {
                    def nexusUser = "admin"
                    def nexusPass = sh(script: "cat /opt/secrets/nexus_password", returnStdout: true).trim()

                    def settingsXml = """
        <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <servers>
            <server>
            <id>maven-releases</id>
            <username>${nexusUser}</username>
            <password>${nexusPass}</password>
            </server>
            <server>
            <id>maven-snapshots</id>
            <username>${nexusUser}</username>
            <password>${nexusPass}</password>
            </server>
        </servers>
        </settings>
        """
                    writeFile file: 'settings.xml', text: settingsXml
                }
            }
        }



        stage('Deploy Artifacts to Nexus') {
            steps {
                script {
                    sh "mvn clean deploy -DskipTests --settings settings.xml"
                }
            }
        }

        stage('Set Image List') {
            steps {
                script {
                    def tmpList = [
                        'account-order-management',
                        'account-balance-management-function',
                        'charging-gateway-function',
                        'diameter-gateway',
                        'notification-function',
                        'online-charging-system',
                        'traffic-generator-function'
                    ]
                    imageList = tmpList
                }
            }
        }



        stage('Dockerize with Jib') {
            steps {
                script {
                    def dockerUser = "beko2001"
                    def dockerPass = sh(
                        script: 'cat /opt/secrets/docker_secret',
                        returnStdout: true
                    ).trim()

                    imageList.each { imageName ->
                        sh """
                            mvn -pl ${imageName} jib:dockerBuild \
                            -Ddocker.username=${dockerUser} \
                            -Ddocker.password=${dockerPass}
                        """
                    }
                }
            }
        }

        stage('Docker Image Scan') {
            steps {
                script {
                    imageList.each { imageName ->
                        def fullImageName = "beko2001/${imageName}"
                        def reportName = "trivy-scan-${imageName.replaceAll('-', '_')}.html"
                        sh "trivy image --format table -o ${reportName} ${fullImageName}:latest || true"
                    }
                }
            }
        }
        stage('Push Images to Docker Hub') {
            steps {
                script {
                    def dockerUser = "beko2001"
                    def dockerPass = sh(
                        script: 'cat /opt/secrets/docker_secret',
                        returnStdout: true
                    ).trim()

                    imageList.each { imageName ->
                        def fullImageName = "beko2001/${imageName}"
                        sh """
                            echo "${dockerPass}" | docker login -u "${dockerUser}" --password-stdin
                            docker push ${fullImageName}:latest
                        """
                    }
                }
            }
}
        stage('Deploy To Kubernetes') {
            steps {
                withEnv(["KUBECONFIG=/opt/secrets/kubeconfig"]) {
                    echo 'Deploying Kubernetes manifests...'
                    sh '''
                        set -e
                        kubectl apply -f kubernetes/services/ --validate=false
                        kubectl apply -f kubernetes/deployments/ --validate=false
                    '''
                    echo 'Deployment completed.'
                }
            }
        }

    }

    post {
        always {
            archiveArtifacts artifacts: 'trivy-scan-*.html', allowEmptyArchive: true
        }
    }
}
