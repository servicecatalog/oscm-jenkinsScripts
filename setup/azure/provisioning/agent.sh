apt-get install build-essential libssl-dev libffi-dev python-dev -y
apt-get update
apt-get install python3-pip -y
apt-get install python-jmespath

pip3 install --upgrade pip
pip3 install ansible[azure]==2.10.7

echo "Executing ansible agent playbook"

ansible-playbook /home/vagrant/agent.yml
