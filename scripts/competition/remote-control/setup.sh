sudo bash -c "echo -e 'cd $HOME/scripts/remote-control/\n. addAutoComplete.sh \ncd - >/dev/null' >/etc/bash_completion.d/rcrsAutoComplete.sh && chmod +x /etc/bash_completion.d/rcrsAutoComplete.sh "



echo "export PATH=$PATH:~/scripts/remote-control/:~/scripts/evaluation/">>~/.bashrc

apt update
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do sudo apt-get remove $pkg; done
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update

sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

#wget https://download.java.net/java/GA/jdk24.0.1/24a58e0e276943138bf3e963e6291ac2/9/GPL/openjdk-24.0.1_linux-x64_bin.tar.gz
#sudo mkdir -p /usr/lib/jvm
#sudo tar -zxf openjdk-24.0.1_linux-x64_bin.tar.gz     -C /usr/lib/jvm
#sudo update-alternatives --install /usr/bin/java java   /usr/lib/jvm/jdk-24.0.1/bin/java 2000
#sudo update-alternatives --install /usr/bin/javac javac   /usr/lib/jvm/jdk-24.0.1/bin/javac 2000
apt install -y openjdk-17-jdk rng-tools-debian imagemagick p7zip-full python-is-python3 gnuplot zip

echo "HRNGDEVICE=/dev/urandom">/etc/default/rng-tools-debian

/etc/init.d/rng-tools-debian start