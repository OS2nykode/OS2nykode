package dk.digitalidentity.os2faktor.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import dk.digitalidentity.os2faktor.service.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SQLService {
	private HikariDataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	@Value("${sql.connection.url:}")
	private String sqlConnectionUrl;
	
	@Value("${sql.connection.username:}")
	private String sqlConnectionUsername;
	
	@Value("${sql.connection.password:}")
	private String sqlConnectionPassword;
	
	// SELECT xx FROM zzz WHERE sss = ?
	@Value("${sql.query.getSsn:}")
	private String sqlQueryGetSsn;
	
	// SELECT xx AS name, yy AS sAMAccountName FROM zzz WHERE sss = ?
	@Value("${sql.query.getSAMAccountNames:}")
	private String sqlQueryGetSAMAccountNames;

	private DataSource getDataSource() {
		if (this.dataSource == null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(sqlConnectionUrl);
			config.setUsername(sqlConnectionUsername);
			config.setPassword(sqlConnectionPassword);
			
			this.dataSource = new HikariDataSource(config);
		}

		return this.dataSource;
	}
	
	private JdbcTemplate getJdbcTemplate() {
		if (this.jdbcTemplate == null) {
			this.jdbcTemplate = new JdbcTemplate(getDataSource());
		}
		
		return this.jdbcTemplate;
	}

	public String getSsn(String sAMAccountName) {
		try {
			String ssn = getJdbcTemplate().queryForObject(sqlQueryGetSsn, new Object[] { sAMAccountName }, String.class);
			
			return ssn;
		}
		catch (Exception ex) {
			log.error("Failed to query SQL for SSN", ex);
		}

		return null;
	}
	
	public List<UserDTO> getSAMAccountNames(String ssn) {
		try {
			List<UserDTO> result = getJdbcTemplate().query(sqlQueryGetSAMAccountNames, new Object[] { ssn }, new RowMapper<UserDTO>() {
	
				@Override
				public UserDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
					UserDTO dto = new UserDTO();
					dto.setName(rs.getString("name"));
					dto.setSAMAccountName(rs.getString("sAMAccountName"));
	
					return dto;
				}
			});
			
			return result;
		}
		catch (Exception ex) {
			log.error("Failed to query SQL for sAMAccountNames", ex);
		}
		
		return new ArrayList<UserDTO>();
	}
}
