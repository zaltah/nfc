#!/usr/bin/env bash

## NAT traffic going to the internet
route add default gw 172.18.18.1
iptables -t nat -A POSTROUTING -o enp0s8 -j MASQUERADE

######################Do I forward traffic from old address or add new one to the app's config?


## Save the iptables rules
iptables-save > /etc/iptables/rules.v4
ip6tables-save > /etc/iptables/rules.v6

##ADD PSK
cat <<EOF > /etc/ipsec.secret
172.16.16.16 172.30.30.30 : PSK "Qgmk9u9B5wAgFSMALnVH0xIjFc79cNzqOoH3GebzmMXsb3yNPw5pIx8OasBKGAa9"
172.18.18.18 172.30.30.30 : PSK "zEVzTiFtZDDOxbkn6aV4RTk1VUAFcDZal83G9KaALQfuRxhKzXPDotJEU6R3apsa"
EOF

##write ipsec.conf
cat <<EOF > /etc/ipsec.conf
config setup
    charondebug=all
    uniqueids=yes
    strictcrlpolicy=no

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

###################add timeouts?

#keyingtries=0
#ikelifetime=1h
#lifetime=8h
#dpddelay=30
#dpdtimeout=120
#dpdaction=restart
#auto=start

ipsec restart