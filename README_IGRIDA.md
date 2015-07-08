How to run this program on Igrida Cluster ?
===================

This program use Igrida Cluster (INRIA Rennes) but you can definitely use your cluster (require OAR task manager).

This program analyse FEN list files with Stockfish UCI Engine and produces log file (for each depth, we record a lot of information).

## Steps to retrieve FEN ##

You can see an example of expected files in the resource folder (https://github.com/fresnault/chess-analysis/tree/master/resources).

 1. Get list of 100 000 FEN from database
> mysql --host=127.0.0.1 --user=root --password=password--port=XXXX -e”SELECT id FROM chess.FEN LIMIT 100000” > {file-name}.txt

 2. Split FEN file
> split -d -l 1000 {file-name}.txt

We have 100 files each with 1000 FEN. You need to put these files in the resource folder.

## Run the program on Igrida ##

 1. Connect to Igrida
> ssh $USER@igrida-oar-frontend

 2. Reserve n cores
> oarsub -I -l /core=n,walltime=03:00:00
> oarsub -I -l /core=100,walltime=03:00:00 to reserve 100 cores during 3 hours

 3. Run parallel processus with mpi
> mpirun --mca plm_rsh_agent “oarsh” -machinefile $OAR_NODEFILE bash run.sh

The run.sh bash program use a file lock to increment a counter. In fact, we run n java program with counter arguments (ie: "-i 30").

Congratulations, you are analyzing 100 000 FEN on 100 cores.
Your log files are recorded in the temporary hard drive (/temp_dd/igrida-fs1/$USER/SCRATCH/fen)
