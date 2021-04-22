Prerequisites
- install ansible 
- install vagrant


start

Start vagrant and initialize the boxes
-
vagrant up
ansible-playbook -i inventory/virtualbox.hosts playbooks/virtualbox.yml