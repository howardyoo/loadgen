#!/bin/zsh
export KUBECONFIG="$(kind get kubeconfig-path --name="kind-wf")"
kubectl cluster-info

# setup wavefront-proxy
kubectl apply -f ./loadgen

echo -n "waiting for wavefront loadgen to be ready"
while [[ $(kubectl get pods --selector=app=loadgen --namespace=loadgen -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}' | grep -o "True" | wc -l) = 0 ]]; do echo -n "." && sleep 1; done
echo ""

echo "waiting for the service to be callable..."
sleep 5

# curl commands
curl "http://127.0.0.1:8001/api/v1/namespaces/loadgen/services/loadgen-svc/proxy/"
# running cpu load:
curl "http://127.0.0.1:8001/api/v1/namespaces/loadgen/services/loadgen-svc/proxy/cpu/run?threads=5&duration=30"
# running mem load:
curl "http://127.0.0.1:8001/api/v1/namespaces/loadgen/services/loadgen-svc/proxy/mem/run?threads=3&duration=20"


