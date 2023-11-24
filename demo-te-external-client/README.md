openssl x509 -outform der -in keycloak.cer -out keycloak.der
keytool -import -noprompt -alias keycloak -keystore keycloak.jks -file keycloak.der
keytool -list -keystore keycloak.jks
oc create secret generic demo-te-external-client "--from-file=keycloak.jks=$(pwd)/src/main/resources/keycloak.jks" "--from-file=keycloak.pem=$(pwd)/src/main/resources/keycloak.pem" "--from-literal=client-secret=179e4c7a386043d9693cd9084e681037" "--from-literal=client-id=db23aeb0"
mvn oc:build
mvn oc:resource oc:apply
mvn oc:undeploy

./mvnw clean package oc:resource oc:build oc:apply -DskipTests


keytool -storepass "changeit" -keypass "changeit" -importcert -noprompt -alias keycloak -cacerts -file /certificates/keycloak.cer
keytool -storepass "changeit" -keypass "changeit" -list -keystore $JAVA_HOME/lib/security/cacerts
keytool -storepass "changeit" -keypass "changeit" -list -cacerts