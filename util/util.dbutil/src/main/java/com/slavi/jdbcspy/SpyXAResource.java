package com.slavi.jdbcspy;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SpyXAResource<TT extends XAResource> extends Spy<TT> implements XAResource {

	public SpyXAResource(TT delegate) {
		super(delegate);
	}

	static String getXidName(Xid xid) {
		return xid == null ? null : bytesToHex(xid.getGlobalTransactionId());
	}

	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {
		try (SpyTimer tt = new SpyTimer(log, "commit " + getXidName(xid) + (onePhase ? " one phase":" two phase"))) {
			t.commit(xid, onePhase);
		}
	}

	@Override
	public void end(Xid xid, int flags) throws XAException {
		try (SpyTimer tt = new SpyTimer(log, "end " + getXidName(xid))) {
			t.end(xid, flags);
		}
	}

	@Override
	public void forget(Xid xid) throws XAException {
		try (SpyTimer tt = new SpyTimer(log, "forget " + getXidName(xid))) {
			t.forget(xid);
		}
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return t.getTransactionTimeout();
	}

	@Override
	public boolean isSameRM(XAResource xares) throws XAException {
		return t.isSameRM(xares);
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		try (SpyTimer tt = new SpyTimer(log, "prepare " + getXidName(xid))) {
			return t.prepare(xid);
		}
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
		return t.recover(flag);
	}

	@Override
	public void rollback(Xid xid) throws XAException {
		try (SpyTimer tt = new SpyTimer(log, "rollback " + getXidName(xid))) {
			t.rollback(xid);
		}
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		return t.setTransactionTimeout(seconds);
	}

	@Override
	public void start(Xid xid, int flags) throws XAException {
		try (SpyTimer tt = new SpyTimer(log, "start " + getXidName(xid))) {
			t.start(xid, flags);
		}
	}
}
