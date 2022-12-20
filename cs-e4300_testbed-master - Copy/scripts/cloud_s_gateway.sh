#!/usr/bin/env bash

## Traffic going to the internet
route add default gw 172.30.30.1

## Currently no NAT
#iptables -t nat -A POSTROUTING -o enp0s8 -j MASQUERADE

###############Should I drop messages coming from other ports than just 8080?
###############Do i need to add the port to these commands?
###############Do i need to differentiate between udp and tpc?

iptables -t nat -A PREROUTING -s 172.16.16.16 -j DNAT --to-destination 10.1.0.2
iptables -t nat -A PREROUTING -s 172.18.18.18 -j DNAT --to-destination 10.1.0.3
iptables -t nat -A POSTROUTING -o enp0s8 -j MASQUERADE

##ADD PSKs
#A to S "Qgmk9u9B5wAgFSMALnVH0xIjFc79cNzqOoH3GebzmMXsb3yNPw5pIx8OasBKGAa9"
#B to S "zEVzTiFtZDDOxbkn6aV4RTk1VUAFcDZal83G9KaALQfuRxhKzXPDotJEU6R3apsa"

cat <<EOF > /etc/ipsec.secrets
172.16.16.16 172.30.30.30 : PSK "Qgmk9u9B5wAgFSMALnVH0xIjFc79cNzqOoH3GebzmMXsb3yNPw5pIx8OasBKGAa9"
172.18.18.18 172.30.30.30: PSK "zEVzTiFtZDDOxbkn6aV4RTk1VUAFcDZal83G9KaALQfuRxhKzXPDotJEU6R3apsa"
EOF


########################### do I need to change if 172.30.30.30 is on the left side?
##write ipsec.conf
cat <<EOF > /etc/ipsec.conf
config setup
    charondebug=all
    uniqueids=yes
    strictcrlpolicy=no

conn aToServer
    authby=secret
    keyexchange=ikev2
    type=tunnel
    left=172.16.16.16
    leftsubnet=172.16.16.16/32
    right=172.30.30.30
    rightsubnet=1172.30.30.30/32
    ike=aes256-sha2_256-modp2048! 
    esp=aes256-sha2_256! 
    dpaction=restart
    auto=start

conn bToServer
    authby=secret
    keyexchange=ikev2
    type=tunnel
    left=172.18.18.18
    leftsubnet=172.18.18.18/32
    right=172.30.30.30
    rightsubnet=172.30.30.30/32
    ike=aes256-sha2_256-modp2048! 
    esp=aes256-sha2_256! 
    dpaction=restart
    auto=start
EOF

## Save the iptables rules
iptables-save > /etc/iptables/rules.v4
ip6tables-save > /etc/iptables/rules.v6
