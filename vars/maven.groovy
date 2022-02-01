/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
def call(stages){
  env.TAREA = "Paso 1: Compliar"
  stage("$env.TAREA"){
    sh "mvn clean compile -e"
  }
  env.TAREA = "Paso 2: Testear"
  stage("$env.TAREA"){

    sh "mvn clean test -e"
  }
  env.TAREA = "Paso 3: Build .Jar"
  stage("$env.TAREA"){
    sh "mvn clean package -e"
  }
  env.TAREA = "Paso 4: Sonar - Análisis Estático"
  stage("$env.TAREA"){
      sh "echo 'Análisis Estático!'"
      withSonarQubeEnv('sonarqube') {
          sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
      }
  }
  env.TAREA = "Paso 5: Curl Springboot Gradle sleep 20"
  stage("$env.TAREA"){
      sh "gradle bootRun&"
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }
  env.TAREA = "Paso 6: Subir Nexus"
  stage("$env.TAREA"){
      nexusPublisher nexusInstanceId: 'nexus',
      nexusRepositoryId: 'devops-usach-nexus',
      packages: [
          [$class: 'MavenPackage',
              mavenAssetList: [
                  [classifier: '',
                  extension: '.jar',
                  filePath: 'build/DevOpsUsach2020-0.0.1.jar'
              ]
          ],
              mavenCoordinate: [
                  artifactId: 'DevOpsUsach2020',
                  groupId: 'com.devopsusach2020',
                  packaging: 'jar',
                  version: '0.0.1'
              ]
          ]
      ]
  }
  env.TAREA = "Paso 7: Descargar Nexus"
  stage("$env.TAREA"){
      sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
  }
  env.TAREA = "Paso 8: Levantar Artefacto Jar"
  stage("$env.TAREA"){
      sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
  }
  env.TAREA = "Paso 9: Testear Artefacto - Dormir(Esperar 20sg)"
  stage("$env.TAREA"){
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }
}
return this;
