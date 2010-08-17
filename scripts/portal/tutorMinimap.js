function enter(pi) {
	pi.displayGuide(1);
	pi.updateCygnusIntroState("minimap=clear;helper=clear");
	pi.blockPortal();
	return true;
}