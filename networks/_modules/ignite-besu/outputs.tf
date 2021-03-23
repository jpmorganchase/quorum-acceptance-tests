output "chainId" {
  value = local.chainId
}

output "network_id" {
  value = random_integer.network_id.result
}

output "generated_dir" {
  value = quorum_bootstrap_network.this.network_dir_abs
}

output "besu_dirs" {
  value = local.besu_dirs
}

output "tm_dirs" {
  value = [for id in local.node_indices : format("%s/%s%s", quorum_bootstrap_network.this.network_dir_abs, local.tm_dir_prefix, id)]
}

output "ethsigner_dirs" {
  value = [for id in local.node_indices : format("%s/%s%s", quorum_bootstrap_network.this.network_dir_abs, local.ethsigner_dir_prefix, id)]
}

output "node_keys_hex" {
  value = quorum_bootstrap_node_key.nodekeys-generator[*].node_key_hex
}

output "network_name" {
  value = local.network_name
}

output "application_yml_file" {
  value = local_file.configuration.filename
}

output "accounts_count" {
  value = { for id in local.node_indices : id => length(local.named_accounts_alloc[id]) }
}
