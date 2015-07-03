# Makefile to run a battery of tests of the basic functionality of pgn-extract.
# Copyright (C) David J. Barnes, 2013-2015
#
# Each test is run and then, where relevant, a comparison is made between
# the output files generated in the current directory and the 'oracle'
# files that exist in $(OUTPUT).
#
# Beware of case-insensitive filenames when testing flags that
# differ only in letter case. In general, double letters have been
# used for output-files generated from using upper-case flag letters.

PGN_EXTRACT=../pgn-extract
# Use for memory checking if valgrind is installed.
#PGN_EXTRACT=valgrind --dsymutil=yes ../pgn-extract

# Location of the file of ECO classifications.
ECO_FILE=../eco.pgn

# Location of the input files
INPUT=infiles
# Location of the oracle output files
OUTPUT=outfiles

# Test everything.
all: no-flags-1 no-flags-2 test-7 test-append test-AA test-bl45 \
     test-bu45 test-bu30 test-checkfile test-nocomments test-duplicates \
     test-noduplicates test-e test-EE test-f test-FF test-h test-l \
     test-LL test-checkmate test-n test-NN test-output test-PP test-r \
     test-RR test-s test-SS test-t test-TT test-nounique test-v \
     test-VV test-linelength test-WW test-x test-ZZ test-z test-hash \
     test-evaluation test-fencomments test-markmatches test-nochecks \
     test-nomovenumbers test-noresults test-notags test-plylimit \
     test-stalemate test-long-line test-totalplycount test-addhashcode \
     test-selectonly

# BEWARE: This removes all PGN files in the current directory.
# The required test PGN files are assumed to be in $(INPUT).
clean:
	-rm *.pgn *og.txt

# No flags:
#     + No input file.
#     - Input file(s): N/A
#     - Example input:
#       f3 e5 g4 Qh4 0-1
#     - Resulting output should be that the game input on standard input is
#       formatted as PGN on standard output.
#     - Expected output: 
#       Contents of fools-mate.pgn on standard output.
no-flags-1:
	echo "test: No input file."
	$(PGN_EXTRACT) < $(INPUT)/fools-mate.txt

# No flags:
#     + Single input file
#     - Input file(s): fools-mate.txt
#     - Resulting output should be the input formatted as PGN on standard output.
#     - Expected output: Contents of fools-mate.pgn should appear on standard output.
no-flags-2:
	echo "test: No flags."
	$(PGN_EXTRACT) $(INPUT)/fools-mate.txt

# -7 / --seven
#     + Input file with games having tags additional to the seven tag roster.
#     - Input file(s): test-7.pgn
#     - Game output should have only the tags of the seven tag roster.
#     - Expected output: test-7-out.pgn
test-7:
	echo "test-7:"
	$(PGN_EXTRACT) -7 -otest-7-out.pgn $(INPUT)/test-7.pgn
	cmp test-7-out.pgn $(OUTPUT)/test-7-out.pgn

# -a / --append
#     + Input file containing games in any accepted format.
#     - Input file(s): test-a.txt
#     - Resulting output should contain two versions of the input game
#       formatted in PGN. The --output command is run first to create
#       a new file, then the -a version to append to that file.
#     - Expected output: test-a-out.pgn
test-append:
	echo "test-append:"
	$(PGN_EXTRACT) --output test-a-out.pgn $(INPUT)/test-a.txt
	$(PGN_EXTRACT) -atest-a-out.pgn $(INPUT)/test-a.txt
	cmp test-a-out.pgn $(OUTPUT)/test-a-out.pgn

	$(PGN_EXTRACT) --output test-a-out.pgn $(INPUT)/test-a.txt
	$(PGN_EXTRACT) --append test-a-out.pgn $(INPUT)/test-a.txt
	cmp test-a-out.pgn $(OUTPUT)/test-a-out.pgn

# -A
#     + Input file containing games and a file of arguments.
#     - Input file(s): fischer.pgn, petrosian.pgn, arglist.txt
#     - Resulting output should be files separating the unique and
#       duplicated games in the input files.
#     - Expected output: test-AA-unique.pgn, test-AA-dupes.pgn
test-AA:
	echo "test-A:"
	$(PGN_EXTRACT) -A$(INPUT)/argslist.txt
	cmp test-AA-dupes.pgn $(OUTPUT)/test-AA-dupes.pgn
	cmp test-AA-unique.pgn $(OUTPUT)/test-AA-unique.pgn

# -b
#     + Input file containing games of different length.
#     - Input file(s): fischer.pgn
#     - Resulting output: games whose number of moves is within
#       the specified bounds.
# 
test-b: test-bl45 test-bu45 test-bu30

#     - Expected output: 45 moves or more: test-bl45-out.pgn
test-bl45:
	echo "test-b:"
	$(PGN_EXTRACT) -bl45 -s -otest-bl45-out.pgn $(INPUT)/fischer.pgn
	cmp test-bl45-out.pgn $(OUTPUT)/test-bl45-out.pgn

#     - Expected output: 45 moves or less: test-bu45-out.pgn
test-bu45:
	echo "test-b:"
	$(PGN_EXTRACT) -bu45 -s -otest-bu45-out.pgn $(INPUT)/fischer.pgn
	cmp test-bu45-out.pgn $(OUTPUT)/test-bu45-out.pgn

#     - Expected output: exactly 30 moves: test-b30-out.pgn
test-bu30:
	echo "test-b:"
	$(PGN_EXTRACT) -b30 -s -otest-b30-out.pgn $(INPUT)/fischer.pgn
	cmp test-b30-out.pgn $(OUTPUT)/test-b30-out.pgn

# 
# -c / --checkfile
#     + Input files containing games.
#     - Input file(s): fischer.pgn, petrosian.pgn
#     - Resulting output should contain matched games that do not already occur in
#       the check file.
#     - Expected output: test-c-out.pgn
test-checkfile:
	echo "test-checkfile:"
	$(PGN_EXTRACT) -c$(INPUT)/petrosian.pgn -D -TpPetrosian -otest-c-out.pgn $(INPUT)/fischer.pgn
	cmp test-c-out.pgn $(OUTPUT)/test-c-out.pgn
	$(PGN_EXTRACT) -c$(INPUT)/clist.txt -D -TpPetrosian -otest-c-out.pgn $(INPUT)/fischer.pgn
	cmp test-c-out.pgn $(OUTPUT)/test-c-out.pgn

# -C / --nocomments
#     + Input file containing games with comments
#     - Input file(s): test-C.pgn
#     - Resulting output should have all comments removed.
#     - Expected output: test-CC-out.pgn
test-nocomments:
	echo "test-nocomments:"
	$(PGN_EXTRACT) -C -otest-CC-out.pgn $(INPUT)/test-C.pgn
	cmp test-CC-out.pgn $(OUTPUT)/test-CC-out.pgn

# -d / --duplicates
#     + Input file containing games with duplicates and non-duplicates.
#     - Input file(s): fischer.pgn, $(INPUT)/petrosian.pgn
#     - Resulting output should be files separating the unique and
#       duplicated games in the input files.
#     - Expected output: test-d-unique.pgn, test-d-dupes.pgn
test-duplicates:
	echo "test-duplicates:"
	$(PGN_EXTRACT) -dtest-d-dupes.pgn -s -otest-d-unique.pgn $(INPUT)/fischer.pgn $(INPUT)/petrosian.pgn
	cmp test-d-dupes.pgn $(OUTPUT)/test-d-dupes.pgn
	cmp test-d-unique.pgn $(OUTPUT)/test-d-unique.pgn

# -D / --noduplicates
#     + Input file containing games with duplicates and non-duplicates.
#     - Input file(s): fischer.pgn, $(INPUT)/petrosian.pgn
#     - Resulting output should be a file containing the combined input
#       with duplicate games removed.
#     - Expected output: test-DD-unique.pgn
test-noduplicates:
	echo "test-noduplicates:"
	$(PGN_EXTRACT) -D -s -otest-DD-unique.pgn $(INPUT)/fischer.pgn $(INPUT)/petrosian.pgn
	cmp test-DD-unique.pgn $(OUTPUT)/test-DD-unique.pgn
	$(PGN_EXTRACT) --noduplicates -s -otest-DD-unique.pgn $(INPUT)/fischer.pgn $(INPUT)/petrosian.pgn
	cmp test-DD-unique.pgn $(OUTPUT)/test-DD-unique.pgn

# -e
#     + Input file containing games without ECO classifications.
#     - Input file(s): test-e.pgn and eco.pgn in the test folder.
#     - Resulting output should have ECO classification added to the tags.
#     - Expected output: test-e-out.pgn
test-e:
	echo "test-e:"
	$(PGN_EXTRACT) -e$(ECO_FILE) -otest-e-out.pgn $(INPUT)/test-e.pgn
	cmp test-e-out.pgn $(OUTPUT)/test-e-out.pgn

# -E
#     + Input file containing games.
#     - Input file(s): test-E.pgn and eco.pgn if -e flag is used.
#     - Resulting output should be separate files for each ECO classification
#       at the level of the initial letter (A-E).
#     - Expected output: A.pgn, B.pgn, E.pgn
test-EE:
	echo "test-E:"
	$(PGN_EXTRACT) -e$(ECO_FILE) -E1 $(INPUT)/test-E.pgn
	cmp A.pgn $(OUTPUT)/A.pgn
	cmp B.pgn $(OUTPUT)/B.pgn
	cmp E.pgn $(OUTPUT)/E.pgn

# -f
#     + Input files containing games.
#     - Input file(s): test-f1.pgn, test-f2.pgn, files.txt
#     - Resulting output should be the combination of the input files.
#     - Expected output: test-f-out.pgn
test-f:
	echo "test-f:"
	$(PGN_EXTRACT) -f$(INPUT)/files.txt -otest-f-out.pgn
	cmp test-f-out.pgn $(OUTPUT)/test-f-out.pgn

# -F:
#     + Input file containing games without a trailing FEN comment.
#     - Input file(s): test-F.pgn
#     - Resulting output should have a comment at the end containing
#       the FEN description of the final position.
#     - Expected output: test-FF-out.pgn
test-FF:
	echo "test-F:"
	$(PGN_EXTRACT) -F -otest-FF-out.pgn $(INPUT)/test-F.pgn
	cmp test-FF-out.pgn $(OUTPUT)/test-FF-out.pgn

# 
# -h / -? / --help
#     + No input required.
#     - Input file(s): N/A
#     - Resulting output should be a description of how to use pgn-extract
#     - Expected output: N/A
# Also:
# 	../pgn-extract -?
# 	../pgn-extract --help
test-h:
	echo "test-h:"
	-$(PGN_EXTRACT) -h

# -l
#     + Input file containing games.
#     - Input file(s): fischer.pgn
#     - Resulting output: List of games parsed written to log.txt
#     - Expected output: log.txt
test-l:
	echo "test-l:"
	$(PGN_EXTRACT) -llog.txt -otest-l-out.pgn $(INPUT)/fischer.pgn
	cmp log.txt $(OUTPUT)/log.txt
	cmp test-l-out.pgn $(OUTPUT)/test-l-out.pgn

# -L
#     + Input file containing games.
#     - Input file(s): test-L1.pgn, test-L2.pgn
#     - Resulting output should be that log.txt contains the combined
#       logs from two runs of pgn-extract.
#     - Expected output: log.txt
test-LL:
	echo "test-L:"
	$(PGN_EXTRACT) -lLLog.txt -r $(INPUT)/test-L1.pgn
	$(PGN_EXTRACT) -LLLog.txt -r $(INPUT)/test-L2.pgn
	cmp LLog.txt $(OUTPUT)/LLog.txt

# -M / --checkmate
#     + Input file containing games.
#     - Input file(s): test-checkmate.pgn
#     - Resulting output should contain only those games that end in checkmate.
#     - Expected output: test-checkmate-out.pgn
test-checkmate:
	echo "test-checkmate:"
	$(PGN_EXTRACT) --checkmate -otest-checkmate-out.pgn $(INPUT)/test-checkmate.pgn
	cmp test-checkmate-out.pgn $(OUTPUT)/test-checkmate-out.pgn

# -n
#     + Input file containing games.
#     - Input file(s): petrosian.pgn
#     - Resulting output should be separate files containing matched
#       and non-matched games.
#     - Expected output: test-n-matched.pgn, test-n-unmatched.pgn
test-n:
	echo "test-n:"
	$(PGN_EXTRACT) -TpFischer -otest-n-matched.pgn -ntest-n-unmatched.pgn $(INPUT)/petrosian.pgn
	cmp test-n-matched.pgn $(OUTPUT)/test-n-matched.pgn
	cmp test-n-unmatched.pgn $(OUTPUT)/test-n-unmatched.pgn

# -N / --nonags
#     + Input file containing games with NAGs.
#     - Input file(s): test-N.pgn
#     - Resulting output should have all NAGs removed.
#     - Expected output: test-NN-out.pgn
test-NN:
	echo "test-N:"
	$(PGN_EXTRACT) -N -otest-NN-out.pgn $(INPUT)/test-N.pgn
	cmp test-NN-out.pgn $(OUTPUT)/test-NN-out.pgn

# 
# -o / --output
#     + Input file containing games in any accepted format.
#     - Input file(s): test-o.txt
#     - Resulting output should contain the input game formatted in PGN.
#     - Expected output: test-o-out.pgn
test-output:
	echo "test-output:"
	$(PGN_EXTRACT) -otest-o-out.pgn $(INPUT)/test-o.txt
	cmp test-o-out.pgn $(OUTPUT)/test-o-out.pgn
	$(PGN_EXTRACT) --output test-o-out.pgn $(INPUT)/test-o.txt
	cmp test-o-out.pgn $(OUTPUT)/test-o-out.pgn

# -P
#     + Input file containing games with different sequences for the same
#       opening.
#     - Input file(s): test-P.pgn, Pvars.txt
#     - Resulting output should be games whose opening move exactly match
#       the sequence specified in Pvars.txt
#     - Expected output: test-PP-out.pgn
test-PP:
	echo "test-P:"
	$(PGN_EXTRACT) -P -v$(INPUT)/Pvars.txt -otest-PP-out.pgn $(INPUT)/test-P.pgn
	cmp test-PP-out.pgn $(OUTPUT)/test-PP-out.pgn

# -r
#     + Input file containing games in any accepted format.
#     - Input file(s): test-r.text
#     - Resulting output should contain tag summary lines for the games
#       matched and a report of any errors.
#     - Expected output: test-r-log.txt
test-r:
	echo "test-r:"
	$(PGN_EXTRACT) -r -ltest-r-log.txt $(INPUT)/test-r.txt
	cmp test-r-log.txt $(OUTPUT)/test-r-log.txt

# -R
#     + Input file containing games.
#     - Input file(s): test-R.pgn, roster.txt
#     - Resulting output should contain games with their tag roster in
#       the order specified in roster.txt
#     - Expected output: test-R-out.pgn
test-RR:
	echo "test-R:"
	$(PGN_EXTRACT) -R$(INPUT)/roster.txt --output test-RR-out.pgn $(INPUT)/test-R.pgn
	cmp test-RR-out.pgn $(OUTPUT)/test-RR-out.pgn

# -s
#     + Input file containing games.
#     - Input file(s): test-s.pgn
#     - Resulting output should be silent, with games written to the output file.
#     - Expected output: test-s-out.pgn
test-s:
	echo "test-s:"
	$(PGN_EXTRACT) -s -o test-s-out.pgn $(INPUT)/test-s.pgn
	cmp test-s-out.pgn $(OUTPUT)/test-s-out.pgn

# -S
#     + Input file containing games whose players' names have slight
#       sound variations from anglesized versions.
#     - Input file(s): test-S.pgn
#     - Resulting output should be games that match by ignoring slight
#       soundex differences.
#     - Expected output: test-S-out.pgn
test-SS:
	echo "test-S:"
	$(PGN_EXTRACT) -S -TpPetrosian -otest-SS-out.pgn $(INPUT)/test-S.pgn
	cmp test-SS-out.pgn $(OUTPUT)/test-SS-out.pgn

# -t
#     + Input file containing games and a file of tag criteria.
#     - Input file(s): test-t.pgn, taglist.txt
#     - Resulting output should be only those games whose tags match
#       all of the criteria.
#     - Expected output: test-t-out.pgn
test-t:
	echo "test-t:"
	$(PGN_EXTRACT) -t$(INPUT)/taglist.txt -otest-t-out.pgn $(INPUT)/test-t.pgn
	cmp test-t-out.pgn $(OUTPUT)/test-t-out.pgn

# -T
#     + Input file containing games with tag information.
#     - Input file(s): fischer.pgn, test-Ta.pgn (and eco.pgn for -Te test.)
#     - Resulting output should contain only those games whose tag information
#       matches that specified.
#     - Expected output: test-Ta-out.pgn, test-Tb-out.pgn, test-Td-out.pgn,
#                        test-Tdd-out.pgn test-Te-out.pgn, test-Tp-out.pgn,
#                        test-Tw-out.pgn
test-TT:
	echo "test-T:"
	$(PGN_EXTRACT) -Td1970 -s -otest-TTd-out.pgn $(INPUT)/fischer.pgn
	cmp test-TTd-out.pgn $(OUTPUT)/test-TTd-out.pgn
	$(PGN_EXTRACT) -Td1960 -Td1962 -s -otest-TTdd-out.pgn $(INPUT)/fischer.pgn
	cmp test-TTdd-out.pgn $(OUTPUT)/test-TTdd-out.pgn
	$(PGN_EXTRACT) -TbPetrosian -s -otest-TTb-out.pgn $(INPUT)/fischer.pgn
	cmp test-TTb-out.pgn $(OUTPUT)/test-TTb-out.pgn
	$(PGN_EXTRACT) -e$(ECO_FILE) -TeB14 -s -otest-Te-out.pgn $(INPUT)/fischer.pgn
	$(PGN_EXTRACT) -TpPetrosian -s -otest-TTp-out.pgn $(INPUT)/fischer.pgn
	cmp test-TTp-out.pgn $(OUTPUT)/test-TTp-out.pgn
	$(PGN_EXTRACT) -Tr0-1 -s -otest-TTr-out.pgn $(INPUT)/fischer.pgn
	cmp test-TTr-out.pgn $(OUTPUT)/test-TTr-out.pgn
	$(PGN_EXTRACT) -TwFischer -s -otest-TTw-out.pgn $(INPUT)/fischer.pgn
	cmp test-TTw-out.pgn $(OUTPUT)/test-TTw-out.pgn
	$(PGN_EXTRACT) -TaBarnes -s -otest-TTa-out.pgn $(INPUT)/test-Ta.pgn
	cmp test-TTa-out.pgn $(OUTPUT)/test-TTa-out.pgn

# -U / --nounique
#     + Input file containing games with duplicates and non-duplicates.
#     - Input file(s): fischer.pgn, petrosian.pgn
#     - Resulting output should be a file containing just the duplicate games.
#     - Expected output: test-U-unique.pgn
test-nounique:
	echo "test-nounique:"
	$(PGN_EXTRACT) -U -s -otest-UU-unique.pgn $(INPUT)/fischer.pgn $(INPUT)/petrosian.pgn
	cmp test-UU-unique.pgn $(OUTPUT)/test-UU-unique.pgn
	$(PGN_EXTRACT) --nounique -s -otest-UU-unique.pgn $(INPUT)/fischer.pgn $(INPUT)/petrosian.pgn
	cmp test-UU-unique.pgn $(OUTPUT)/test-UU-unique.pgn

# -v
#     + Input file containing games.
#     - Input file(s): najdorf.pgn, vvars.txt
#     - Resulting output should be only those games whose opening moves
#       textually match (in any order) the moves in vars.txt.
#     - Expected output: test-v-out.pgn
test-v:
	echo "test-v:"
	$(PGN_EXTRACT) -v$(INPUT)/vvars.txt -otest-v-out.pgn $(INPUT)/najdorf.pgn
	cmp test-v-out.pgn $(OUTPUT)/test-v-out.pgn

# -V
#     + Input file containing games with variations
#     - Input file(s): test-V.pgn
#     - Resulting output should have all variations removed.
#     - Expected output: test-V-out.pgn
test-VV:
	echo "test-V:"
	$(PGN_EXTRACT) -V -otest-VV-out.pgn $(INPUT)/test-V.pgn
	cmp test-VV-out.pgn $(OUTPUT)/test-VV-out.pgn

# -w / --linelength
#     + Input file containing games.
#     - Input file(s): test-w.pgn
#     - Resulting output Games formatted up to the specified line length.
#       The default is 75.
#     - Expected output: test-w60-out.pgn, test-w75-out.pgn, test-w1000-out.pgn
test-linelength:
	echo "test-linelength:"
	$(PGN_EXTRACT) -w60 -otest-w60-out.pgn $(INPUT)/test-w.pgn
	cmp test-w60-out.pgn $(OUTPUT)/test-w60-out.pgn
	$(PGN_EXTRACT) -w75 -otest-w75-out.pgn $(INPUT)/test-w.pgn
	cmp test-w75-out.pgn $(OUTPUT)/test-w75-out.pgn
	$(PGN_EXTRACT) -w1000 -otest-w1000-out.pgn $(INPUT)/test-w.pgn
	cmp test-w1000-out.pgn $(OUTPUT)/test-w1000-out.pgn

	$(PGN_EXTRACT) --linelength 60 -otest-w60-out.pgn $(INPUT)/test-w.pgn
	cmp test-w60-out.pgn $(OUTPUT)/test-w60-out.pgn
	$(PGN_EXTRACT) --linelength 75 -otest-w75-out.pgn $(INPUT)/test-w.pgn
	cmp test-w75-out.pgn $(OUTPUT)/test-w75-out.pgn
	$(PGN_EXTRACT) --linelength 1000 -otest-w1000-out.pgn $(INPUT)/test-w.pgn
	cmp test-w1000-out.pgn $(OUTPUT)/test-w1000-out.pgn

# -W
#     + Input file containing games.
#     - Input file(s): test-W.pgn
#     - Resulting output should be games formatted in the specified notation:
#       halg (hyphenated long algebraic), lalg (non-hyphenated long algebraic),
#       elalg (enhanced long algebraic), xlalg (enhanced with capture info),
#       uci (UCI-compatible output),
#	and alternative piece letters.
#     - Expected output: test-WWhalg-out.pgn, test-WWlalg-out.pgn,
#                        test-WWelalg-out.pgn, test-WWdeutsch-out.pgn,
#                        test-WWuci-out.pgn
test-WW:
	echo "test-W:"
	$(PGN_EXTRACT) -Whalg -otest-WWhalg-out.pgn $(INPUT)/test-W.pgn
	cmp test-WWhalg-out.pgn $(OUTPUT)/test-WWhalg-out.pgn
	$(PGN_EXTRACT) -Wlalg -otest-WWlalg-out.pgn $(INPUT)/test-W.pgn
	cmp test-WWlalg-out.pgn $(OUTPUT)/test-WWlalg-out.pgn
	$(PGN_EXTRACT) -Welalg -otest-WWelalg-out.pgn $(INPUT)/test-W.pgn
	cmp test-WWelalg-out.pgn $(OUTPUT)/test-WWelalg-out.pgn
	$(PGN_EXTRACT) -Wxlalg -otest-WWxlalg-out.pgn $(INPUT)/test-W.pgn
	cmp test-WWxlalg-out.pgn $(OUTPUT)/test-WWxlalg-out.pgn
	$(PGN_EXTRACT) -WsanBSLTDK -otest-WWdeutsch-out.pgn $(INPUT)/test-W.pgn
	cmp test-WWdeutsch-out.pgn $(OUTPUT)/test-WWdeutsch-out.pgn
	$(PGN_EXTRACT) -Wuci -otest-WWuci-out.pgn $(INPUT)/test-W.pgn
	cmp test-WWuci-out.pgn $(OUTPUT)/test-WWuci-out.pgn

# -x
#     + Input file containing games.
#     - Input file(s): najdorf.pgn, xvars.txt
#     - Resulting output should be only those games which match
#       the result of reaching the opening sequence in vars.txt.
#     - Expected output: test-x-out.pgn
test-x:
	echo "test-x:"
	$(PGN_EXTRACT) -x$(INPUT)/xvars.txt -otest-x-out.pgn $(INPUT)/najdorf.pgn
	cmp test-x-out.pgn $(OUTPUT)/test-x-out.pgn

# -z
#     + Input file containing games.
#     - Input file(s): petrosian.pgn, zmatch.txt
#     - Resulting output should be games whose material balance matches that
#       specified in zmatch.txt
#     - Expected output: test-z-out.pgn
test-z:
	echo "test-z:"
	$(PGN_EXTRACT) -z$(INPUT)/zmatch.txt -otest-z-out.pgn $(INPUT)/petrosian.pgn
	cmp test-z-out.pgn $(OUTPUT)/test-z-out.pgn

# -Z
#     + Input file containing games with duplicates and non-duplicates.
#     - Input file(s): fischer.pgn, petrosian.pgn
#     - Resulting output should be files separating the unique and
#       duplicated games in the input files.
#     - Expected output: test-ZZ-unique.pgn, test-ZZ-dupes.pgn
test-ZZ:
	echo "test-Z:"
	$(PGN_EXTRACT) -Z -s -dtest-ZZ-dupes.pgn -otest-ZZ-unique.pgn $(INPUT)/fischer.pgn $(INPUT)/petrosian.pgn
	cmp test-ZZ-dupes.pgn $(OUTPUT)/test-ZZ-dupes.pgn
	cmp test-ZZ-unique.pgn $(OUTPUT)/test-ZZ-unique.pgn

# -#
#     + Input file containing games.
#     - Input file(s): test-hash.pgn
#     - Resulting output The input file split in to separate sub-files,
#       each containing 10 games, except the last which may contain fewer.
#     - Expected output: 1.pgn, 2.pgn
test-hash:
	echo "test-hash:"
	$(PGN_EXTRACT) -#20 -s $(INPUT)/test-hash.pgn
	cmp 1.pgn $(OUTPUT)/1.pgn
	cmp 2.pgn $(OUTPUT)/2.pgn

# --evaluation
#     + Input file containing games.
#     - Input file(s): test-evaluation.pgn
#     - Resulting output should include an evaluation value in a comment
#       after every move.
#     - Expected output: test-evaluation-out.pgn
test-evaluation:
	echo "test-evaluation:"
	$(PGN_EXTRACT) --evaluation -otest-evaluation-out.pgn $(INPUT)/test-evaluation.pgn
	cmp test-evaluation-out.pgn $(OUTPUT)/test-evaluation-out.pgn

# --fencomments
#     + Input file containing games.
#     - Input file(s): test-fencomments.pgn
#     - Resulting output should have a comment after every move containing a
#       FEN description of the position after that move.
#     - Expected output: test-fencomments-out.pgn
test-fencomments:
	echo "test-fencomments:"
	$(PGN_EXTRACT) --fencomments -otest-fencomments-out.pgn $(INPUT)/test-fencomments.pgn
	cmp test-fencomments-out.pgn $(OUTPUT)/test-fencomments-out.pgn

# --markmatches
#     + Input file containing games.
#     - Input file(s): najdorf.pgn, xvars.txt
#     - Resulting output should be only those games which match
#       the result of reaching the opening sequence in vars.txt.
#       The point of each match is marked with the comment { MATCH }
#     - Expected output: test-markmatches-out.pgn
test-markmatches:
	echo "test-markmatches:"
	$(PGN_EXTRACT) --markmatches MATCH -x$(INPUT)/xvars.txt -otest-markmatches-out.pgn $(INPUT)/najdorf.pgn
	cmp test-markmatches-out.pgn $(OUTPUT)/test-markmatches-out.pgn

# --nochecks
#     + Input file containing games with moves involving moves that give check
#       and/or mate.
#     - Input file(s): test-nochecks.pgn
#     - Resulting output should contain games with no check indicators after moves.
#     - Expected output: test-nochecks-out.pgn
test-nochecks:
	echo "test-nochecks:"
	$(PGN_EXTRACT) --nochecks -otest-nochecks-out.pgn $(INPUT)/test-nochecks.pgn
	cmp test-nochecks-out.pgn $(OUTPUT)/test-nochecks-out.pgn

# --nomovenumbers
#     + Input file containing games with move numbers.
#     - Input file(s): test-nomovenumbers.pgn
#     - Resulting output should contain games with no move numbers.
#     - Expected output: test-nomovenumbers-out.pgn
test-nomovenumbers:
	echo "test-nomovenumbers:"
	$(PGN_EXTRACT) -otest-nomovenumbers-out.pgn --nomovenumbers $(INPUT)/test-nomovenumbers.pgn
	cmp test-nomovenumbers-out.pgn $(OUTPUT)/test-nomovenumbers-out.pgn

# --noresults
#     + Input file containing games with results.
#     - Input file(s): test-noresults.pgn
#     - Resulting output should contain games with no results.
#     - Expected output: test-noresults-out.pgn
test-noresults:
	echo "test-noresults:"
	$(PGN_EXTRACT) -otest-noresults-out.pgn --noresults $(INPUT)/test-noresults.pgn
	cmp test-noresults-out.pgn $(OUTPUT)/test-noresults-out.pgn

# --notags
#     + Input file containing games with tag information.
#     - Input file(s): test-notags.pgn
#     - Resulting output should contain games with no tag information.
#     - Expected output: test-notags-out.pgn
test-notags:
	echo "test-notags:"
	$(PGN_EXTRACT) -otest-notags-out.pgn --notags $(INPUT)/test-notags.pgn
	cmp test-notags-out.pgn $(OUTPUT)/test-notags-out.pgn

# --plylimit
#     + Input file containing games.
#     - Input file(s): test-plylimit.pgn
#     - Resulting output should contain games whose number of moves (plies) are
#       limited at the specified ply limit.
#     - Expected output: test-plylimit-out.pgn
test-plylimit:
	echo "test-plylimit:"
	$(PGN_EXTRACT) --plylimit 10 -otest-plylimit-out.pgn $(INPUT)/test-plylimit.pgn
	cmp test-plylimit-out.pgn $(OUTPUT)/test-plylimit-out.pgn

# --selectonly
#     + Input file containing games.
#     - Input file(s): test-selectonly.pgn
#     - Resulting output should contain a single matched game.
#     - Expected output: test-selectonly-out.pgn
test-selectonly:
	echo "test-selectonly:"
	$(PGN_EXTRACT) -TpPetrosian --selectonly 2 -otest-selectonly-out.pgn $(INPUT)/test-selectonly.pgn
	cmp test-selectonly-out.pgn $(OUTPUT)/test-selectonly-out.pgn

# --stalemate
#     + Input file containing games.
#     - Input file(s): test-stalemate.pgn
#     - Resulting output should contain only those games that end in stalemate.
#     - Expected output: test-stalemate-out.pgn
test-stalemate:
	echo "test-stalemate:"
	$(PGN_EXTRACT) --stalemate -otest-stalemate-out.pgn $(INPUT)/test-stalemate.pgn
	cmp test-stalemate-out.pgn $(OUTPUT)/test-stalemate-out.pgn

# --totalplycount
#     + Input file containing games.
#     - Input file(s): test-totalplycount.pgn
#     - Resulting output should contain games with a TotalPlyCount tag.
#     - Expected output: test-totalplycount-out.pgn
test-totalplycount:
	echo "test-totalplycount:"
	$(PGN_EXTRACT) --totalplycount -otest-totalplycount-out.pgn $(INPUT)/test-totalplycount.pgn
	cmp test-totalplycount-out.pgn $(OUTPUT)/test-totalplycount-out.pgn

# --addhashcode
#     + Input file containing games.
#     - Input file(s): test-addhashcode.pgn
#     - Resulting output should contain games with a HashCode tag.
#     - Expected output: test-addhashcode-out.pgn
test-addhashcode:
	echo "test-addhashcode:"
	$(PGN_EXTRACT) --addhashcode -otest-addhashcode-out.pgn $(INPUT)/test-addhashcode.pgn
	cmp test-addhashcode-out.pgn $(OUTPUT)/test-addhashcode-out.pgn

# Test on a file with a string too long to be output within the
# defined line length.
#     + Input file containing a game with a very long comment.
#     + Input file(s): test-long-line.pgn
#     + Resulting output should contain the game properly formatted and
#       the log file should contain an error message reporting the problem.
#     + Expected output: test-long-line-out.pgn, test-long-line-log.txt
test-long-line:
	echo "test long line:"
	$(PGN_EXTRACT) -otest-long-line-out.pgn -ltest-long-line-log.txt $(INPUT)/test-long-line.pgn
	cmp test-long-line-out.pgn $(OUTPUT)/test-long-line-out.pgn
	cmp test-long-line-log.txt $(OUTPUT)/test-long-line-log.txt
