-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: javaproject
-- ------------------------------------------------------
-- Server version	9.0.1

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
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admins` (
  `admin_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admins`
--

LOCK TABLES `admins` WRITE;
/*!40000 ALTER TABLE `admins` DISABLE KEYS */;
INSERT INTO `admins` VALUES (1,'admin123','123456');
/*!40000 ALTER TABLE `admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books` (
  `book_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `genre` varchar(100) DEFAULT NULL,
  `isbn` varchar(20) DEFAULT NULL,
  `copies_available` int DEFAULT NULL,
  `rating` float DEFAULT NULL,
  PRIMARY KEY (`book_id`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
INSERT INTO `books` VALUES (101,'The Alchemist','Paulo Coelho','Fiction','9780061122415',5),(102,'To Kill a Mockingbird','Harper Lee','Classic','9780060935467',3),(103,'1984','George Orwell','Dystopian','9780451524935',0),(104,'The Great Gatsby','F. Scott Fitzgerald','Classic','9780743273565',2),(105,'A Brief History of Time','Stephen Hawking','Science','9780553380163',4),(106,'The Catcher in the Rye','J.D. Salinger','Fiction','9780316769488',1),(107,'The Hobbit','J.R.R. Tolkien','Fantasy','9780547928227',6),(108,'Thinking, Fast and Slow','Daniel Kahneman','Psychology','9780374533557',3),(109,'Clean Code','Robert C. Martin','Programming','9780132350884',2),(110,'The Pragmatic Programmer','Andrew Hunt','Programming','9780201616224',4),(111,'Sapiens: A Brief History of Humankind','Yuval Noah Harari','History','9780062316097',3),(112,'The Power of Habit','Charles Duhigg','Self-help','9780812981605',5),(113,'Rich Dad Poor Dad','Robert T. Kiyosaki','Finance','9781612680194',2),(114,'Atomic Habits','James Clear','Self-help','9780735211292',4),(115,'The Subtle Art of Not Giving a F*ck','Mark Manson','Self-help','9780062457714',3),(116,'Deep Work','Cal Newport','Productivity','9781455586691',4),(117,'Ikigai','HÃ©ctor GarcÃ­a & Francesc Miralles','Self-help','9780143130727',2),(118,'Man\'s Search for Meaning','Viktor E. Frankl','Psychology','9780807014271',3),(119,'Start with Why','Simon Sinek','Motivation','9781591846444',5),(120,'Zero to One','Peter Thiel','Entrepreneurship','9780804139296',2),(121,'Pride and Prejudice','Jane Austen','Classic','9780141439518',4),(122,'Lord of the Rings','J.R.R. Tolkien','Fantasy','9780544003415',3),(123,'The Da Vinci Code','Dan Brown','Thriller','9780307474278',2),(124,'The Shining','Stephen King','Horror','9780307743657',1),(125,'Dune','Frank Herbert','Science Fiction','9780441172719',5),(126,'The Name of the Wind','Patrick Rothfuss','Fantasy','9780756404079',3),(127,'Ender\'s Game','Orson Scott Card','Science Fiction','9780812550702',2),(128,'The Road','Cormac McCarthy','Dystopian','9780307387899',1),(129,'The Girl with the Dragon Tattoo','Stieg Larsson','Mystery','9780307454546',4),(130,'The Fault in Our Stars','John Green','Young Adult','9780142424179',3),(131,'Harry Potter and the Sorcererâ€™s Stone','J.K. Rowling','Fantasy','9780590353427',6),(132,'The Hunger Games','Suzanne Collins','Dystopian','9780439023528',4),(133,'The Kite Runner','Khaled Hosseini','Fiction','9781594480003',2),(134,'The Book Thief','Markus Zusak','Historical Fiction','9780375842207',3),(135,'The Handmaidâ€™s Tale','Margaret Atwood','Dystopian','9780385490818',2),(136,'The Giver','Lois Lowry','Young Adult','9780544336261',4),(138,'Charlotteâ€™s Web','E.B. White','Childrenâ€™s','9780064400558',3),(139,'The Outsiders','S.E. Hinton','Young Adult','9780142407332',2),(140,'The Diary of a Young Girl','Anne Frank','Biography','9780553577129',4),(141,'The Bell Jar','Sylvia Plath','Fiction','9780060837020',1),(142,'Animal Farm','George Orwell','Dystopian','9780451526342',3),(143,'Brave New World','Aldous Huxley','Dystopian','9780060850524',2),(144,'Fahrenheit 451','Ray Bradbury','Dystopian','9781451673319',4),(145,'The Little Prince','Antoine de Saint-ExupÃ©ry','Childrenâ€™s','9780156012195',5),(146,'The Chronicles of Narnia','C.S. Lewis','Fantasy','9780066238500',3),(147,'The Perks of Being a Wallflower','Stephen Chbosky','Young Adult','9781451696196',2),(148,'The Secret Garden','Frances Hodgson Burnett','Childrenâ€™s','9780142437018',4),(149,'The Old Man and the Sea','Ernest Hemingway','Classic','9780684801223',1),(150,'The Grapes of Wrath','John Steinbeck','Classic','9780143039433',3),(151,'The White Tiger','Aravind Adiga','Fiction','9781416562603',4),(152,'The God of Small Things','Arundhati Roy','Fiction','9780812979657',3),(153,'A Suitable Boy','Vikram Seth','Fiction','9780060786526',2),(155,'The Palace of Illusions','Chitra Banerjee Divakaruni','Historical Fiction','9780330458535',3),(156,'Train to Pakistan','Khushwant Singh','Historical Fiction','9780143065883',2),(157,'The Immortals of Meluha','Amish Tripathi','Fantasy','9789380658742',5),(159,'The Namesake','Jhumpa Lahiri','Fiction','9780618485222',3),(160,'Interpreter of Maladies','Jhumpa Lahiri','Short Stories','9780618104505',4),(161,'The Shadow Lines','Amitav Ghosh','Fiction','9780618331857',2),(162,'The Inheritance of Loss','Kiran Desai','Fiction','9780802142818',3),(163,'Sea of Poppies','Amitav Ghosh','Historical Fiction','9780719568961',4),(164,'The Hungry Tide','Amitav Ghosh','Fiction','9780618711666',2),(165,'Shantaram','Gregory David Roberts','Fiction','9780312330538',5),(166,'The Illicit Happiness of Other People','Manu Joseph','Fiction','9780393338683',1),(167,'The Lowland','Jhumpa Lahiri','Fiction','9780307278265',3),(168,'Serious Men','Manu Joseph','Fiction','9780393338645',2),(169,'The Story of My Experiments with Truth','Mahatma Gandhi','Autobiography','9780807059098',4),(170,'Discovery of India','Jawaharlal Nehru','History','9780143031031',3),(171,'India After Gandhi','Ramachandra Guha','History','9780330396110',2),(172,'The Great Indian Novel','Shashi Tharoor','Fiction','9780140120493',1),(173,'Five Point Someone','Chetan Bhagat','Fiction','9788129104601',5),(174,'2 States: The Story of My Marriage','Chetan Bhagat','Fiction','9788129115300',4),(175,'The 3 Mistakes of My Life','Chetan Bhagat','Fiction','9788129113726',3),(176,'An Era of Darkness','Shashi Tharoor','History','9789383064656',2),(177,'Gora','Rabindranath Tagore','Fiction','9788171677559',4),(178,'The Home and the World','Rabindranath Tagore','Fiction','9780140449860',3),(179,'Chokher Bali','Rabindranath Tagore','Fiction','9788171676286',2),(180,'Malgudi Days','R.K. Narayan','Short Stories','9780143039655',5),(181,'The Guide','R.K. Narayan','Fiction','9780143039648',4),(182,'Swami and Friends','R.K. Narayan','Fiction','9780226568317',3),(183,'A Fine Balance','Rohinton Mistry','Fiction','9781400030651',2),(184,'Family Matters','Rohinton Mistry','Fiction','9780375703423',1),(185,'Such a Long Journey','Rohinton Mistry','Fiction','9780679738718',3),(186,'The Space Between Us','Thrity Umrigar','Fiction','9780060791568',4),(187,'Baumgartnerâ€™s Bombay','Anita Desai','Fiction','9780395473948',2),(188,'Clear Light of Day','Anita Desai','Fiction','9780618074518',3),(189,'In Custody','Anita Desai','Fiction','9780060390372',1),(191,'The Village by the Sea','Anita Desai','Fiction','9780143335498',3),(192,'The Blue Umbrella','Ruskin Bond','Childrenâ€™s','9788171673407',5),(193,'Room on the Roof','Ruskin Bond','Fiction','9780143336860',4),(194,'Delhi Is Not Far','Ruskin Bond','Fiction','9780144000951',2),(195,'The Night Train at Deoli','Ruskin Bond','Short Stories','9780140145731',3),(196,'Our Trees Still Grow in Dehra','Ruskin Bond','Short Stories','9780140169027',4),(197,'The Shiva Trilogy','Amish Tripathi','Fantasy','9789383260157',5),(198,'Sacred Games','Vikram Chandra','Fiction','9780061130359',3),(199,'The Rozabal Line','Ashwin Sanghi','Thriller','9789383260911',2),(200,'Chanakyaâ€™s Chant','Ashwin Sanghi','Historical Fiction','9789381626610',4);

/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `issued_books`
--

DROP TABLE IF EXISTS `issued_books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `issued_books` (
  `issue_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int DEFAULT NULL,
  `book_id` int DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `returned` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`issue_id`),
  KEY `student_id` (`student_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `issued_books_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE CASCADE,
  CONSTRAINT `issued_books_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `issued_books`
--

LOCK TABLES `issued_books` WRITE;
/*!40000 ALTER TABLE `issued_books` DISABLE KEYS */;
/*!40000 ALTER TABLE `issued_books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `librarians`
--

DROP TABLE IF EXISTS `librarians`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `librarians` (
  `librarian_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`librarian_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `librarians`
--

LOCK TABLES `librarians` WRITE;
/*!40000 ALTER TABLE `librarians` DISABLE KEYS */;
INSERT INTO `librarians` VALUES (3,'Lib One','lib1@lib.com','lib123');
/*!40000 ALTER TABLE `librarians` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int DEFAULT NULL,
  `message` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `seen` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`notification_id`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `returns`
--

DROP TABLE IF EXISTS `returns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `returns` (
  `return_id` int NOT NULL AUTO_INCREMENT,
  `issue_id` int DEFAULT NULL,
  `return_date` date DEFAULT NULL,
  `fine` decimal(6,2) DEFAULT NULL,
  PRIMARY KEY (`return_id`),
  KEY `issue_id` (`issue_id`),
  CONSTRAINT `returns_ibfk_1` FOREIGN KEY (`issue_id`) REFERENCES `issued_books` (`issue_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `returns`
--

LOCK TABLES `returns` WRITE;
/*!40000 ALTER TABLE `returns` DISABLE KEYS */;
/*!40000 ALTER TABLE `returns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `student_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (2,'Stu One','stu1@stu.com','stu123',1);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-29 10:06:33
