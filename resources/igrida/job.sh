#!/bin/bash
#OAR -n chess_analyse
#OAR -l /core=1,walltime=64:00:00
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

