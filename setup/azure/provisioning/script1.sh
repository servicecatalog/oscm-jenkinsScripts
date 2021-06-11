echo "Getting epel-release package available"

apt-get install build-essential libssl-dev libffi-dev python-dev -y

apt-get update
apt-get install python3-pip -y

pip3 install --upgrade pip
pip3 install ansible[azure]==2.10.7

curl -O https://raw.githubusercontent.com/ansible-collections/azure/dev/requirements-azure.txt
pip3 install -r requirements-azure.txt
rm requirements-azure.txt

ansible-galaxy collection install azure.azcollection 
ansible-galaxy collection install community.general 

echo "Executing ansible playbook"

ansible-playbook /home/vagrant/playbook.yml
