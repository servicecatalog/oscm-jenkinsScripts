echo "Getting epel-release package available"

yum install epel-release -y

echo "Getting ansible installed"

yum install ansible -y

echo "Executing ansible master playbook"

ansible-playbook /home/vagrant/master.yml
