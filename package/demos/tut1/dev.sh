# Create symlinks so this demo runs in the normal jdo development tree
# without any editing of build.xml. Editing build.xml tends to produce
# edits that end up in a release and the demo does not work :)

ln -s lib ../../tools
mkdir -p build/classes
ln -s ../../../../build/compile/za build/classes/za
ln -s ../../../../build/compile/com build/classes/com
ln -s ../../../../build/compile/versant.tasks build/classes
ln -s ../../../../build/compile/jndi.properties build/classes

