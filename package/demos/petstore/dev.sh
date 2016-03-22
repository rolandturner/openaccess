# Create symlinks so this demo runs in the normal jdo development tree
# without any editing of build.xml. Editing build.xml tends to produce
# edits that end up in a release and the demo does not work :)

ln -s lib ../../tools
mkdir -p web/WEB-INF/classes/com/versant
ln -s ../../../../../../../../build/compile/com/versant/jdo web/WEB-INF/classes/com/versant
ln -s ../../../../../build/compile/versant.tasks web/WEB-INF/classes

