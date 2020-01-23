package dk.digitalidentity.os2faktor.log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.os2faktor.log.model.AuditLog;
import dk.digitalidentity.os2faktor.security.SecurityUtil;

@Service
public class AuditLogService {

	@Autowired
	private AuditLogDao auditLogDao;

	public void log(String changedAccountName) {
		AuditLog entry = new AuditLog();
		entry.setTimestamp(new Date());
		entry.setUsername(SecurityUtil.getCurrentlyLoggedInUser());
		entry.setUserSsn(SecurityUtil.getSsn());
		entry.setChangedAccount(changedAccountName);

		auditLogDao.save(entry);
	}

	public List<AuditLog> findByChangedAccountIn(List<String> sAMAccountNames) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -2);
		Date twoMonthsAgo = calendar.getTime();
		
		return auditLogDao.findByChangedAccountInAndTimestampAfter(sAMAccountNames, twoMonthsAgo);
	}
}