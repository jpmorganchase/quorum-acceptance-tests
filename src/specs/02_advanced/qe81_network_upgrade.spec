# Partial Network upgrade from an old version (q2.5.0/t0.10.5) to a privacy enhancements enabled version of `geth` and `tessera`

 Abbreviations:
 PE - privacy enhancements
 SP - StandatrdPrivate transaction/contract
 PP - PartyProtection transaction/contract
 PSV - PrivateStateValidation transaction/contract

 Upgrade means stoping a docker container that uses image A then rebuild and start it using image B (use the new binaries while keeping the original storage).
 The client (the acceptance tests framework) is PE enabled (is able to specify privacyFlag in transaction parameters).
 The non PE nodes are not aware of the privacyFlag and ignore it (for the old nodes all transactions are SP regardless of the privacyFlag).

 Test description:

 We start a 4 node network (with pre PE versions).
 We upgrade Node1 to a quorum version that supports PE and check that:
 - SP/PSV transactions are rejected by the node (as PE are disabled)
 - we try to enable PE (geth init) with a block height lower than the current block height - expecting error from geth init
 - we try to enable PE (geth init) with a block height higher than the current block height - expecting successs and a warning message that informs that the privacy manager must support PE
 - we try to start geth after enabling PE with the original version of tessera that does not support PE - expecting geth to exit and print error message
 We upgrade Node1 to have both quorum and tessera versions that support PE and check that:
 - quorum starts successfully
 - check that SP transactions work as expected
 - check that PP transactions/contracts work as expected on Node1 (attempted interractions from Node3/Node4 are rejected by Node1 - as incoming transactions are received as SP)
 We upgrade Node4 to have both quorum and tessera versions that support PE (but PE are not enabled yet) and check that:
 - quorum starts successfully but it does not connect to Node1 (Fork ID rejected - local incompatible or needs update)
 - when Node4 receives a PP transaction from Node1 it logs an error and stops (with BAD BLOCK). Error: Privacy enhanced transaction received while privacy enhancements are disabled. Please check your node configuration.
 We enable PE on Node4 from the correct block height (same as in Node1) and check that (involves deleting the node stored data and forcing it to resync):
 - we deploy a PP contract from Node1 to Node4
 - we prove that a non party node (Node3) can't affect the state of the deployed PP contract on Node1 and Node4
 - we prove that PE party nodes can update the state of the PP contract
 - run the above scenario with a SP contract (and heighlight that non party can alter the state of party nodes)

 Tags: privacy-enhancements-upgrade, pre-condition/no-record-blocknumber

        | q_from_version | q_to_version | t_from_version | t_to_version |
        | v2.5.0         | pe           | 0.10.5         | pe           |

* Start the network with:
    | node  | quorum           | tessera          |
    |-------|------------------|------------------|
    | Node1 | <q_from_version> | <t_from_version> |
    | Node2 | <q_from_version> | <t_from_version> |
    | Node3 | <q_from_version> | <t_from_version> |
    | Node4 | <q_from_version> | <t_from_version> |


* Record the current block number, named it as "recordedBlockNumber"

## Run selective node upgrade

 Tags: post-condition/datadir-cleanup, post-condition/network-cleanup

* Deploy a "StandardPrivate" simple smart contract with initial value "42" in "Node1"'s default account and it's private for "Node4", named this contract as "contract14"
* "contract14" is deployed "successfully" in "Node1,Node4"
 Upgrade quorum in Node1 to a version that supports privacy enhancements while privacy enhancements are disabled
* Stop and start "Node1" using quorum version <q_to_version> and tessera version <t_from_version>
 StandardPrivate transactions work as epected
* Deploy a "StandardPrivate" simple smart contract with initial value "42" in "Node1"'s default account and it's private for "Node4", named this contract as "contract15"
* "contract15" is deployed "successfully" in "Node1,Node4"
* Deploy a "StandardPrivate" simple smart contract with initial value "42" in "Node4"'s default account and it's private for "Node1", named this contract as "contract16"
* "contract16" is deployed "successfully" in "Node1,Node4"
 PartyProtection transactions are rejected in quorum Node1 as privacy enhancemetns are not enabled yet
* Deploying a "PartyProtection" simple smart contract with initial value "42" in "Node1"'s default account and it's private for "Node4" fails with message "PrivacyEnhancements are disabled"
 Run geth init with privacyEnhancementsBlock=currentBlockHeight-1
* Record the current block number, named it as "peBlockNumber"
* Stop "quorum" in "Node1"
* Run gethInit in "Node1" with genesis file having privacyEnhancementsBlock set to "peBlockNumber" + "-1"
 Geth init fails
* Grep "quorum" in "Node1" for "mismatching Privacy Enhancements fork block in database"
* Check that "quorum" in "Node1" is "down"
* Run gethInit in "Node1" with genesis file having privacyEnhancementsBlock set to "peBlockNumber" + "1"
 Geth init succeeds but displays the warning message that quorum won't start unless the privacy manager supports privacy enhancements
* Grep "quorum" in "Node1" for "Please ensure your privacy manager is upgraded and supports privacy enhancements"
 Tessera hasn't been updated so quorum should fail to start
* Grep "quorum" in "Node1" for "Cannot start quorum with privacy enhancements enabled while the transaction manager does not support it"
* Check that "quorum" in "Node1" is "down"
 Upgrade quorum and tessera in Node1 to the privacy enhancements enabled versions (only tessera needs to be updated but it is easier to use the step that does both)
* Stop and start "Node1" using quorum version <q_to_version> and tessera version <t_to_version>
* Deploy a "StandardPrivate" simple smart contract with initial value "42" in "Node1"'s default account and it's private for "Node4", named this contract as "contract16"
* "contract16" is deployed "successfully" in "Node1,Node4"
 A PartyProtection contract will not be deployed on Node1 as the tessera process on Node1 will detect that the tessera process on Node4 does not support privacy enhancements
* Deploying a "PartyProtection" simple smart contract with initial value "42" in "Node1"'s default account and it's private for "Node4" fails with message "Transactions with enhanced privacy is not currently supported on recipient"
* Stop and start "Node4" using quorum version <q_to_version> and tessera version <t_to_version>
 Node4 is now privacy enhancements capable but privacy enhancements are not enabled (geth init hasnt been run with a genesis file containing privacyEnhancementsBlock)
 Node1 is not able to connect to Node4 (as they have different values for PrivacyEnhancementsBlock)
* Grep "quorum" in "Node4" for "Fork ID rejected - local incompatible or needs update"
* Deploying a "PartyProtection" simple smart contract with initial value "42" in "Node4"'s default account and it's private for "Node1" fails with message "PrivacyEnhancements are disabled"
* Deploy a "StandardPrivate" simple smart contract with initial value "42" in "Node1"'s default account and it's private for "Node4", named this contract as "contract17"
* "contract17" is deployed "successfully" in "Node1,Node4"
* Deploy a "PartyProtection" simple smart contract with initial value "42" in "Node1"'s default account and it's private for "Node4", named this contract as "contract18"
 Node4 for should now stop appending new blocks (due to BAD BLOCK error)
* Grep "quorum" in "Node4" for "Privacy enhanced transaction received while privacy enhancements are disabled. Please check your node configuration."
* Stop "quorum" in "Node4" if consensus is istanbul
* Check that "quorum" in "Node4" is "down"

* Clear quorum data in "Node4"
 This is required so that Node4 can sync properly
* Change raft leader
* Run gethInit in "Node4" with genesis file having privacyEnhancementsBlock set to "peBlockNumber" + "1"
* Check that "quorum" in "Node4" is "up"

 Standard private transactions work as expected (in a mix of old/new nodes)
 Deploy a StandardPrivate simple contract from Node1 to Node2 and Node4
* Deploy a "StandardPrivate" contract `SimpleStorage` with initial value "42" in "Node1"'s default account and it's private for "Node2,Node4", named this contract as "contract19"
* "contract19" is deployed "successfully" in "Node1,Node2,Node4"
* "contract19"'s `get()` function execution in "Node1" returns "42"
* "contract19"'s `get()` function execution in "Node3" returns "0"
* "contract19"'s `get()` function execution in "Node2" returns "42"
* "contract19"'s `get()` function execution in "Node4" returns "42"
 Update the value in Node4 for Node1 and Node2
* Fire and forget execution of "StandardPrivate" simple contract("contract19")'s `set()` function with new value "7" in "Node4" and it's private for "Node1,Node2"
* Transaction Receipt is "successfully" available in "Node1,Node2,Node4" for "contract19"
* "contract19"'s `get()` function execution in "Node1" returns "7"
* "contract19"'s `get()` function execution in "Node2" returns "7"
* "contract19"'s `get()` function execution in "Node3" returns "0"
* "contract19"'s `get()` function execution in "Node4" returns "7"
 Update the value in Node2 for Node1 and Node4
* Fire and forget execution of "StandardPrivate" simple contract("contract19")'s `set()` function with new value "8" in "Node2" and it's private for "Node1,Node4"
* Transaction Receipt is "successfully" available in "Node1,Node2,Node4" for "contract19"
* "contract19"'s `get()` function execution in "Node1" returns "8"
* "contract19"'s `get()` function execution in "Node2" returns "8"
* "contract19"'s `get()` function execution in "Node3" returns "0"
* "contract19"'s `get()` function execution in "Node4" returns "8"
 A non party (Node3) can change the state of party nodes
* Fire and forget execution of "StandardPrivate" simple contract("contract19")'s `set()` function with new value "9" in "Node3" and it's private for "Node1,Node2,Node4"
* Transaction Receipt is "successfully" available in "Node1,Node2,Node4" for "contract19"
* "contract19"'s `get()` function execution in "Node1" returns "9"
* "contract19"'s `get()` function execution in "Node2" returns "9"
* "contract19"'s `get()` function execution in "Node3" returns "0"
* "contract19"'s `get()` function execution in "Node4" returns "9"

 Party protection transactions only work as expected between the upgraded nodes
* Deploy a "PartyProtection" contract `SimpleStorage` with initial value "42" in "Node1"'s default account and it's private for "Node4", named this contract as "contract20"
* "contract20" is deployed "successfully" in "Node1,Node4"
* "contract20"'s `get()` function execution in "Node1" returns "42"
* "contract20"'s `get()` function execution in "Node2" returns "0"
* "contract20"'s `get()` function execution in "Node3" returns "0"
* "contract20"'s `get()` function execution in "Node4" returns "42"
 Node3 which is not party to the contract can't update the state of the PE nodes which are party to the contract (Node1 and Node4).
 Even though we specify the privacy flag as PartyProtection because Node3 is not upgraded the transaction is sent as StandardPrivate.
* Fire and forget execution of "PartyProtection" simple contract("contract20")'s `set()` function with new value "5" in "Node3" and it's private for "Node1,Node4"
* Transaction Receipt is "unsuccessfully" available in "Node1,Node4" for "contract20"
* Transaction Receipt is "successfully" available in "Node2,Node3" for "contract20"
* "contract20"'s `get()` function execution in "Node1" returns "42"
* "contract20"'s `get()` function execution in "Node2" returns "0"
* "contract20"'s `get()` function execution in "Node3" returns "0"
* "contract20"'s `get()` function execution in "Node4" returns "42"
 PP transaction from Node4 updates all party nodes state
* Fire and forget execution of "PartyProtection" simple contract("contract20")'s `set()` function with new value "55" in "Node4" and it's private for "Node1"
* Transaction Receipt is "successfully" available in "Node1,Node4" for "contract19"
* "contract20"'s `get()` function execution in "Node1" returns "55"
* "contract20"'s `get()` function execution in "Node2" returns "0"
* "contract20"'s `get()` function execution in "Node3" returns "0"
* "contract20"'s `get()` function execution in "Node4" returns "55"

// TODO - Check with Nam if we could add some resend steps here (or leave it for a separate upgrade & resend scenario)