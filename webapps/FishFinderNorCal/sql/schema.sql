CREATE DATABASE IF NOT EXISTS fishfindernorcaldb;
USE fishfindernorcaldb;

CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `account_status` varchar(20) NOT NULL DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY (`username`),
  UNIQUE KEY (`email`)
) ENGINE=InnoDB;

CREATE TABLE `moderators` (
  `user_id` int NOT NULL,
  `mod_level` varchar(20) NOT NULL DEFAULT 'junior',
  `appointed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

CREATE TABLE `locationtypes` (
  `type_id` int NOT NULL AUTO_INCREMENT,
  `type_name` varchar(50) NOT NULL,
  PRIMARY KEY (`type_id`),
  UNIQUE KEY (`type_name`)
) ENGINE=InnoDB;

CREATE TABLE `locations` (
  `location_id` int NOT NULL AUTO_INCREMENT,
  `type_id` int NOT NULL,
  `location_name` varchar(100) NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `description` text,
  `created_by_user_id` int NOT NULL,
  PRIMARY KEY (`location_id`),
  FOREIGN KEY (`type_id`) REFERENCES `locationtypes` (`type_id`),
  FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

CREATE TABLE `species` (
  `species_id` int NOT NULL AUTO_INCREMENT,
  `common_name` varchar(100) NOT NULL,
  `scientific_name` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`species_id`),
  UNIQUE KEY (`common_name`)
) ENGINE=InnoDB;

CREATE TABLE `locationspecies` (
  `location_id` int NOT NULL,
  `species_id` int NOT NULL,
  `evidence_count` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`location_id`, `species_id`),
  FOREIGN KEY (`location_id`) REFERENCES `locations` (`location_id`),
  FOREIGN KEY (`species_id`) REFERENCES `species` (`species_id`)
) ENGINE=InnoDB;

CREATE TABLE `catchreports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `location_id` int NOT NULL,
  `species_id` int NOT NULL,
  `catch_date` date NOT NULL,
  `time_of_day` time DEFAULT NULL,
  `weight` decimal(6,2) DEFAULT NULL,
  `size` decimal(5,2) DEFAULT NULL,
  `notes` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  FOREIGN KEY (`location_id`) REFERENCES `locations` (`location_id`),
  FOREIGN KEY (`species_id`) REFERENCES `species` (`species_id`)
) ENGINE=InnoDB;

CREATE TABLE `comments` (
  `comment_id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL,
  `user_id` int NOT NULL,
  `comment_text` text NOT NULL,
  `commented_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  FOREIGN KEY (`report_id`) REFERENCES `catchreports` (`report_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

CREATE TABLE `favorites` (
  `user_id` int NOT NULL,
  `location_id` int NOT NULL,
  `favorited_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `location_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  FOREIGN KEY (`location_id`) REFERENCES `locations` (`location_id`)
) ENGINE=InnoDB;

CREATE TABLE `bans` (
  `ban_id` int NOT NULL AUTO_INCREMENT,
  `moderator_user_id` int NOT NULL,
  `target_user_id` int NOT NULL,
  `reason` text NOT NULL,
  `ban_start` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ban_end` datetime DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`ban_id`),
  FOREIGN KEY (`moderator_user_id`) REFERENCES `moderators` (`user_id`),
  FOREIGN KEY (`target_user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB;

-- seed data

INSERT INTO `users` VALUES (1,'riverking','riverking@email.com','e6c3da5b206634d7f3f3586d747ffdb36b5c675757b380c6a5fe5c570c714349','active','2025-01-10 08:00:00'),(2,'lakequeen','lakequeen@email.com','1ba3d16e9881959f8c9a9762854f72c6e6321cdd44358a10a4e939033117eab9','active','2025-01-12 09:30:00'),(3,'bassmaster','bassmaster@email.com','3acb59306ef6e660cf832d1d34c4fba3d88d616f0bb5c2a9e0f82d18ef6fc167','active','2025-01-15 10:00:00'),(4,'troutchaser','troutchaser@email.com','a417b5dc3d06d15d91c6687e27fc1705ebc56b3b2d813abe03066e5643fe4e74','active','2025-02-01 11:00:00'),(5,'catfishpro','catfishpro@email.com','0eeac8171768d0cdef3a20fee6db4362d019c91e10662a6b55186336e1a42778','active','2025-02-05 07:45:00'),(6,'pikemaster','pikemaster@email.com','5c4950c94a3461441c356afa783f76b83b38fd65f730f291403efbcc798acc1f','suspended','2025-02-10 14:00:00'),(7,'surfcaster','surfcaster@email.com','1526f5e0e31d42fe1c3664ce923ac22ac1333417a90b32043797ac454cd03112','active','2025-03-01 06:30:00'),(8,'flyrodder','flyrodder@email.com','c8fea5b0b76dc690feaf5544749f99b40e78e2a37c0e867a086696509416302a','active','2025-03-05 16:00:00'),(9,'reelhunter','reelhunter@email.com','2d4589473fb3f4581d7452cd25182159d68d2a50056a0cce35a529b010e32f2b','banned','2025-03-10 12:00:00'),(10,'pondhopper','pondhopper@email.com','b35892cb8b089e03e4420b94df688122a2b76d4ad0f8b94ad20808bb029e48a5','active','2025-03-15 09:00:00');

INSERT INTO `moderators` VALUES (1,'admin','2025-01-10 08:00:00'),(2,'senior','2025-01-20 10:00:00'),(3,'junior','2025-02-01 12:00:00'),(4,'junior','2025-02-15 09:00:00'),(5,'senior','2025-03-01 08:00:00'),(6,'junior','2025-04-01 09:00:00'),(7,'junior','2025-03-10 11:00:00'),(8,'junior','2025-03-15 14:00:00'),(9,'junior','2025-04-05 13:00:00'),(10,'junior','2025-03-20 10:00:00');

INSERT INTO `locationtypes` VALUES (7,'Bay'),(10,'Canal'),(5,'Creek'),(1,'Lake'),(8,'Pier'),(3,'Pond'),(4,'Reservoir'),(2,'River'),(9,'Shore'),(6,'Stream');

INSERT INTO `locations` VALUES (1,1,'Mirror Lake','Salem','100 Lake Rd','Calm lake surrounded by pines.',1),(2,2,'Eagle River','Vail','200 River Blvd','Fast-flowing mountain river.',2),(3,3,'Willow Pond','Springfield','15 Pond Ln','Small neighborhood pond.',3),(4,4,'Cedar Reservoir','Portland','88 Dam St','Large reservoir with boat access.',4),(5,5,'Trout Creek','Bend','42 Creek Way','Narrow creek, great for fly fishing.',5),(6,6,'Silver Stream','Aspen','7 Stream Dr','Mountain stream with clear water.',1),(7,7,'Sunset Bay','Tampa','300 Bay Ave','Saltwater bay with mangroves.',7),(8,8,'Harbor Pier','San Diego','1 Pier Walk','Public fishing pier, open 24/7.',8),(9,9,'Rocky Shore','Bar Harbor','55 Shore Rd','Rocky coastline, striped bass spot.',2),(10,10,'Delta Canal','Sacramento','99 Canal Blvd','Slow-moving canal, catfish haven.',10);

INSERT INTO `species` VALUES (1,'Largemouth Bass','Micropterus salmoides'),(2,'Rainbow Trout','Oncorhynchus mykiss'),(3,'Channel Catfish','Ictalurus punctatus'),(4,'Northern Pike','Esox lucius'),(5,'Bluegill','Lepomis macrochirus'),(6,'Walleye','Sander vitreus'),(7,'Crappie','Pomoxis nigromaculatus'),(8,'Striped Bass','Morone saxatilis'),(9,'Brown Trout','Salmo trutta'),(10,'Carp','Cyprinus carpio');

INSERT INTO `locationspecies` VALUES (1,1,25),(1,5,18),(2,2,30),(2,9,12),(3,5,40),(4,6,22),(5,9,15),(7,8,35),(8,1,10),(10,3,28);

INSERT INTO `catchreports` VALUES (1,1,1,1,'2025-04-10','06:30:00',4.50,18.00,'Caught near the dock on a spinnerbait.','2026-03-16 18:42:09'),(2,2,2,2,'2025-04-11','07:00:00',2.10,14.50,'Fly fishing upstream, beautiful morning.','2026-03-16 18:42:09'),(3,3,3,5,'2025-04-12','08:15:00',0.80,7.00,'Small bluegill on a worm.','2026-03-16 18:42:09'),(4,4,5,9,'2025-04-13','09:00:00',3.20,16.00,'Brown trout in the deep pool.','2026-03-16 18:42:09'),(5,5,10,3,'2025-04-14','19:30:00',6.00,22.00,'Big catfish on chicken liver bait.','2026-03-16 18:42:09'),(6,7,7,8,'2025-04-15','17:00:00',8.50,26.00,'Striped bass from the bay at sunset.','2026-03-16 18:42:09'),(7,8,8,1,'2025-04-16','11:00:00',3.80,17.00,'Bass off the pier, topwater lure.','2026-03-16 18:42:09'),(8,1,4,6,'2025-04-17','05:45:00',5.00,21.00,'Walleye trolling at dawn.','2026-03-16 18:42:09'),(9,10,6,2,'2025-04-18','10:30:00',1.90,13.00,'Rainbow on a dry fly in the stream.','2026-03-16 18:42:09'),(10,2,9,8,'2025-04-19','16:00:00',7.20,24.50,'Shore casting with cut bait.','2026-03-16 18:42:09');

INSERT INTO `comments` VALUES (1,1,2,'Nice catch! What pound test line?','2025-04-10 10:00:00'),(2,1,3,'Mirror Lake is my favorite spot too.','2025-04-10 11:30:00'),(3,2,1,'Great trout! What fly pattern did you use?','2025-04-11 12:00:00'),(4,3,4,'Bluegill are so fun on ultralight gear.','2025-04-12 14:00:00'),(5,4,5,'Brown trout are tough to find there.','2025-04-13 15:00:00'),(6,5,7,'Chicken liver always works for catfish!','2025-04-14 21:00:00'),(7,6,8,'Sunset Bay never disappoints.','2025-04-15 19:00:00'),(8,7,10,'Topwater on a pier? Bold move!','2025-04-16 13:00:00'),(9,8,3,'Walleye trolling tips?','2025-04-17 08:00:00'),(10,9,1,'That stream is a hidden gem.','2025-04-18 12:00:00');

INSERT INTO `favorites` VALUES (1,1,'2025-04-10 07:00:00'),(1,4,'2025-04-17 06:00:00'),(2,2,'2025-04-11 08:00:00'),(2,9,'2025-04-19 17:00:00'),(3,3,'2025-04-12 09:00:00'),(4,5,'2025-04-13 10:00:00'),(5,10,'2025-04-14 20:00:00'),(7,7,'2025-04-15 18:00:00'),(8,8,'2025-04-16 12:00:00'),(10,6,'2025-04-18 11:00:00');

INSERT INTO `bans` VALUES (1,1,9,'Repeated spam in comments.','2025-03-10 12:00:00','2025-04-10 12:00:00',1),(2,1,6,'Posting misleading catch photos.','2025-02-10 14:00:00','2025-03-10 14:00:00',0),(3,2,9,'Harassing other users.','2025-04-11 09:00:00',NULL,1),(4,2,6,'Continued misleading reports after warning.','2025-03-15 10:00:00','2025-04-15 10:00:00',0),(5,3,9,'Inappropriate language in comments.','2025-04-01 08:00:00','2025-04-08 08:00:00',0),(6,1,6,'Fake catch report submission.','2025-04-05 11:00:00','2025-05-05 11:00:00',1),(7,5,9,'Threatening other users.','2025-04-12 14:00:00',NULL,1),(8,4,6,'Duplicate account abuse.','2025-04-02 07:00:00','2025-04-16 07:00:00',0),(9,2,3,'Minor rule violation, first warning.','2025-04-10 16:00:00','2025-04-12 16:00:00',0),(10,1,10,'Suspected bot activity.','2025-04-15 09:00:00','2025-04-22 09:00:00',1);
