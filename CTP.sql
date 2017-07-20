-- phpMyAdmin SQL Dump
-- version 4.1.14.8
-- http://www.phpmyadmin.net
--
-- Client :  localhost
-- Généré le :  Ven 30 Juin 2017 à 08:33
-- Version du serveur :  5.1.73
-- Version de PHP :  5.3.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données :  `anousone`
--

-- --------------------------------------------------------

--
-- Structure de la table `CTP`
--

DROP TABLE IF EXISTS `CTP`;
CREATE TABLE IF NOT EXISTS `CTP` (
  `name` varchar(32) NOT NULL,
  `firstName` varchar(32) NOT NULL,
  `birthday` date NOT NULL,
  `codeCentre` varchar(5) NOT NULL,
  `nameAnon` varchar(32) NOT NULL,
  `idAnon` varchar(32) NOT NULL,
  `zipName` varchar(32) NOT NULL,
  `size` int(10) DEFAULT NULL,
  `studyInstanceUid` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`nameAnon`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Contenu de la table `CTP`
--

INSERT INTO `CTP` (`name`, `firstName`, `birthday`, `codeCentre`, `nameAnon`, `idAnon`, `zipName`, `size`, `studyInstanceUid`) VALUES
('doe', 'john', '2015-08-03', '12345', 'NewName', 'NewID', '', NULL, NULL);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
