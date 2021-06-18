echo "Getting epel-release package available"

yum install epel-release -y

echo "Getting ansible installed"

yum install ansible -y

echo "Executing ansible agent playbook"

ansible-playbook /home/vagrant/agent.yml
