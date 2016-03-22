# Setup environment to use the Ant supplied with Versant Open Access.
# Run the following command at a prompt to configure the environment
# for your shell:
# . ant-env.sh

if [ -z "$OPENACCESS_HOME" ]; then
    OPENACCESS_HOME=$PWD/..
fi
export ANT_HOME=${OPENACCESS_HOME}/ant/
export PATH=${ANT_HOME}bin/:$PATH

echo OPENACCESS_HOME=${OPENACCESS_HOME}
echo ANT_HOME=$ANT_HOME
