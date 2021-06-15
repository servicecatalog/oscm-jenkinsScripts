# Local setup of OSCM Jenkins Piplines

## Prerequisites

* Install [Vagrant](https://www.vagrantup.com/docs/installation)
* Install [VirtualBox](https://www.virtualbox.org/)

## Getting started
As this setup is for local usage, Vagrant with VirtualBox (as a provider) is used, since it is free and available on every major platform.

To make it running, simply execute

```
vagrant up --no-parallel
```

This will initialize two vagrant boxes and run local ansible provisioner on them. When the job is executed successfully, you are able to connect to the Jenkins server.
Open your browser on http://192.168.32.113:8080. Login in to the Jenkins web interface with user name `admin`, and same as password. You'll find all OSCM pipline jobs there.
They are configured to work with single agent (192.168.32.115).

Additionally, to stop the boxes use

```
vagrant halt
```

To reboot the boxes use

```
vagrant reload
```

And to kill the boxes use

```
vagrant destroy
```

*Have fun!*
