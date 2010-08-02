/*
Navicat MySQL Data Transfer

Source Server         : gms
Source Server Version : 50089
Source Host           : localhost:3306
Source Database       : v83

Target Server Type    : MYSQL
Target Server Version : 50089
File Encoding         : 65001

Date: 2010-08-02 20:44:41
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `reactordrops`
-- ----------------------------
DROP TABLE IF EXISTS `reactordrops`;
CREATE TABLE `reactordrops` (
  `reactordropid` int(10) unsigned NOT NULL auto_increment,
  `reactorid` int(11) NOT NULL,
  `itemid` int(11) NOT NULL,
  `chance` int(11) NOT NULL,
  PRIMARY KEY  (`reactordropid`),
  KEY `reactorid` (`reactorid`)
) ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of reactordrops
-- ----------------------------
INSERT INTO `reactordrops` VALUES ('1', '2001', '4031161', '1');
INSERT INTO `reactordrops` VALUES ('2', '2001', '4031162', '1');
INSERT INTO `reactordrops` VALUES ('3', '2001', '2010009', '2');
INSERT INTO `reactordrops` VALUES ('4', '2001', '2010000', '4');
INSERT INTO `reactordrops` VALUES ('5', '2001', '2000000', '4');
INSERT INTO `reactordrops` VALUES ('6', '2001', '2000001', '7');
INSERT INTO `reactordrops` VALUES ('7', '2001', '2000002', '10');
INSERT INTO `reactordrops` VALUES ('8', '2001', '2000003', '15');
INSERT INTO `reactordrops` VALUES ('9', '1012000', '2000000', '6');
INSERT INTO `reactordrops` VALUES ('10', '1012000', '4000003', '6');
INSERT INTO `reactordrops` VALUES ('11', '1012000', '4031150', '3');
INSERT INTO `reactordrops` VALUES ('12', '1072000', '4031165', '4');
INSERT INTO `reactordrops` VALUES ('13', '1102000', '4000136', '1');
INSERT INTO `reactordrops` VALUES ('14', '1102001', '4000136', '1');
INSERT INTO `reactordrops` VALUES ('15', '1102002', '4000136', '1');
INSERT INTO `reactordrops` VALUES ('16', '2002000', '2000002', '4');
INSERT INTO `reactordrops` VALUES ('17', '2002000', '2000001', '2');
INSERT INTO `reactordrops` VALUES ('18', '2002000', '4031198', '2');
INSERT INTO `reactordrops` VALUES ('19', '2112000', '2000004', '1');
INSERT INTO `reactordrops` VALUES ('20', '2112001', '2020001', '1');
INSERT INTO `reactordrops` VALUES ('21', '2112004', '4001016', '1');
INSERT INTO `reactordrops` VALUES ('22', '2112005', '4001015', '1');
INSERT INTO `reactordrops` VALUES ('23', '2112003', '2000005', '1');
INSERT INTO `reactordrops` VALUES ('24', '2112007', '2022001', '1');
INSERT INTO `reactordrops` VALUES ('25', '2112008', '2000004', '1');
INSERT INTO `reactordrops` VALUES ('26', '2112009', '2020001', '1');
INSERT INTO `reactordrops` VALUES ('27', '2112010', '2000005', '1');
INSERT INTO `reactordrops` VALUES ('28', '2112011', '4001016', '1');
INSERT INTO `reactordrops` VALUES ('29', '2112012', '4001015', '1');
INSERT INTO `reactordrops` VALUES ('30', '2112014', '4001018', '1');
INSERT INTO `reactordrops` VALUES ('31', '2112016', '4001113', '1');
INSERT INTO `reactordrops` VALUES ('32', '2112017', '4001114', '1');
INSERT INTO `reactordrops` VALUES ('33', '2202000', '4031094', '1');
INSERT INTO `reactordrops` VALUES ('34', '2212000', '4031142', '2');
INSERT INTO `reactordrops` VALUES ('35', '2212000', '2000002', '3');
INSERT INTO `reactordrops` VALUES ('36', '2212001', '2000002', '3');
INSERT INTO `reactordrops` VALUES ('37', '2212002', '2000002', '3');
INSERT INTO `reactordrops` VALUES ('38', '2212001', '4031141', '2');
INSERT INTO `reactordrops` VALUES ('39', '2212002', '4031143', '2');
INSERT INTO `reactordrops` VALUES ('40', '2212003', '4031107', '2');
INSERT INTO `reactordrops` VALUES ('41', '2212004', '4031116', '2');
INSERT INTO `reactordrops` VALUES ('42', '2212004', '2000001', '2');
INSERT INTO `reactordrops` VALUES ('43', '2212005', '4031136', '8');
INSERT INTO `reactordrops` VALUES ('44', '2222000', '4031231', '2');
INSERT INTO `reactordrops` VALUES ('45', '2222000', '4031258', '2');
INSERT INTO `reactordrops` VALUES ('46', '2222000', '2000002', '3');
INSERT INTO `reactordrops` VALUES ('47', '2302000', '2000001', '3');
INSERT INTO `reactordrops` VALUES ('48', '2302000', '2022040', '6');
INSERT INTO `reactordrops` VALUES ('49', '2302000', '4031274', '50');
INSERT INTO `reactordrops` VALUES ('50', '2302000', '4031275', '50');
INSERT INTO `reactordrops` VALUES ('51', '2302000', '4031276', '50');
INSERT INTO `reactordrops` VALUES ('52', '2302000', '4031277', '50');
INSERT INTO `reactordrops` VALUES ('53', '2302000', '4031278', '50');
INSERT INTO `reactordrops` VALUES ('54', '2302001', '2000002', '3');
INSERT INTO `reactordrops` VALUES ('55', '2302001', '2022040', '4');
INSERT INTO `reactordrops` VALUES ('56', '2302002', '2000001', '3');
INSERT INTO `reactordrops` VALUES ('57', '2302002', '2022040', '8');
INSERT INTO `reactordrops` VALUES ('58', '2302003', '4161017', '1');
INSERT INTO `reactordrops` VALUES ('59', '2302005', '4031508', '1');
INSERT INTO `reactordrops` VALUES ('60', '2502000', '2022116', '1');
INSERT INTO `reactordrops` VALUES ('61', '2052001', '2022116', '1');
INSERT INTO `reactordrops` VALUES ('62', '9202000', '1032033', '1');
INSERT INTO `reactordrops` VALUES ('63', '9202009', '1032033', '1');
INSERT INTO `reactordrops` VALUES ('64', '2202001', '4031092', '1');
INSERT INTO `reactordrops` VALUES ('65', '9202001', '4001025', '1');
INSERT INTO `reactordrops` VALUES ('66', '9202002', '4001037', '1');
INSERT INTO `reactordrops` VALUES ('67', '9202003', '4001029', '1');
INSERT INTO `reactordrops` VALUES ('68', '9202004', '4001030', '1');
INSERT INTO `reactordrops` VALUES ('69', '9202005', '4001031', '1');
INSERT INTO `reactordrops` VALUES ('70', '9202006', '4001032', '1');
INSERT INTO `reactordrops` VALUES ('71', '9202007', '4001033', '1');
INSERT INTO `reactordrops` VALUES ('72', '9202008', '4001034', '1');
INSERT INTO `reactordrops` VALUES ('73', '9202012', '2020014', '3');
INSERT INTO `reactordrops` VALUES ('74', '9202012', '2020015', '3');
INSERT INTO `reactordrops` VALUES ('75', '9202012', '2001001', '3');
INSERT INTO `reactordrops` VALUES ('76', '9202012', '2000004', '3');
INSERT INTO `reactordrops` VALUES ('77', '9202012', '2000005', '3');
INSERT INTO `reactordrops` VALUES ('78', '9202012', '2000001', '3');
INSERT INTO `reactordrops` VALUES ('79', '9202012', '2000002', '3');
INSERT INTO `reactordrops` VALUES ('80', '9202012', '2000006', '3');
INSERT INTO `reactordrops` VALUES ('81', '9202012', '2001002', '3');
INSERT INTO `reactordrops` VALUES ('82', '9202012', '2040504', '40');
INSERT INTO `reactordrops` VALUES ('83', '9202012', '2040501', '40');
INSERT INTO `reactordrops` VALUES ('84', '9202012', '2040513', '40');
INSERT INTO `reactordrops` VALUES ('85', '9202012', '2040516', '40');
INSERT INTO `reactordrops` VALUES ('86', '9202012', '2041007', '40');
INSERT INTO `reactordrops` VALUES ('87', '9202012', '2041010', '40');
INSERT INTO `reactordrops` VALUES ('88', '9202012', '2041004', '40');
INSERT INTO `reactordrops` VALUES ('89', '9202012', '2041001', '40');
INSERT INTO `reactordrops` VALUES ('90', '9202012', '2041019', '40');
INSERT INTO `reactordrops` VALUES ('91', '9202012', '2041022', '40');
INSERT INTO `reactordrops` VALUES ('92', '9202012', '2041013', '40');
INSERT INTO `reactordrops` VALUES ('93', '9202012', '2041016', '40');
INSERT INTO `reactordrops` VALUES ('94', '9202012', '2040301', '40');
INSERT INTO `reactordrops` VALUES ('95', '9202012', '2040704', '40');
INSERT INTO `reactordrops` VALUES ('96', '9202012', '2040707', '40');
INSERT INTO `reactordrops` VALUES ('97', '9202012', '2040701', '40');
INSERT INTO `reactordrops` VALUES ('98', '9202012', '2040804', '40');
INSERT INTO `reactordrops` VALUES ('99', '9202012', '2040801', '40');
INSERT INTO `reactordrops` VALUES ('100', '9202012', '2040004', '40');
INSERT INTO `reactordrops` VALUES ('101', '9202012', '2040001', '40');
INSERT INTO `reactordrops` VALUES ('102', '9202012', '2290009', '60');
INSERT INTO `reactordrops` VALUES ('103', '9202012', '2290031', '60');
INSERT INTO `reactordrops` VALUES ('104', '9202012', '2290039', '60');
INSERT INTO `reactordrops` VALUES ('105', '9202012', '2290033', '60');
INSERT INTO `reactordrops` VALUES ('106', '9202012', '2290045', '60');
INSERT INTO `reactordrops` VALUES ('107', '9202012', '2290081', '60');
INSERT INTO `reactordrops` VALUES ('108', '9202012', '2290083', '60');
INSERT INTO `reactordrops` VALUES ('109', '9202012', '2290087', '60');
INSERT INTO `reactordrops` VALUES ('110', '9202012', '2290060', '60');
INSERT INTO `reactordrops` VALUES ('111', '9202012', '2290073', '60');
INSERT INTO `reactordrops` VALUES ('112', '9202012', '2100000', '250');
INSERT INTO `reactordrops` VALUES ('113', '9102000', '4001100', '50');
INSERT INTO `reactordrops` VALUES ('114', '9102002', '4001096', '50');
INSERT INTO `reactordrops` VALUES ('115', '9102002', '4001098', '50');
INSERT INTO `reactordrops` VALUES ('116', '9102003', '4001096', '50');
INSERT INTO `reactordrops` VALUES ('117', '9102004', '4001097', '50');
INSERT INTO `reactordrops` VALUES ('118', '9102005', '4001098', '50');
INSERT INTO `reactordrops` VALUES ('119', '9102006', '4001099', '50');
INSERT INTO `reactordrops` VALUES ('120', '9102007', '4001099', '50');
INSERT INTO `reactordrops` VALUES ('121', '9102007', '4001100', '50');
INSERT INTO `reactordrops` VALUES ('122', '1402000', '4032309', '1');
INSERT INTO `reactordrops` VALUES ('123', '1402000', '4032310', '1');
