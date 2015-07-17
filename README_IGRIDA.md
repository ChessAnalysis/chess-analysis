How to run this program on Igrida Cluster ?
===================

This program use Igrida Cluster (INRIA Rennes) but you can definitely use your cluster (require OAR task manager).

This program analyse FEN list files with Stockfish UCI Engine and produces log file (for each depth, we record a lot of information).

Your file tree need to look like this one :
> HOME DIRECTORY<br/>
> ├── run.jar<br/>
> ├── job.sh<br/>
> ├── param-file.txt<br/>
> └── uci-engine (directory)<br/>

> TEMP DISK DIRECTORY<br/>
> ├── input (directory)<br/>
> └── output (directory)<br/>


## Steps to retrieve FEN ##

You can see an example of expected files in the resource folder (https://github.com/fresnault/chess-analysis/tree/master/resources).

 1. Get list of N FEN from database
> mysql --host=127.0.0.1 --user=root --password=password --port=XXXX -e”SELECT id FROM chess.FEN LIMIT N” > {file-name}.txt

 2. Split FEN file (M lines)
> split -a 4 -d -l M {file-name}.txt<br/>
> split -a 4 -d -l 1000 {file-name}.txt

We have 100 files each with 1000 FEN. You need to put these files in the resource folder.

## Run the program on Igrida ##

 1. Connect to Igrida<br/>
> ssh $USER@igrida-oar-frontend
 2. Edit the param-file<br/>
You need to put parameters in param-file.txt file.
For example, if you want analyse 1000 and 1001 file, you can put
> -i 1000 -d 19 -t 1 -pv 1<br/>
> -i 1001 -d 19 -t 1 -pv 1
Or simply<br/>
> -i 1000<br/>
> -i 1001
 3. Edit configuration file job.sh (number of cores, walltime)<br/>
 4. Reserve cores<br/>
> oarsub -S ./job.sh<br/>
Congratulations, you are analyzing 100 000 FEN on 100 cores.
Your log files are recorded in the temporary hard drive (/temp_dd/igrida-fs1/$USER/SCRATCH/output)
