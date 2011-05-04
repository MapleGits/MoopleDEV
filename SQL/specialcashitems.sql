/*
Navicat MySQL Data Transfer

Source Server         : gms
Source Server Version : 50144
Source Host           : localhost:3306
Source Database       : moopledev

Target Server Type    : MYSQL
Target Server Version : 50144
File Encoding         : 65001

Date: 2011-04-15 16:37:34
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `specialcashitems`
-- ----------------------------
DROP TABLE IF EXISTS `specialcashitems`;
CREATE TABLE `specialcashitems` (
  `id` int(11) NOT NULL,
  `sn` int(11) NOT NULL,
  `modifier` int(11) NOT NULL,
  `info` int(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of specialcashitems
-- ----------------------------
INSERT INTO `specialcashitems` VALUES ('1', '92000017', '1024', '1');
