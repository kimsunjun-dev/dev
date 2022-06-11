package org.tmt.core.link.external.letter;

import java.util.Map;

import org.tmt.core.service.LinkLetterService;

public interface Deliver {
	public String deliver(Map sender, Map receiver, LinkLetterService service );
	public void initTest(boolean isTestSend, String testClpNo, String testEmail);
}
