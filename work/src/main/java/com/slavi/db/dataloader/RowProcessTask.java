package com.slavi.db.dataloader;

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
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.db.dataloader.cfg.Config;
import com.slavi.db.dataloader.cfg.EntityDef;
import com.slavi.derbi.dbload.DbDataParser;
import com.slavi.derbi.dbload.DbDataParserTemplate;
import com.slavi.util.CEncoder;
import com.slavi.util.concurrent.CloseableBlockingQueue;

public class RowProcessTask implements Callable {
	static Logger log = LoggerFactory.getLogger(DataLoader.class);

	static class EntityDefWorkspace {
		EntityDef def;
		String lastSql;
		PreparedStatement ps;
		DbDataParserTemplate pt;
		DbDataParser dp;
	}

	Config cfg;
	CloseableBlockingQueue<Map> rows;

	static String defaultDateFormats[] = {
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
		if (!log.isTraceEnabled())
			return;
		StringBuilder r = new StringBuilder("Parser leaf");
		var l = new ArrayList<String>();
		var loop = row;
		while (loop != null) {
			l.clear();
			for (var i : loop.entrySet()) {
				if (DataLoader.tagParent.equals(i.getKey()) ||
					DataLoader.tagPath.equals(i.getKey()))
					continue;
				l.add(CEncoder.encode(i.getKey() + "=" + StringUtils.abbreviate(i.getValue().toString(), 20)));
			}
			Collections.sort(l);
			r.append("\n").append("  Path").append("=").append(loop.get(DataLoader.tagPath));
			for (var i : l)
				r.append("; ").append(i);
			loop = (Map) loop.get(DataLoader.tagParent);
		}
		log.trace(r.toString());
	}

	@Override
	public Void call() throws Exception {
		ctx = DataLoader.makeContext();
		try (Connection conn = DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword())) {
			this.conn = conn;
			sqlCount = 0;
			if (cfg.getCommitEveryNumSqls() > 1)
				conn.setAutoCommit(false);

			Map rec;
			while ((rec = rows.take()) != null) {
				if (log.isTraceEnabled()) {
					debugPrint(rec);
				} else if (log.isDebugEnabled()) {
					log.debug("Parser leaf: _ID:{} _LINE:{} _COL:{} _INDEX:{} _NAME:{} _PATH:{} _VALUE:{} ",
							StringUtils.rightPad(String.valueOf(rec.get(DataLoader.tagId)), 4),
							StringUtils.rightPad(String.valueOf(rec.get(DataLoader.tagLine)), 4),
							StringUtils.rightPad(String.valueOf(rec.get(DataLoader.tagCol)), 4),
							StringUtils.rightPad(String.valueOf(rec.get(DataLoader.tagIndex)), 4),
							rec.get(DataLoader.tagName),
							rec.get(DataLoader.tagPath),
							rec.get(DataLoader.tagValue));
				}

				ctx.put("rec", rec);
				String path = (String) rec.get(DataLoader.tagPath);
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
		String sql = DataLoader.applyTemplate(ctx, def.getSqlTemplate());
		if (log.isInfoEnabled()) {
			for (var t : def.getParamTemplates()) {
				params.add(DataLoader.applyTemplate(ctx, t));
			}
			StringBuilder sb = new StringBuilder();
			sb.append("Matched def ").append(def.getName())
				.append("\n  SQL: ").append(sql);
			for (var i : params)
				sb.append("\n  Param: ").append(CEncoder.encode(StringUtils.abbreviate(i, 40)));
			log.info(sb.toString());
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
					val = DataLoader.applyTemplate(ctx, def.getParamTemplates().get(i));
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
			e.printStackTrace();
			// TODO:
		}
	}
}
