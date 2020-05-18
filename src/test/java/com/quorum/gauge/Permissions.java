package com.quorum.gauge;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.quorum.gauge.common.GethArgBuilder;
import com.quorum.gauge.common.NodeType;
import com.quorum.gauge.common.QuorumNetworkProperty;
import com.quorum.gauge.common.QuorumNode;
import com.quorum.gauge.core.AbstractSpecImplementation;
import com.quorum.gauge.services.InfrastructureService;
import com.quorum.gauge.services.RaftService;
import com.quorum.gauge.services.UtilService;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.DataStoreFactory;
import io.reactivex.Observable;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.methods.response.permissioning.*;
import org.web3j.tx.Contract;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Service
public class Permissions extends AbstractSpecImplementation {

    @Autowired
    UtilService utilService;

    @Autowired
    private InfrastructureService infraService;

    @Autowired
    private RaftService raftService;


    private static final Map<String, Integer> accountAccessMap = new HashMap<>();
    private static final Map<String, Integer> nodeStatusMap = new HashMap<>();
    private static final Map<String, Integer> orgStatusMap = new HashMap<>();
    private static final Map<String, Integer> acctStatusMap = new HashMap<>();
    private static final Map<String, String> configMap = new HashMap<>();

    static {
        configMap.put("NWADMIN-org", "NWADMIN");
        configMap.put("NWADMIN-role", "NWADMIN");
        configMap.put("OADMIN-role", "ORGDMIN");

        accountAccessMap.put("ReadOnly", 0);
        accountAccessMap.put("Transact", 1);
        accountAccessMap.put("ContractDeploy", 2);
        accountAccessMap.put("FullAccess", 3);

        nodeStatusMap.put("Pending", 1);
        nodeStatusMap.put("Approved", 2);
        nodeStatusMap.put("Deactivated", 3);
        nodeStatusMap.put("Blacklisted", 4);

        acctStatusMap.put("Pending", 1);
        acctStatusMap.put("Active", 2);
        acctStatusMap.put("InActive", 3);

        orgStatusMap.put("Proposed", 1);
        orgStatusMap.put("Approved", 2);
        orgStatusMap.put("PendingSuspension", 3);
        orgStatusMap.put("Suspended", 4);
        orgStatusMap.put("RevokeSuspension", 5);
    }

    private static final Logger logger = LoggerFactory.getLogger(SmartContractDualState.class);

    public Permissions() {
    }

    public class PermissionsConfig {
        private String upgradableAddress;
        private String interfaceAddress;
        private String implAddress;
        private String orgMgrAddress;
        private String nodeMgrAddress;
        private String accountMgrAddress;
        private String roleMgrAddress;
        private String voterMgrAddress;
        private List<String> accounts;
        private String nwAdminOrg;
        private String nwAdminRole;
        private String orgAdminRole;
        private int subOrgBreadth;
        private int subOrgDepth;

        public String getUpgradableAddress() {
            return upgradableAddress;
        }

        public void setUpgradableAddress(String upgradableAddress) {
            this.upgradableAddress = upgradableAddress;
        }

        public String getInterfaceAddress() {
            return interfaceAddress;
        }

        public void setInterfaceAddress(String interfaceAddress) {
            this.interfaceAddress = interfaceAddress;
        }

        public String getImplAddress() {
            return implAddress;
        }

        public void setImplAddress(String implAddress) {
            this.implAddress = implAddress;
        }

        public String getNodeMgrAddress() {
            return nodeMgrAddress;
        }

        public void setNodeMgrAddress(String nodeMgrAddress) {
            this.nodeMgrAddress = nodeMgrAddress;
        }

        public String getAccountMgrAddress() {
            return accountMgrAddress;
        }

        public void setAccountMgrAddress(String accountMgrAddress) {
            this.accountMgrAddress = accountMgrAddress;
        }

        public String getRoleMgrAddress() {
            return roleMgrAddress;
        }

        public void setRoleMgrAddress(String roleMgrAddress) {
            this.roleMgrAddress = roleMgrAddress;
        }

        public String getVoterMgrAddress() {
            return voterMgrAddress;
        }

        public void setVoterMgrAddress(String voterMgrAddress) {
            this.voterMgrAddress = voterMgrAddress;
        }

        public String getNwAdminOrg() {
            return nwAdminOrg;
        }

        public void setNwAdminOrg(String nwAdminOrg) {
            this.nwAdminOrg = nwAdminOrg;
        }

        public String getNwAdminRole() {
            return nwAdminRole;
        }

        public void setNwAdminRole(String nwAdminRole) {
            this.nwAdminRole = nwAdminRole;
        }

        public String getOrgAdminRole() {
            return orgAdminRole;
        }

        public void setOrgAdminRole(String orgAdminRole) {
            this.orgAdminRole = orgAdminRole;
        }

        public int getSubOrgBreadth() {
            return subOrgBreadth;
        }

        public void setSubOrgBreadth(int subOrgBreadth) {
            this.subOrgBreadth = subOrgBreadth;
        }

        public int getSubOrgDepth() {
            return subOrgDepth;
        }

        public void setSubOrgDepth(int subOrgDepth) {
            this.subOrgDepth = subOrgDepth;
        }

        public String getOrgMgrAddress() {
            return orgMgrAddress;
        }

        public void setOrgMgrAddress(String orgMgrAddress) {
            this.orgMgrAddress = orgMgrAddress;
        }

        public void setAccounts(List<String> accounts) {
            this.accounts = accounts;
        }

        @Override
        public String toString() {
            return "PermissionsConfig{" +
                "upgradableAddress='" + upgradableAddress + '\'' +
                ", interfaceAddress='" + interfaceAddress + '\'' +
                ", implAddress='" + implAddress + '\'' +
                ", orgMgrAddress='" + orgMgrAddress + '\'' +
                ", nodeMgrAddress='" + nodeMgrAddress + '\'' +
                ", accountMgrAddress='" + accountMgrAddress + '\'' +
                ", roleMgrAddress='" + roleMgrAddress + '\'' +
                ", voterMgrAddress='" + voterMgrAddress + '\'' +
                ", accounts=" + accounts +
                ", nwAdminOrg='" + nwAdminOrg + '\'' +
                ", nwAdminRole='" + nwAdminRole + '\'' +
                ", orgAdminRole='" + orgAdminRole + '\'' +
                ", subOrgBreadth=" + subOrgBreadth +
                ", subOrgDepth=" + subOrgDepth +
                '}';
        }

        public List<String> getAccounts() {
            return accounts;
        }
    }

    @Step("Deploy <contractName> smart contract from a default account in <node>, name this contract as <contractNameKey>")
    public void deployPermissionsContracts(String contractName, QuorumNode node, String contractNameKey) {
        Contract c = permissionContractService.createPermissionsGenericContracts(node, contractName, null).blockingFirst();
        logger.debug("{} contract address is:{}", contractName, c.getContractAddress());

        assertThat(c.getContractAddress()).isNotBlank();
        DataStoreFactory.getScenarioDataStore().put(contractNameKey, c);
    }

    @Step("From <node> deploy <contractName> contract passing <upgrContractKey> address, name it <contractNameKey>")
    public void deployPermissionsGenericContracts(QuorumNode node, String contractName, String upgrContractKey, String contractNameKey) {
        // get the upgradable contract address from store, pass it to deploy call
        String upgrContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), upgrContractKey, Contract.class).getContractAddress();
        logger.debug("upgradable contract address is:{}", upgrContractAddress);
        Contract c = permissionContractService.createPermissionsGenericContracts(node, contractName, upgrContractAddress).blockingFirst();
        logger.debug("{} contract address is:{}", contractName, c.getContractAddress());

        assertThat(c.getContractAddress()).isNotBlank();
        DataStoreFactory.getScenarioDataStore().put(contractNameKey, c);
    }

    @Step("From <node> deploy implementation contract passing addresses of <upgrContractKey>, <orgContractKey>, <roleContractKey>, <accountContractKey>, <voterContractKey>, <nodeContractKey>. Name this as <contractNameKey>")
    public void deployPermissionsImpementation(QuorumNode node, String upgrContractKey, String orgContractKey, String roleContractKey, String accountContractKey, String voterContractKey, String nodeContractKey, String contractNameKey) {
        // get the address of all deployed contracts
        String upgrContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), upgrContractKey, Contract.class).getContractAddress();
        String orgMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), orgContractKey, Contract.class).getContractAddress();
        String roleMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), roleContractKey, Contract.class).getContractAddress();
        String acctMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), accountContractKey, Contract.class).getContractAddress();
        String voterMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), voterContractKey, Contract.class).getContractAddress();
        String nodeMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), nodeContractKey, Contract.class).getContractAddress();

        Contract c = permissionContractService.createPermissionsImplementationContract(node, upgrContractAddress, orgMgrAddress, roleMgrAddress, acctMgrAddress, voterMgrAddress, nodeMgrAddress).blockingFirst();
        assertThat(c.getContractAddress()).isNotBlank();
        DataStoreFactory.getScenarioDataStore().put(contractNameKey, c);
    }

    @Step("Create permissions-config.json object using the contract addersses of <upgrContractKey>, <interfaceContractKey>, <implContractKey>, <orgContractKey>, <roleContractKey>,  <acctContractKey>, <voterContractKey>, <nodeContractKey>. Name it <objectName>")
    public void createPermissionsConfig(String upgrContractKey, String interfaceContractKey, String implContractKey, String orgContractKey, String roleContractKey, String acctContractKey, String voterContractKey, String nodeContractKey, String objectName) {
        String upgrContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), upgrContractKey, Contract.class).getContractAddress();
        String interfaceContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), interfaceContractKey, Contract.class).getContractAddress();
        String implContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), implContractKey, Contract.class).getContractAddress();
        String orgMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), orgContractKey, Contract.class).getContractAddress();
        String roleMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), roleContractKey, Contract.class).getContractAddress();
        String acctMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), acctContractKey, Contract.class).getContractAddress();
        String voterMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), voterContractKey, Contract.class).getContractAddress();
        String nodeMgrAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), nodeContractKey, Contract.class).getContractAddress();

        PermissionsConfig permissionsConfig = new PermissionsConfig();
        permissionsConfig.setUpgradableAddress(upgrContractAddress);
        permissionsConfig.setInterfaceAddress(interfaceContractAddress);
        permissionsConfig.setImplAddress(implContractAddress);
        permissionsConfig.setNodeMgrAddress(nodeMgrAddress);
        permissionsConfig.setAccountMgrAddress(acctMgrAddress);
        permissionsConfig.setRoleMgrAddress(roleMgrAddress);
        permissionsConfig.setVoterMgrAddress(voterMgrAddress);
        permissionsConfig.setOrgMgrAddress(orgMgrAddress);

        logger.debug("perm config object is {}", permissionsConfig.toString());
        DataStoreFactory.getScenarioDataStore().put(objectName, permissionsConfig);
    }

    @Step("Update <objectName>. Add <node>'s default account to accounts in config")
    public void addAccountToConfig(String objectName, QuorumNode node) {
        PermissionsConfig permConfig = mustHaveValue(DataStoreFactory.getScenarioDataStore(), objectName, PermissionsConfig.class);
        logger.debug("perm config object is {}", permConfig.toString());

        List<String> accounts = new ArrayList<>();
        accounts.add(accountService.getDefaultAccountAddress(node).blockingFirst());
        permConfig.setAccounts(accounts);

        logger.debug("perm config object is {}", permConfig.toString());
        DataStoreFactory.getScenarioDataStore().put(objectName, permConfig);
    }

    @Step("Update <objectName>. Add <nwAdminOrg> as network admin org, <nwAdminRole> network admin role, <orgAdminRole> as the org admin role")
    public void setNetworkDetails(String objectName, String nwAdminOrg, String nwAdminRole, String orgAdminRole) {
        PermissionsConfig permConfig = mustHaveValue(DataStoreFactory.getScenarioDataStore(), objectName, PermissionsConfig.class);
        permConfig.setNwAdminOrg(nwAdminOrg);
        permConfig.setNwAdminRole(nwAdminRole);
        permConfig.setOrgAdminRole(orgAdminRole);

        logger.debug("perm config object is {}", permConfig.toString());
        DataStoreFactory.getScenarioDataStore().put(objectName, permConfig);
    }

    @Step("Update <objectName>. Set suborg depth as <subOrgDepth>, suborg breadth as <subOrgBreadth>")
    public void setSubDepthBreadth(String objectName, int subOrgDepth, int subOrgBreadth) {
        PermissionsConfig permConfig = mustHaveValue(DataStoreFactory.getScenarioDataStore(), objectName, PermissionsConfig.class);
        permConfig.setSubOrgBreadth(subOrgBreadth);
        permConfig.setSubOrgDepth(subOrgDepth);

        logger.debug("perm cofnig object is {}", permConfig.toString());
        DataStoreFactory.getScenarioDataStore().put(objectName, permConfig);
    }

    @Step("Write <objectName> to the data directory of <nodes>")
    public void writePermissionConfig(String objectName, List<QuorumNetworkProperty.Node> nodes) {
        InfrastructureService.NetworkResources networkResources = mustHaveValue(DataStoreFactory.getScenarioDataStore(), "networkResources", InfrastructureService.NetworkResources.class);
        PermissionsConfig permConfig = mustHaveValue(DataStoreFactory.getScenarioDataStore(), objectName, PermissionsConfig.class);

        ObjectMapper mapper = new ObjectMapper();

        Observable.fromIterable(networkResources.allResourceIds())
            .filter(containerId -> infraService.isGeth(containerId).blockingFirst())
            .doOnNext(gethContainerId -> logger.debug("Writing permissions-config.json {}", StringUtils.substring(gethContainerId, 0, 12)))
            .flatMap(gethContainerId -> infraService.writeFile(gethContainerId,
                "/data/qdata/permission-config.json",
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(permConfig)))
            .blockingSubscribe();
    }

    @Step("From <node> execute permissions init on <upgrContractKey> passing <interfaceContractKey> and <implContractKey> contract addresses")
    public void executePermInit(QuorumNode node, String upgrContractKey, String interfaceContractKey, String implContractKey) {
        String upgrContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), upgrContractKey, Contract.class).getContractAddress();
        String interfaceContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), interfaceContractKey, Contract.class).getContractAddress();
        String implContractAddress = mustHaveValue(DataStoreFactory.getScenarioDataStore(), implContractKey, Contract.class).getContractAddress();

        TransactionReceipt tx = permissionContractService.executeNetworkInit(node, upgrContractAddress, interfaceContractAddress, implContractAddress).blockingFirst();

        assertThat(tx.getTransactionHash()).isNotBlank();
    }

    @Step("Get netowrk details from <node>")
    public void getNetworkDetails(QuorumNode node) {
        PermissionOrgList orgList = permissionService.getPermissionOrgList(node).blockingFirst();
        int c = 0;
        for (PermissionOrgInfo o : orgList.getPermissionOrgList()) {
            ++c;
            logger.debug("{} org -> {}", c, o);
        }

        DataStoreFactory.getScenarioDataStore().put("permOrgList", orgList.getPermissionOrgList());

        PermissionAccountList acctList = permissionService.getPermissionAccountList(node).blockingFirst();
        List<PermissionAccountInfo> pa = acctList.getPermissionAccountList();
        c = 0;
        for (PermissionAccountInfo o : pa) {
            ++c;
            logger.debug("{} acct -> {}", c, o);
        }

        DataStoreFactory.getScenarioDataStore().put("permAcctList", pa);


        PermissionNodeList nodeList = permissionService.getPermissionNodeList(node).blockingFirst();
        List<PermissionNodeInfo> pn = nodeList.getPermissionNodeList();
        c = 0;
        for (PermissionNodeInfo o : pn) {
            ++c;
            logger.debug("{} node -> {}", c, o);
        }
        DataStoreFactory.getScenarioDataStore().put("permNodeList", pn);

        PermissionRoleList roleList = permissionService.getPermissionRoleList(node).blockingFirst();
        List<PermissionRoleInfo> pr = roleList.getPermissionRoleList();
        c = 0;
        for (PermissionRoleInfo o : pr) {
            ++c;
            logger.debug("{} role -> {}", c, o);
        }
        DataStoreFactory.getScenarioDataStore().put("permRoleList", pr);
    }

    @Step("Check org <org> is <status> with no parent, level <level> and empty sub orgs")
    public void checkOrgExists3(String org, String status, int level) throws Exception {
        boolean isPresent = orgExists("TYPE3", org, "", status, level, "");
        assertThat(isPresent).isTrue();
    }

    private String getNetworkAdminOrg(String org) {
        String key = org + "-org";
        if (configMap.get(key) != null) {
            logger.debug("key " + key + " has value");
            return configMap.get(key);
        }
        return org;
    }

    private String getNetworkOrgAdminRole(String role) {
        String key = role + "-role";
        if (configMap.get(key) != null) {
            return configMap.get(key);
        }
        return role;
    }

    private boolean orgExists(String checkType, String org, String porg, String status, int level, String slist) {
        org = getNetworkAdminOrg(org);
        porg = getNetworkAdminOrg(porg);
        List<PermissionOrgInfo> permOrgList = (ArrayList<PermissionOrgInfo>) DataStoreFactory.getScenarioDataStore().get("permOrgList");
        assertThat(permOrgList.size()).isNotEqualTo(0);
        int c = 0;
        boolean isPresent = false;
        l1:
        for (PermissionOrgInfo i : permOrgList) {
            ++c;
            logger.debug("{} org id: {} parent id: {} level: {}", c, i.getFullOrgId(), i.getParentOrgId(), i.getLevel());
            switch (checkType) {
                case "TYPE1":
                    if (i.getFullOrgId().equals(org) && (i.getParentOrgId() == null || i.getParentOrgId().equals(""))
                        && i.getLevel() == level && (i.getSubOrgList().size() > 0 && i.getSubOrgList().containsAll(Arrays.asList(slist.split(",")))) &&
                        i.getStatus() == orgStatusMap.get(status)) {
                        isPresent = true;
                        break l1;
                    }
                    break;
                case "TYPE2":
                    if (i.getFullOrgId().equals(org) && i.getParentOrgId().equals(porg)
                        && i.getLevel() == level && (i.getSubOrgList() == null || i.getSubOrgList().size() == 0) &&
                        i.getStatus() == orgStatusMap.get(status)) {
                        isPresent = true;
                        break l1;
                    }
                    break;
                case "TYPE3":
                    if (i.getFullOrgId().equals(org) && (i.getParentOrgId() == null || i.getParentOrgId().equals(""))
                        && i.getLevel() == level && (i.getSubOrgList() == null || i.getSubOrgList().size() == 0) &&
                        i.getStatus() == orgStatusMap.get(status)) {
                        isPresent = true;
                        break l1;
                    }
                    break;
            }
        }
        return isPresent;
    }

    public String getEnodeId(QuorumNode node) {
        String enode = permissionService.NodeInfo(node);
        String enodeId = enode.substring(0, enode.indexOf("@")).substring("enode://".length());
        return enodeId;
    }

    @Step("Check org <org> has <node> with status <status>")
    public void checkNodeExists(String org, QuorumNode node, String status) throws Exception {
        org = getNetworkAdminOrg(org);
        String enodeId = getEnodeId(node);
        List<PermissionNodeInfo> permNodeList = (ArrayList<PermissionNodeInfo>) DataStoreFactory.getScenarioDataStore().get("permNodeList");
        assertThat(permNodeList.size()).isNotEqualTo(0);
        int c = 0;
        boolean isPresent = false;
        for (PermissionNodeInfo i : permNodeList) {
            ++c;
            logger.debug("{} node: {} status: {}", c, i.getUrl(), i.getStatus());
            if (i.getOrgId().equals(org) && i.getUrl().contains(enodeId) && i.getStatus() == nodeStatusMap.get(status)) {
                isPresent = true;
                break;
            }
        }
        assertThat(isPresent).isTrue();
    }

    private boolean roleExists(String checkType, String org, String role, String access) {
        org = getNetworkAdminOrg(org);
        role = getNetworkOrgAdminRole(role);
        List<PermissionRoleInfo> permRoleList = (ArrayList<PermissionRoleInfo>) DataStoreFactory.getScenarioDataStore().get("permRoleList");
        assertThat(permRoleList.size()).isNotEqualTo(0);
        int c = 0;
        boolean isPresent = false;
        l1:
        for (PermissionRoleInfo i : permRoleList) {
            ++c;
            switch (checkType) {
                case "TYPE1":
                    if (i.getOrgId().equals(org) && i.getRoleId().equals(role) && i.getAccess() == accountAccessMap.get(access) && i.getActive() && i.isVoter()) {
                        isPresent = true;
                        break l1;
                    }
                    break;
                case "TYPE2":
                    if (i.getOrgId().equals(org) && i.getRoleId().equals(role) && i.getAccess() == accountAccessMap.get(access) && i.getActive() && !i.isVoter()) {
                        isPresent = true;
                        break l1;
                    }
                    break;
                case "TYPE3":
                    if (i.getOrgId().equals(org) && i.getRoleId().equals(role) && i.getAccess() == accountAccessMap.get(access) && !i.getActive() && !i.isVoter()) {
                        isPresent = true;
                        break l1;
                    }
                    break;
            }

        }
        return isPresent;
    }

    @Step("Check org <org> has role <role> with access <access> and permission to vote and is active")
    public void checkRoleExists1(String org, String role, String access) throws Exception {
        boolean isPresent = roleExists("TYPE1", org, role, access);
        assertThat(isPresent).isTrue();
    }

    private boolean accountExists(String acct, String org, String role, boolean orgAdmin) {
        org = getNetworkAdminOrg(org);
        role = getNetworkOrgAdminRole(role);
        String defaultAcct = acct;
        List<PermissionAccountInfo> pacctList = (ArrayList<PermissionAccountInfo>) DataStoreFactory.getScenarioDataStore().get("permAcctList");
        assertThat(pacctList.size()).isNotEqualTo(0);
        int c = 0;
        boolean isPresent = false;
        for (PermissionAccountInfo i : pacctList) {
            ++c;
            logger.debug("{} address: {} role: {} org: {}", c, i.getAcctId(), i.getRoleId(), i.getOrgId());
            if (orgAdmin) {
                if (i.getAcctId().equalsIgnoreCase(defaultAcct) && i.getOrgId().equals(org) &&
                    i.getRoleId().equals(role) && i.isOrgAdmin() && i.getStatus() == acctStatusMap.get("Active")) {
                    isPresent = true;
                    break;
                }
            } else {
                if (i.getAcctId().equalsIgnoreCase(defaultAcct) && i.getOrgId().equals(org) &&
                    i.getRoleId().equals(role) && !i.isOrgAdmin() && i.getStatus() == acctStatusMap.get("Active")) {
                    isPresent = true;
                    break;
                }
            }
        }
        return isPresent;
    }

    @Step("Check <node>'s default account is from org <org> and has role <role> and is org admin and is active")
    public void checkAccountExists3(QuorumNode node, String org, String role) throws Exception {
        String acct = accountService.getDefaultAccountAddress(node).blockingFirst();
        boolean isPresent = accountExists(acct, org, role, true);
        assertThat(isPresent).isTrue();
    }

    @Step("From <proposingNode> propose new org <orgId> into the network with <node>'s enode id and <accountKey> account")
    public void proposeOrg(QuorumNode proposingNode, String orgId, QuorumNetworkProperty.Node node, String accountKey) {
        String acctId = node.getAccountAliases().get(accountKey);
        ExecStatusInfo execStatus = permissionService.addOrg(proposingNode, orgId, node.getEnodeUrl(), acctId).blockingFirst();
        assertThat(!execStatus.hasError());
    }

    @Step("From <proposingNode> approve new org <orgId> into the network with <node>'s enode id and <accountKey> account")
    public void approveOrg(QuorumNode proposingNode, String orgId, QuorumNetworkProperty.Node node, String accountKey) {
        String acctId = node.getAccountAliases().get(accountKey);
        ExecStatusInfo execStatus = permissionService.approveOrg(proposingNode, orgId, node.getEnodeUrl(), acctId).blockingFirst();
        assertThat(!execStatus.hasError());
    }

    @Step("Start stand alone <node> in <networkId>")
    public void startOneNodes(QuorumNetworkProperty.Node node, String networkId) {
        GethArgBuilder additionalGethArgs = GethArgBuilder.newBuilder();
        InfrastructureService.NetworkResources networkResources = mustHaveValue(DataStoreFactory.getScenarioDataStore(), "networkResources", InfrastructureService.NetworkResources.class);
        raftService.addPeer(networkResources.aNodeName(), node.getEnodeUrl(), NodeType.peer)
            .doOnNext(res -> {
                Response.Error err = Optional.ofNullable(res.getError()).orElse(new Response.Error());
                assertThat(err.getMessage()).as("raft.addPeer must succeed").isBlank();
            })
            .map(Response::getResult)
            .flatMap(raftId -> infraService.startNode(
                InfrastructureService.NodeAttributes.forNode(node.getName())
                    .withAdditionalGethArgs(additionalGethArgs.raftjoinexisting(raftId))
                    .withAdditionalGethArgs(additionalGethArgs.permissioned(true)),
                resourceId -> networkResources.add(node.getName(), resourceId)
            ))
            .blockingSubscribe();
    }

}