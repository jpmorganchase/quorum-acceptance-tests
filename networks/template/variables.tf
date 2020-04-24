//---------- standard inputs -----------

variable "network_name" {}
variable "consensus" {}
variable "output_dir" {
  default = "/tmp"
}

variable "remote_docker_config" {
  type        = object({ ssh_user = string, ssh_host = string, private_key_file = string, docker_host = string })
  default     = null
  description = "Configuration to connect to a VM which enables remote docker API"
}

variable "properties_outdir" {
  default     = ""
  description = "Output directory containing DockerWaitMain-network.properties"
}

variable "gauge_env_outdir" {
  default     = ""
  description = "Output directory containing user.properties for Gauge env"
}

//---------- advanced inputs -----------
variable "number_of_nodes" {
  default = 4
}

variable "exclude_initial_nodes" {
  default     = []
  description = "Exclude nodes (0-based index) from initial network setup"
}

variable "docker_registry" {
  type        = list(object({ name = string, username = string, password = string }))
  default     = []
  description = "List of docker registeries to pull images from"
}

variable "docker_images" {
  type        = list(string)
  default     = []
  description = "List of docker images for pulling"
}