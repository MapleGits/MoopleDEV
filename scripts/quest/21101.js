var status = -1;

function start(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
        qm.sendYesNo("#b(Are you certain that you were the hero that wielded the #p1201001#? Yes, you're sure. You better grab the #p1201001# really tightly. Surely it will react to you.)#k");
    } else if (status == 1) {
		if (mode == 0 && type == 2) {
			qm.sendNext("#b(You need to think about this for a second...)#k");
		} else {
			qm.gainItem(1142129, true);
			qm.forceCompleteQuest();
			qm.changeJobById(2100);
			qm.teachSkill(20009000, 1, 0, -1);
			qm.earnTitle("You have acquired the Pig's Weakness skill.");
			qm.sendNext("#b(You might be starting to remember something...)#k", 3);
		}
		qm.dispose();
	}
}