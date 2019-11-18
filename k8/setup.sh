#!/bin/zsh
kind create cluster --name kind-wf

export KUBECONFIG="$(kind get kubeconfig-path --name="kind-wf")"
kubectl cluster-info

# setup wavefront-proxy
kubectl apply -f ./proxy

# setup kube state
kubectl apply -f ./kube-state

# setup wavefront-collector
kubectl apply -f ./wf-collector

# wait until we have the pod ready
echo -n "waiting for wavefront proxy to be ready"
while [[ $(kubectl get pods --selector=app=wavefront-proxy -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo -n "." && sleep 1; done
echo ""

echo -n "waiting for wavefront collector to be ready"
while [[ $(kubectl get pods --selector=k8s-app=wavefront-collector --namespace=wavefront-collector -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo -n "." && sleep 1; done
echo ""

# start out the kubectl proxy
kubectl proxy

