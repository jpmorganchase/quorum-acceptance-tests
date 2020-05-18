# Quorum Permissions model testing with dynamic network

  Tags: networks/template::raft-3plus1, pre-condition/no-record-blocknumber, permissions

## Permissions dynamic network testing
* Start a "permissioned" Quorum Network, named it "default", consisting of "Node1,Node2,Node3" with "full" `gcmode`
* Deploy "PermissionsUpgradable" smart contract from a default account in "Node1", name this contract as "upgradable"
* From "Node1" deploy "AccountManager" contract passing "upgradable" address, name it "accountmgr"
* From "Node1" deploy "OrgManager" contract passing "upgradable" address, name it "orgmgr"
* From "Node1" deploy "RoleManager" contract passing "upgradable" address, name it "rolemgr"
* From "Node1" deploy "NodeManager" contract passing "upgradable" address, name it "nodemgr"
* From "Node1" deploy "VoterManager" contract passing "upgradable" address, name it "votermgr"
* From "Node1" deploy "PermissionsInterface" contract passing "upgradable" address, name it "interface"
* From "Node1" deploy implementation contract passing addresses of "upgradable", "orgmgr", "rolemgr", "accountmgr", "votermgr", "nodemgr". Name this as "implementation"
* Create permissions-config.json object using the contract addersses of "upgradable", "interface", "implementation", "orgmgr", "rolemgr",  "accountmgr", "votermgr", "nodemgr". Name it "permissionsConfig"
* Update "permissionsConfig". Add "Node1"'s default account to accounts in config
* Update "permissionsConfig". Add "NWADMIN" as network admin org, "NWADMIN" network admin role, "ORGADMIN" as the org admin role
* Update "permissionsConfig". Set suborg depth as "4", suborg breadth as "4"
* Write "permissionsConfig" to the data directory of "Node1,Node2,Node3"
* From "Node1" execute permissions init on "upgradable" passing "interface" and "implementation" contract addresses
* Restart network "default"
* Validate that "NWADMIN" is approved, has "Node1" linked and has role "NWADMIN"
* Validate that "NWADMIN" is approved, has "Node2" linked and has role "NWADMIN"
* Validate that "NWADMIN" is approved, has "Node3" linked and has role "NWADMIN"
* Check "Node1"'s default account is from org "NWADMIN" and has role "NWADMIN" and is org admin and is active
* From "Node1" propose new org "ORG1" into the network with "Node4"'s enode id and "Default" account
* From "Node1" approve new org "ORG1" into the network with "Node4"'s enode id and "Default" account
* Start stand alone "Node4" in "networkId"
* Write "permissionsConfig" to the data directory of "Node4"
* Restart network "default"
* Validate that "ORG1" is approved, has "Node4" linked and has role "ORGADMIN"