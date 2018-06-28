package com.slavi.dbutil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

public class ResultSetToStringHandler implements ResultSetHandler<String> {

	int recordsToSkip = 0;
	int recordsToShow = 10;
	boolean showHeader = true;
	int maxColumnWidth = 40;

	public String handle(ResultSet rs) throws SQLException {
		return ResultSetToString.resultSetToString(rs);
	}

	public int getRecordsToSkip() {
		return recordsToSkip;
	}

	public void setRecordsToSkip(int recordsToSkip) {
		this.recordsToSkip = recordsToSkip;
	}

	public int getRecordsToShow() {
		return recordsToShow;
	}

	public void setRecordsToShow(int recordsToShow) {
		this.recordsToShow = recordsToShow;
	}

	public boolean isShowHeader() {
		return showHeader;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	public int getMaxColumnWidth() {
		return maxColumnWidth;
	}

	public void setMaxColumnWidth(int maxColumnWidth) {
		this.maxColumnWidth = maxColumnWidth;
	}
}
