#OAR -n test_passif
#OAR -l /core=2,walltime=2:00:00
#OAR --array-param-file /udd/fesnault/param-file.txt
#OAR -O /temp_dd/igrida-fs1/fesnault/SCRATCH/igrida.%jobid%.output
#OAR -E /temp_dd/igrida-fs1/fesnault/SCRATCH/igrida.%jobid%.output


. /etc/profile.d/modules.sh

module load java/7

cd /udd/fesnault

echo "=============== RUN ==============="
echo "Running ..."
java -jar run.jar $*
echo "Done"
echo "==================================="

