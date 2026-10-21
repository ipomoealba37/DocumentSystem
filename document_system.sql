-- MySQL dump 10.13  Distrib 9.4.0, for Win64 (x86_64)
--
-- Host: localhost    Database: document_system
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `document`
--

DROP TABLE IF EXISTS `document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `document` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `type` varchar(50) NOT NULL,
  `author` varchar(50) DEFAULT NULL,
  `content` text,
  `file_size` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` int DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `deleted_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `document_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document`
--

LOCK TABLES `document` WRITE;
/*!40000 ALTER TABLE `document` DISABLE KEYS */;
INSERT INTO `document` VALUES (1,'Spring Boot入门','技术文档','张三','Spring Boot是...',1024,'2026-06-04 13:24:28','2026-06-04 13:24:28',1,0,NULL),(5,'MyBatis 教程','技术文档','赵六','MyBatis 是持久层框架...',2048,'2026-06-05 22:26:51','2026-06-05 22:26:51',1,0,NULL),(6,'MyBatis 教程','技术文档','赵六','MyBatis 是持久层框架...',2048,'2026-06-05 22:29:07','2026-06-05 22:29:07',1,0,NULL),(7,'MyBatis 教程','技术文档','赵六','MyBatis 是持久层框架...',2048,'2026-06-05 22:38:04','2026-06-06 00:15:22',1,0,NULL),(8,'MyBatis 教程','技术文档','赵六','MyBatis 是持久层框架...',2048,'2026-06-05 22:43:02','2026-06-06 00:10:59',1,1,'2026-06-06 00:10:59'),(9,'Spring Boot教程','技术文档','张三','学习Spring Boot',1024,'2026-06-05 23:48:09','2026-06-05 23:48:09',1,0,NULL),(10,'课程笔记（演示）','学习资料','ljn','演示内容',0,'2026-06-05 23:53:03','2026-06-05 23:53:03',1,0,NULL),(11,'课程笔记（演示）','学习资料','ljn','演示内容',0,'2026-06-05 23:53:47','2026-06-06 00:15:35',1,0,NULL),(14,'课程笔记（演示）','学习资料','ljn','演示内容',8,'2026-06-05 23:54:23','2026-06-05 23:59:01',1,1,'2026-06-05 23:59:01'),(16,'课程笔记（演示）','学习资料','ljn','演示内容',2,'2026-06-06 00:03:21','2026-06-06 00:03:21',1,0,NULL),(17,'课程笔记（演示）','技术文档','ljn','演示内容',1,'2026-06-06 00:04:20','2026-06-06 00:15:35',1,0,NULL),(18,'课程笔记（演示）','个人文档','ljn','演示内容',0,'2026-06-06 00:10:12','2026-06-06 00:10:45',1,1,'2026-06-06 00:10:45'),(19,'随笔（演示）','个人文档','judy','演示内容',0,'2026-06-06 15:46:28','2026-06-06 15:47:05',2,0,NULL),(20,'课程笔记（演示）','学习资料','judy','演示内容',0,'2026-06-06 15:46:52','2026-06-06 15:46:52',2,0,NULL);
/*!40000 ALTER TABLE `document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','123456','admin@test.com','2026-05-30 22:47:22'),(2,'judy','666666','','2026-06-06 12:03:24');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-06 15:50:08
