oc create secret generic demo-te-internal-client "--from-file=keycloak.pem=$(pwd)/src/main/resources/keycloak.pem" "--from-literal=client-secret=GClIHIqnDikaEXPl0Ns2ElQzncIHYhjP" "--from-literal=client-id=demo-te-internal-client"
mvn oc:build
mvn oc:resource oc:apply
mvn oc:undeploy

./mvnw clean package -U oc:resource oc:build oc:apply -DskipTests