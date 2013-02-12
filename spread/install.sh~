#!/bin/bash
# Script to install Spread

abort ()
{
	echo "$0: Aborting..."
	exit 1
}

#
# configure, compile and install spread in ~/spread
#
echo -n "Installing Spread in $HOME/spread... "
./configure --prefix=$HOME/spread > /dev/null
if [ ! $? -eq 0 ]; then
        echo "FAILED! (configure)"
        abort
fi
make &> /dev/null
if [ ! $? -eq 0 ]; then
	echo "FAILED! (make)"
	abort
fi
make install &> /dev/null
if [ ! $? -eq 0 ]; then
        echo "FAILED! (make install)"
        abort
fi
echo "ok"

#
# Compile Java API for Spread
#
echo -n "Build the Java API for Spread... "
cd java
ant jar > /dev/null
if [ ! $? -eq 0 ]; then
        echo "FAILED!"
        abort
fi
cd ..
echo "ok"

#
# Add ~/spread/sbin to the $PATH if needed
#
echo $PATH | grep -q $HOME/spread/sbin
if [ $? -eq 1 ]; then
	echo "export PATH=\${PATH}:\${HOME}/spread/sbin" >> $HOME/.bashrc
fi

#
# Add ~/spread/bin to the $PATH if needed
#
echo $PATH | grep -q $HOME/spread/bin
if [ $? -eq 1 ]; then
        echo "export PATH=\${PATH}:\${HOME}/spread/bin" >> $HOME/.bashrc
fi

#
# Create SPREAD_HOME environment variable
#
mv $HOME/.bashrc $HOME/.bashrc.tmp 
cat $HOME/.bashrc.tmp | grep -v SPREAD_HOME > $HOME/.bashrc
rm -f $HOME/.bashrc.tmp
echo "export SPREAD_HOME=\${HOME}/spread" >> $HOME/.bashrc

# The spread.conf file is used many times below
spread_conf=${HOME}/spread/etc/spread.conf

#
# Install Java API in $HOME/spread/lib
#
rm -f $HOME/spread/lib/*.jar
cp `find java/dest -name "*.jar"` $HOME/spread/lib/

#
# Install spread.conf in $HOME/spread/etc
#
hostname | grep -q uis.no
if [ $? -eq 0 ]; then
	cp -f spread-pitter.conf ${spread_conf}
else
	cp -f spread-sahara.conf ${spread_conf}
fi

#
# Read the group number and compute the port number to use
#
base_port=4800
group_num=0
echo -n "Enter your group number (E.g., if your group NO. is "02", please input "02"): "
read group_num
port=$((base_port + group_num*2))
echo "Your port number is: $port"
echo "Setting the port in your ${spread_conf}"
cat ${spread_conf} | sed 's/48??/'"$port"'/g' > spread.tmp && mv -f spread.tmp ${spread_conf}
echo "Setting the port in your ../sourcecode/build.xml file"
cat ../sourcecode/build.xml | sed 's/4803/'"$port"'/g' > build.tmp && mv -f build.tmp ../sourcecode/build.xml

#
# Print out successful installation message
#
echo ""
echo "Spread is installed successfully in $HOME/spread"
echo "Run 'source $HOME/.bashrc' to update environment variables in the current shell."

exit 0
