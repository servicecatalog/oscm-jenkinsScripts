echo "Getting epel-release package available"

yum install epel-release -y

echo "Getting ansible installed"

yum install ansible -y

echo "Executing ansible playbook"

ansible-playbook /home/vagrant/playbook.yml
