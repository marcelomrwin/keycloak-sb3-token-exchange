# keycloak-sb3-token-exchange

## Demonstration of the interaction between two spring boot 3 services and keycloak performing the token exchange.


oc login --token=<TOKEN> --server=<SERVER>
oc new-project demo-te
## to allow applications to read configuration like Kubernetes Secrets and ConfigMaps
oc create rolebinding default-view --clusterrole=view --serviceaccount=demo-te:default --namespace=demo-te