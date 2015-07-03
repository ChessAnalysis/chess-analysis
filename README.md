Chess Analyzing
===================

This repository contains a Java project created as part of research stage on chess analyzing.
You can find different resources to analyse chess games.

#### Statically analyse

 - Parse PGN File and insert each games in a database
 - Parse Games with Spark SQL in order to generate a CSV File

#### Dynamically analyse

 - Use Stockfish to evaluate games retrieved by Spark SQL

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

We use Spark SQL to collect data from our database. Then, we parse each game with a [Java parser](http://sourceforge.net/projects/pgnparse/) in order to analyze moves (promotions, king castling, captured pieces...). This file also generated a CSV file usable by R.



----------

Dynamically Analyze
-------------

As statically analyze, we collect data from our database with the help of Spark SQL. Then, we give a evaluation to each move thankful Stockfish UCI engine.