#!/usr/bin/python

"""
This example creates a multi-controller network from semi-scratch by
using the net.add*() API and manually starting the switches and controllers.

This is the "mid-level" API, which is an alternative to the "high-level"
Topo() API which supports parametrized topology classes.

Note that one could also create a custom switch class and pass it into
the Mininet() constructor.
"""

from mininet.net import Mininet
from mininet.node import Controller, OVSSwitch, RemoteController
from mininet.cli import CLI
from mininet.log import setLogLevel

def multiControllerNet():
    "Create a network from semi-scratch with multiple controllers."

    net = Mininet( controller=Controller, switch=OVSSwitch )

    print "*** Creating (reference) controllers"
    
    c1 = RemoteController( 'c1', port=6633 )
    c2 = RemoteController( 'c2', port=6644 )


    #c1 = net.addController('c1')
    #c2 = net.addController('c2')
    
    print "*** Creating switches"
    s1 = net.addSwitch( 's1' )
    s2 = net.addSwitch( 's2' )
    s3 = net.addSwitch( 's3' )
    s4 = net.addSwitch( 's4' )
    s5 = net.addSwitch( 's5' )
    s6 = net.addSwitch( 's6' )
    s7 = net.addSwitch( 's7' )
    s8 = net.addSwitch( 's8' )
    s9 = net.addSwitch( 's9' ) 
    print "*** Creating hosts"
    hosts3 = [ net.addHost( 'h%d' % n ) for n in 1, 2 ]
    hosts4 = [ net.addHost( 'h%d' % n ) for n in 3, 4 ]
    hosts5 = [ net.addHost( 'h%d' % n ) for n in 5, 6 ]
    hosts6 = [ net.addHost( 'h%d' % n ) for n in 7, 8 ]
    hosts8 = [ net.addHost( 'h%d' % n ) for n in 9, 10 ]
    hosts9 = [ net.addHost( 'h%d' % n ) for n in 11, 12 ]

    print "*** Creating links"
    for h in hosts3:
        net.addLink( s3, h )
    for h in hosts4:
        net.addLink( s4, h )
    for h in hosts5:
        net.addLink( s5, h )
    for h in hosts6:
        net.addLink( s6, h )
    for h in hosts8:
        net.addLink( s8, h )
    for h in hosts9:
        net.addLink( s9, h )
    net.addLink( s1, s3 )
    net.addLink( s1, s4 )
    net.addLink( s2, s5 )
    net.addLink( s2, s6 )
    net.addLink( s7, s9 )
    net.addLink( s6, s9 )

    print "*** Starting network"
    net.build()
    c1.start()
    c2.start()
    s1.start( [ c1 ] )
    s2.start( [ c1 ] )
    s3.start( [ c1 ] )  
    s4.start( [ c1 ] )
    s5.start( [ c1 ] )
    s6.start( [ c1 ] )
    s7.start( [ c2 ] )
    s8.start( [ c2 ] )
    s9.start( [ c2 ] )
    print "*** Testing network"
    # net.pingAll()

    print "*** Running CLI"
    CLI( net )

    print "*** Stopping network"
    #net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )  # for CLI output
    multiControllerNet()
