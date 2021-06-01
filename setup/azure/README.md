# Azure cloud setup of OSCM Jenkins Pipelines

## Prerequisites

* Install [Vagrant](https://www.vagrantup.com/docs/installation)
* Valid [Azure platform](http://portal.azure.com/) subscription with service principal (registered client application) created in Azure Active Directory.

## Getting started
As this setup is using [Microsoft Azure](https://azure.microsoft.com/) as Vagrant provider, which allows Vagrant to control and provision machines in Microsoft Azure, following steps need to be executed for make it working:

```
$ vagrant box add azure https://github.com/azure/vagrant-azure/raw/v2.0/dummy.box --provider azure
$ vagrant plugin install vagrant-azure
```

Additionally, following environment variables must be set as they are used for setting up the connection to Azure platform:

```
AZURE_TENANT_ID
AZURE_CLIENT_ID
AZURE_CLIENT_SECRET
AZURE_SUBSCRIPTION_ID
```

Finally, ssh private key need to be specified in Vagrantfile so that SSH connection to VM could be possible with it. It can be created by running:

```
ssh-keygen -m PEM -t rsa
```

Now to make Vargrant setup everything, simply execute

```
vagrant up
```

*Have fun!*
