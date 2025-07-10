-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: roomies
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `collectives`
--

DROP TABLE IF EXISTS `collectives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collectives` (
  `collective_id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `address_line` varchar(200) DEFAULT NULL,
  `zip_code` varchar(20) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`collective_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collectives`
--

LOCK TABLES `collectives` WRITE;
/*!40000 ALTER TABLE `collectives` DISABLE KEYS */;
/*!40000 ALTER TABLE `collectives` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_items`
--

DROP TABLE IF EXISTS `shopping_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_items` (
  `item_id` int unsigned NOT NULL AUTO_INCREMENT,
  `collective_id` int unsigned NOT NULL,
  `added_by` int unsigned NOT NULL,
  `name` varchar(150) NOT NULL,
  `quantity` varchar(40) DEFAULT '1',
  `purchased` tinyint(1) NOT NULL DEFAULT '0',
  `purchased_by` int unsigned DEFAULT NULL,
  `added_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `purchased_at` datetime DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  KEY `fk_shop_collective` (`collective_id`),
  KEY `fk_shop_added_by` (`added_by`),
  KEY `fk_shop_purchased_by` (`purchased_by`),
  CONSTRAINT `fk_shop_added_by` FOREIGN KEY (`added_by`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_shop_collective` FOREIGN KEY (`collective_id`) REFERENCES `collectives` (`collective_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_shop_purchased_by` FOREIGN KEY (`purchased_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_items`
--

LOCK TABLES `shopping_items` WRITE;
/*!40000 ALTER TABLE `shopping_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `shopping_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int unsigned NOT NULL AUTO_INCREMENT,
  `email` varchar(200) NOT NULL,
  `display_name` varchar(100) NOT NULL,
  `password` char(60) NOT NULL,
  `collective_id` int unsigned DEFAULT NULL,
  `role` enum('MEMBER','ADMIN') DEFAULT 'MEMBER',
  `confirmed` tinyint(1) DEFAULT '0',
  `confirmation_token` varchar(100) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_users_collective` (`collective_id`),
  CONSTRAINT `fk_users_collective` FOREIGN KEY (`collective_id`) REFERENCES `collectives` (`collective_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-07 16:57:03
