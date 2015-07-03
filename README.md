Chess Analyzing
===================

This repository contains a Java project created as part of research internship at INRIA laboratory on chess analyzing.
You can find different resources to analyse chess games with Stockfish UCI Engine.

#### Statically analyse

 - Parse PGN File and insert each games in a database
 - Parse Games with Spark SQL in order to generate a CSV File

#### Dynamically analyse

 - Use Stockfish to evaluate games

----------

Statically Analyze
-------------

Chess games are save in PGN file , for example
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

As you have noticed, each PGN file is separated in 2 parts: (1) headers (2) moves

The statically analyze consists to parse each file and inspects each interesting headers information like *White Elo Rating*, *Result* or *Date*. We can also get an iterator on moves to get information about *Pieces Captured Count* or *Pieces Moves Count*...

We parse all games with a [Java parser](http://sourceforge.net/projects/pgnparse/) in order to analyze different moves (promotions, king castling, captured pieces...) and generate FEN.


----------

Dynamically Analyze
-------------

We can calculate intrinsic strength of players with recent UCI Engine. Indeed, we calculate a score and evaluation of each move.

| Move                 | Evaluation | Gain  |
|----------------------|------------|-------|
| 1 - f3               | 0.12       | -0.01 |
| 1 - f6               | 0.12       | 0.00  |
| 2 - e4               | 0.07       | -0.05 |

We will analyse all generated FEN on Igrida Cluster with Stockfish UCI Engine (depth 20 with multi-pv 5). All logs are saved in file.

```
rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1
info depth 1 seldepth 1 multipv 1 score cp 3 nodes 64 nps 64000 tbhits 0 time 1 pv d7d5
info depth 1 seldepth 1 multipv 2 score cp -3 nodes 64 nps 64000 tbhits 0 time 1 pv d7d6
info depth 1 seldepth 1 multipv 3 score cp -6 nodes 64 nps 64000 tbhits 0 time 1 pv g8f6
info depth 2 seldepth 2 multipv 1 score cp -8 nodes 199 nps 199000 tbhits 0 time 1 pv d7d5 b2b3
info depth 2 seldepth 2 multipv 2 score cp -64 nodes 199 nps 199000 tbhits 0 time 1 pv d7d6 d2d3
info depth 2 seldepth 2 multipv 3 score cp -67 nodes 199 nps 199000 tbhits 0 time 1 pv e7e6 d2d3
