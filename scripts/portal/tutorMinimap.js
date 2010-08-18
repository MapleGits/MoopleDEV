function enter(pi) {
	pi.updateCygnusIntroState("minimap=clear;helper=clear");
	pi.updateQuest(20022, "1");
	pi.displayGuide(1);
	pi.blockPortal();
	return true;
}