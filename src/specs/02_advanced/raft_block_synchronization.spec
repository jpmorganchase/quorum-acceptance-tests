# Block synchronization when using Raft consensus

  Tags: networks/template::raft-3plus1, pre-condition/no-record-blocknumber, gcmode, block-sync

  Geth 1.8.12 introduces `--gcmode=full/archive`. This controls trie pruning which is enabled by default on all `--syncmode`.
  Setting `--gcmode=archive` would retain all historical data.

  This specification is to describe the expection w.r.t block synchronization for Quorum Network to function based on the following permutations

      |id    |networkType      |consensus|gcmode | nodeType |
      |raft1 |permissioned     |raft     |full   | peer     |
      |raft2 |permissioned     |raft     |archive| peer     |
      |raft3 |non-permissioned |raft     |full   | peer     |
      |raft4 |non-permissioned |raft     |archive| peer     |
      |raft5 |permissioned     |raft     |full   | learner  |
      |raft6 |permissioned     |raft     |archive| learner  |
      |raft7 |non-permissioned |raft     |full   | learner  |
      |raft8 |non-permissioned |raft     |archive| learner  |

## Verify block synchronization

  Tags: post-condition/datadir-cleanup, post-condition/network-cleanup

  This scenario is run against each row of data in the table above

* Start a <networkType> Quorum Network, named it <id>, consisting of "Node1,Node2,Node3" with <gcmode> `gcmode` using <consensus> consensus
* Blocks are synced when adding new node "Node4" with <gcmode> `gcmode` to network <id> as <nodeType>
* Verify privacy between "Node1" and "Node4" excluding "Node3" when using a simple smart contract
* Record the current block number, named it as "blockHeightBeforeStart"
* Stop all nodes in the network <id>
* Start all nodes in the network <id>
* Verify block heights in all nodes are greater or equals to "blockHeightBeforeStart" in the network <id>
* Verify privacy between "Node1" and "Node4" excluding "Node3" when using a simple smart contract
