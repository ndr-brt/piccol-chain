# Piccol chain

Based on
* https://medium.com/crypto-currently/lets-build-the-tiniest-blockchain-e70965a248b
* https://github.com/lhartikk/naivechain  

## Prerequisites

You need:
* jdk 11
* maven
* docker
* docker compose

## Run it!

Execute  
`mvn clean package && docker-compose up`  

Than you can communicate with nodes on these url:  
* **guestlist**: http://localhost:10000/
* **node_one**: http://localhost:10001/
* **node_two**: http://localhost:10002/
* **node_three**: http://localhost:10003/

Put those urls in you browser to show node's data.

## Api

### Guestlist

* `POST /nodes` join the net, body contains the hostname
* `GET /nodes` retrieve the nodes' hostnames

### Node

* `POST /transactions` create a transaction, body contains the json representation
* `GET /mine` mine a block
* `GET /blocks` retrieve the blockchain
* `POST /addPeer` make the peer extabilish a websocket connection. body contains the hostname
