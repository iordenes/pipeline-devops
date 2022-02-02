def call(stages){

    stage("Paso 1: Compliar"){
        sh "echo 'Compile Code!'"
        // Run Maven on a Unix agent.
        sh "mvn clean compile -e"
    }
    stage("Paso 2: Testear"){
            sh "echo 'Test Code!'"
            // Run Maven on a Unix agent.
            sh "mvn clean test -e"
    }
    stage("Paso 3: Build .Jar"){
            sh "echo 'Build .Jar!'"
            // Run Maven on a Unix agent.
            sh "mvn clean package -e"
    }
    stage("Paso 4: Análisis SonarQube"){
        withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar Service in another docker container!'"
            // Run Maven on a Unix agent to execute Sonar.
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=github-sonar'
        }
    }
    stage("Paso 5: Levantar Springboot APP"){
        sh 'mvn spring-boot:run &'
    }
    stage("Paso 6: Dormir(Esperar 10sg) "){
        sh 'sleep 10'
    }
    stage("Paso 7: Test Alive Service - Testing Application!"){
        sh 'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
    }

}
return this;