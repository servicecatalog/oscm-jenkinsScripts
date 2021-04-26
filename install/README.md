# OSCM Jenkins Pipline Installation

## Prerequisites

System minimum 
- 1 Core CPU 
- 4GB RAM
- 20GB Storage

Software
- Ansible
- Vagrant

Please note that this minimum configuration is not suitable for production use.

## Getting started

Change into the directory where the vagrantfile is located and start vagrant

``` 
vagrant up 
```

To stop the boxes use

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

When the vagrant boxes are initialized and running, you are able to run the playbook 

``` 
ansible-playbook -i inventory/inventory playbook/playbook.yml
```

After the playbook has ran successfully, you are able to connect to the Jenkins server.

Open your browser on http://192.168.32.113:8080. Login in to the Jenkins web interface with user name `Admin`, and same as password. You'll find all OSCM pipline jobs there.


*Have fun!*

