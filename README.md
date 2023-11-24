# keycloak-sb3-token-exchange

## Demonstration of the interaction between two spring boot 3 services and keycloak performing the token exchange.


oc login --token=sha256~ZGxIEPGcuaN4F34JeFOTAmu-FVQS6rqv8NCjhNw1i38 --server=https://api.ocp4.masales.cloud:6443
oc new-project demo-te
## to allow applications to read configuration like Kubernetes Secrets and ConfigMaps
oc create rolebinding default-view --clusterrole=view --serviceaccount=demo-te:default --namespace=demo-te