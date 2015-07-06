Large-scale Analysis of Chess Games
===================

5 millions of chess games (300+ million of chess positions) have been recorded from the very beginning of chess history to the last tournaments of Magnus Carlsen. 
It's time to analyse all of them! 

This repository contains different resources (e.g., Java code) to analyse chess games. ***Current status***:
 * We are writing a technical report on various statistics of the chessgame database (e.g., number of unique positions);
 * We are using [Igrida Cluster](http://igrida.gforge.inria.fr/) for large computations with Stockfish UCI Engine
 
**Do not hesitate to participate or contact us!**

#### Objectives

We hope to gather various interesting insights on the skills, ratings, or styles of (famous) chess players. 
In fact numerous applications can be and have been considered such as cheat detection, computation of an intrinsic, "universal" rating, or the determination of key moments chess players blunder. For instance we would like to answer a question like "Who are the best chess players in history?"

For doing so, you typically need to analyze millions of moves with chess engines; it requires lots of computations. 
Our goal is to propose an open infrastructure for **large-scale analysis of chess games**. 
Specifically, we aim to:
 * replicate state-of-the-art research results (e.g., on cheat detection or intrinsic ratings); 
 * provide open data and procedures for exploring new directions;
 * investigate software engineering/scalability issues when computing millions of moves; 
 * organize a community of potential contributors for fun and profit.
 
#### Static analysis 

We are essentially analysing headers information (related to players' ratings, dates, openings, etc.). 
We qualify the analysis as "static" (as opposed to "dynamic", see below) since we do not analyse moves with chess engines.
Until now we have:
 * parsed various PGN files and structure each game in a (relational) database
 * processed games with [Spark SQL](https://spark.apache.org/sql/) in order to generate CSV files together with [R](http://www.r-project.org/) scripts to compute statistics 

*We are writing a technical report on various statistics of the database (e.g., number of unique positions)*

Chess games are saved in PGN file, for example
```
[Event "FIDE Candidates 2014"]
[Site "Khanty-Mansiysk RUS"]
[Date "2014.03.25"]
[Round "10.1"]
[White "Karjakin,Sergey"]
[Black "Andreikin,D"]
[Result "1/2-1/2"]
[WhiteTitle "GM"]
[BlackTitle "GM"]
[WhiteElo "2766"]
[BlackElo "2709"]
[ECO "B46"]
[Opening "Sicilian"]
[Variation "Taimanov variation"]

1. e4 c5 2. Nf3 e6 3. d4 cxd4 4. Nxd4 Nc6 5. Nc3 a6 6. Nxc6 bxc6 7. Qd3 Qc7 8. Qg3 Qxg3 9. hxg3 d5 10. g4 Rb8 11. g5 f6 12. gxf6 Nxf6 13. e5 Nd7 14. f4 Nc5 15. Rh3 a5 16. b3 Ba6 17. Bxa6 Nxa6 18. Na4 Rb4 19. Bd2 Re4+ 20. Kf1 Bb4 21. c3 Ba3
22. Re1 Rxe1+ 23. Kxe1 O-O 24. Ke2 h6 25. Rg3 Kf7 26. Rh3 Kg6 27. Rg3+ Kf7 28. Rh3 Kg6 29. Rg3+ Kf7 1/2-1/2
```

As you can notice, each PGN file is separated in two parts: (1) headers (2) moves

The static analysis first parses each file and inspects each interesting headers information like *White Elo Rating*, *Result* or *Date*. We can also get an iterator on moves to get information about *Pieces Captured Count* or *Pieces Moves Count*...
We parse all games with a [Java parser](http://sourceforge.net/projects/pgnparse/) in order to analyze different moves (promotions, king castling, captured pieces...) and generate FENs.

#### Dynamic analysis 

We aim to analyse each move (ply) and position with chess engines such as Stockfish.

For instance, Stockfish can calculate a score and evaluation of each move:

| Move                 | Evaluation | Gain  |
|----------------------|------------|-------|
| 1 - f3               | 0.12       | -0.01 |
| 1 - f6               | 0.12       | 0.00  |
| 2 - e4               | 0.07       | -0.05 |

We will analyse all generated [FEN](https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation) on [Igrida Cluster](http://igrida.gforge.inria.fr/) with [Stockfish](https://stockfishchess.org/) UCI Engine (e.g., depth 20 with multi-pv 5). All logs are saved in files and afterwards in a (relational) database.

```
rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1
info depth 1 seldepth 1 multipv 1 score cp 3 nodes 64 nps 64000 tbhits 0 time 1 pv d7d5
info depth 1 seldepth 1 multipv 2 score cp -3 nodes 64 nps 64000 tbhits 0 time 1 pv d7d6
info depth 1 seldepth 1 multipv 3 score cp -6 nodes 64 nps 64000 tbhits 0 time 1 pv g8f6
info depth 2 seldepth 2 multipv 1 score cp -8 nodes 199 nps 199000 tbhits 0 time 1 pv d7d5 b2b3
info depth 2 seldepth 2 multipv 2 score cp -64 nodes 199 nps 199000 tbhits 0 time 1 pv d7d6 d2d3
info depth 2 seldepth 2 multipv 3 score cp -67 nodes 199 nps 199000 tbhits 0 time 1 pv e7e6 d2d3
```

#### Contact 

This project is part of a research internship at Inria/IRISA laboratory [DiverSE team](http://diverse.irisa.fr) on chess analysis:
 * François Esnault (MSc Student, University of Rennes 1) is the main developer and contributor of the project. 
 * Mathieu Acher (Associate Professor, University of Rennes 1) is supervising the project. 

