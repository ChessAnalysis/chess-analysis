-- phpMyAdmin SQL Dump
-- version 4.4.9
-- http://www.phpmyadmin.net
--
-- Client :  localhost:3306
-- Généré le :  Mar 04 Août 2015 à 09:54
-- Version du serveur :  5.5.42
-- Version de PHP :  5.6.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  `chess`
--

-- --------------------------------------------------------

--
-- Structure de la table `Event`
--

CREATE TABLE `Event` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `FEN`
--

CREATE TABLE `FEN` (
  `id` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `score` int(11) DEFAULT NULL,
  `analyzed` tinyint(1) NOT NULL DEFAULT '0',
  `log` text
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `Game`
--

CREATE TABLE `Game` (
  `id` int(11) NOT NULL,
  `eventId` int(11) NOT NULL,
  `whiteId` int(11) NOT NULL,
  `blackId` int(11) NOT NULL,
  `ecoId` int(11) NOT NULL,
  `whiteElo` int(11) DEFAULT NULL,
  `blackElo` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `round` varchar(255) DEFAULT NULL,
  `result` varchar(1) DEFAULT NULL,
  `totalPlyCount` int(11) DEFAULT NULL,
  `movesSAN` text,
  `movesUCI` text
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `Move`
--

CREATE TABLE `Move` (
  `id` int(11) NOT NULL,
  `idGame` int(11) NOT NULL,
  `halfMove` int(11) NOT NULL,
  `move` varchar(10) NOT NULL,
  `idFEN` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `evaluation` int(11) NOT NULL,
  `eco` int(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `MoveECO`
--

CREATE TABLE `MoveECO` (
  `id` int(11) NOT NULL,
  `halfMove` int(11) NOT NULL,
  `move` varchar(10) NOT NULL,
  `idFEN` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `evaluation` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `Opening`
--

CREATE TABLE `Opening` (
  `id` int(11) NOT NULL,
  `eco` varchar(20) NOT NULL,
  `opening` varchar(255) NOT NULL,
  `variation` varchar(255) DEFAULT NULL,
  `moves` text,
  `nbMoves` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Structure de la table `Player`
--

CREATE TABLE `Player` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Index pour les tables exportées
--

--
-- Index pour la table `Event`
--
ALTER TABLE `Event`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `FEN`
--
ALTER TABLE `FEN`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `Game`
--
ALTER TABLE `Game`
  ADD PRIMARY KEY (`id`),
  ADD KEY `game_event_fk` (`eventId`),
  ADD KEY `game_player_white_fk` (`whiteId`),
  ADD KEY `game_player_black_fk` (`blackId`),
  ADD KEY `game_eco_fk` (`ecoId`);

--
-- Index pour la table `Move`
--
ALTER TABLE `Move`
  ADD PRIMARY KEY (`id`),
  ADD KEY `move_game_fk` (`idGame`),
  ADD KEY `move_fen_fk` (`idFEN`);

--
-- Index pour la table `MoveECO`
--
ALTER TABLE `MoveECO`
  ADD PRIMARY KEY (`id`),
  ADD KEY `move_fen_fk` (`idFEN`);

--
-- Index pour la table `Opening`
--
ALTER TABLE `Opening`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `Player`
--
ALTER TABLE `Player`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables exportées
--

--
-- AUTO_INCREMENT pour la table `Event`
--
ALTER TABLE `Event`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `Game`
--
ALTER TABLE `Game`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `Move`
--
ALTER TABLE `Move`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `MoveECO`
--
ALTER TABLE `MoveECO`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `Opening`
--
ALTER TABLE `Opening`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT pour la table `Player`
--
ALTER TABLE `Player`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- Contraintes pour les tables exportées
--

--
-- Contraintes pour la table `Game`
--
ALTER TABLE `Game`
  ADD CONSTRAINT `game_eco_fk` FOREIGN KEY (`ecoId`) REFERENCES `Opening` (`id`),
  ADD CONSTRAINT `game_event_fk` FOREIGN KEY (`eventId`) REFERENCES `Event` (`id`),
  ADD CONSTRAINT `game_player_black_fk` FOREIGN KEY (`blackId`) REFERENCES `Player` (`id`),
  ADD CONSTRAINT `game_player_white_fk` FOREIGN KEY (`whiteId`) REFERENCES `Player` (`id`);

--
-- Contraintes pour la table `Move`
--
ALTER TABLE `Move`
  ADD CONSTRAINT `move_fen_fk` FOREIGN KEY (`idFEN`) REFERENCES `FEN` (`id`),
  ADD CONSTRAINT `move_game_fk` FOREIGN KEY (`idGame`) REFERENCES `Game` (`id`);

--
-- Contraintes pour la table `MoveECO`
--
ALTER TABLE `MoveECO`
  ADD CONSTRAINT `moveEco_fen2_fk` FOREIGN KEY (`idFEN`) REFERENCES `FEN` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
