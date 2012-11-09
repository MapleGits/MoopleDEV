DROP TABLE IF EXISTS `char_area_info`;
DROP TABLE IF EXISTS `area_info`;
CREATE TABLE `area_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `charid` int(11) NOT NULL,
  `area` int(11) NOT NULL,
  `info` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cid` (`charid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;