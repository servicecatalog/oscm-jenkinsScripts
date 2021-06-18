apt-get install build-essential libssl-dev libffi-dev python-dev -y
apt-get update
apt-get install python3-pip -y
apt-get install python-jmespath

pip3 install --upgrade pip
pip3 install ansible[azure]==2.10.7


curl -O https://raw.githubusercontent.com/ansible-collections/azure/dev/requirements-azure.txt
pip3 install -r requirements-azure.txt
rm requirements-azure.txt

ansible-galaxy collection install azure.azcollection 
ansible-galaxy collection install community.general 


 echo "export AZURE_SUBSCRIPTION_ID=${AZURE_SUBSCRIPTION_ID}" >> /etc/environment
 echo "export AZURE_CLIENT_ID=${AZURE_CLIENT_ID}" >> /etc/environment
 echo "export AZURE_OBJECT_ID=${AZURE_OBJECT_ID}" >> /etc/environment
 echo "export AZURE_TENANT_ID=${AZURE_TENANT_ID}" >> /etc/environment
 echo "export AZURE_CLIENT_SECRET=${AZURE_CLIENT_SECRET}" >> /etc/environment


echo "Executing ansible playbook"

ansible-playbook /home/vagrant/master.yml
