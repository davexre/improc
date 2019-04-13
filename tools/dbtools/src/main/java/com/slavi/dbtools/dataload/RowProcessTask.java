package com.slavi.dbtools.dataload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.dbutil.DbDataParser;
import com.slavi.dbutil.DbDataParserTemplate;
import com.slavi.util.concurrent.CloseableBlockingQueue;

public class RowProcessTask implements Callable {
	static Logger logParser = LoggerFactory.getLogger(DataLoad.log.getName() + ".parser");
	static Logger logSql = LoggerFactory.getLogger(DataLoad.log.getName() + ".sql");

	static class EntityDefWorkspace {
		EntityDef def;
		String lastSql;
		PreparedStatement ps;
		DbDataParserTemplate pt;
		DbDataParser dp;
	}

	Config cfg;
	CloseableBlockingQueue<Map> rows;

	public static String defaultDateFormats[] = {
			"EEE MMM dd HH:mm:ss zzz yyyy",
			"yyyy-MM-dd'T'HH:mm:ss.SSS",
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd HH:mm:ss SSS",
			"yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd HH:mm",
			"yyyy-MM-dd HH/mm/ss",
			"yyyy-MM-dd HH/mm",
			"yyyy-MM-dd",
			"dd/MM/yyyy'T'HH:mm:ss.SSS",
			"dd/MM/yyyy'T'HH:mm:ss",
			"dd/MM/yyyy'T'HH:mm",
			"dd/MM/yyyy HH:mm:ss.SSS",
			"dd/MM/yyyy HH:mm:ss",
			"dd/MM/yyyy HH:mm",
			"dd/MM/yyyy"
		};

	public RowProcessTask(Config cfg, CloseableBlockingQueue<Map> rows) {
		this.cfg = cfg;
		this.rows = rows;
	}

	VelocityContext ctx;
	Connection conn;
	int sqlCount;

	void debugPrint(Map<?,?> row) {
		if (!logParser.isTraceEnabled())
			return;
		StringBuilder r = new StringBuilder("Parser leaf");
		var l = new ArrayList<String>();
		var loop = row;
		while (loop != null) {
			l.clear();
			for (var i : loop.entrySet()) {
				if (DataLoad.tagParent.equals(i.getKey()) ||
					DataLoad.tagPath.equals(i.getKey()))
					continue;
				Object v = i.getValue();
				if (v instanceof Map) {
					v = i.getValue().toString();
				} else {
					v = StringUtils.abbreviate(v.toString(), 20);
				}
				l.add(StringEscapeUtils.escapeCsv(i.getKey() + "=" + v));
			}
			Collections.sort(l);
			r.append("\n").append("  Path").append("=").append(loop.get(DataLoad.tagPath));
			for (var i : l)
				r.append("; ").append(i);
			loop = (Map) loop.get(DataLoad.tagParent);
		}
		logParser.trace(r.toString());
	}

	@Override
	public Void call() throws Exception {
		ctx = cfg.makeContext();
		try (Connection conn = DriverManager.getConnection(
				DataLoad.applyTemplate(ctx, cfg.getUrlTemplate()),
				DataLoad.applyTemplate(ctx, cfg.getUsernameTemplate()),
				DataLoad.applyTemplate(ctx, cfg.getPasswordTemplate()))) {
			this.conn = conn;
			sqlCount = 0;
			if (cfg.getCommitEveryNumSqls() > 1)
				conn.setAutoCommit(false);

			Map rec;
			while ((rec = rows.take()) != null) {
				if (logParser.isTraceEnabled()) {
					debugPrint(rec);
				} else if (logParser.isDebugEnabled()) {
					logParser.debug("Parser leaf: _ID:{} _LINE:{} _COL:{} _INDEX:{} _NAME:{} _PATH:{} _VALUE:{} ",
							StringUtils.rightPad(String.valueOf(rec.get(DataLoad.tagId)), 4),
							StringUtils.rightPad(String.valueOf(rec.get(DataLoad.tagLine)), 4),
							StringUtils.rightPad(String.valueOf(rec.get(DataLoad.tagCol)), 4),
							StringUtils.rightPad(String.valueOf(rec.get(DataLoad.tagIndex)), 4),
							rec.get(DataLoad.tagName),
							rec.get(DataLoad.tagPath),
							rec.get(DataLoad.tagValue));
				}

				ctx.put("rec", rec);
				String path = (String) rec.get(DataLoad.tagPath);
				for (var def : cfg.defs) {
					if (def.getPathPattern().matcher(path).matches()) {
						applyDef(def);
					}
				}
			}
			conn.commit();
		}
		return null;
	}

	Map<EntityDef, EntityDefWorkspace> workspaces = new HashMap<>();

	void applyDef(EntityDef def) throws SQLException {
		ArrayList<String> params = new ArrayList<>();
		String sql = DataLoad.applyTemplate(ctx, def.getSqlTemplate());
		if (logSql.isDebugEnabled()) {
			for (var t : def.getParamTemplates()) {
				String val;
				try {
					val = DataLoad.applyTemplate(ctx, t);
				} catch (Throwable e) {
					logSql.trace("Error processing value", e);
					val = null;
				}
				params.add(val);
			}
			StringBuilder sb = new StringBuilder();
			sb.append("Matched def ").append(def.getName())
				.append("\n  SQL: ").append(sql);
			for (var i : params)
				sb.append("\n  Param: ").append(StringEscapeUtils.escapeCsv(StringUtils.abbreviate(i, 40)));
			logSql.debug(sb.toString());
		}

		EntityDefWorkspace ws = workspaces.get(def);
		if (ws == null) {
			ws = new EntityDefWorkspace();
			ws.def = def;
			workspaces.put(def, ws);
		}

		if (!sql.equals(ws.lastSql)) {
			ws.lastSql = sql;
			if (ws.ps != null)
				ws.ps.close();
			ws.ps = conn.prepareStatement(sql);
			if (ws.pt == null) {
				var df = def.getDateFormats();
				if (df == null || df.isEmpty())
					df = cfg.getDateFormats();
				if (df == null || df.isEmpty()) {
					ws.pt = new DbDataParserTemplate(defaultDateFormats);
				} else {
					ws.pt = new DbDataParserTemplate(df.toArray(new String[df.size()]));
				}
			}
			ws.dp = new DbDataParser(ws.ps, ws.pt);
		}
		try {
			ws.dp.reset();
			for (int i = 0; i < ws.dp.size(); i++) {
				String val = null;
				if (i < params.size())
					val = params.get(i);
				if (val == null && i < def.getParamTemplates().size()) {
					val = DataLoad.applyTemplate(ctx, def.getParamTemplates().get(i));
				}
				ws.dp.set(val);
			}
			ws.ps.executeUpdate();
			sqlCount++;
			if (cfg.getCommitEveryNumSqls() > 1 && sqlCount >= cfg.getCommitEveryNumSqls()) {
				sqlCount = 0;
				conn.commit();
			}
		} catch (Exception e) {
			logSql.info("Error processing row", e);
			// TODO:
		}
	}
}
